/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.util.bean;

import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.util.ReflectUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The dynamic partner to a PropertyExpression that represents the evaluation of the expression
 * against a particular bean or starting object.  When constructed the evaluation will examine
 * type information on the bean and nested properties to create a chain of type information for
 * the expression.  The evaluation can then be used (repeatedly) to determine the type of the
 * expression, retrieve it's value and set it's value - all against the supplied object.
 *
 * @author Tim Fennell
 * @since Stripes 1.4
 */
public class PropertyExpressionEvaluation {
    private PropertyExpression expression;
    private Object bean;
    private NodeEvaluation root, leaf;

    /**
     * Constructs a new PropertyExpressionEvaluation for the expression and bean supplied.
     * Loops through the expression creating NodeEvaluation objects corresponding to each
     * node in the expression and then fills in type information so that it is accessible
     * to all further calls.
     *
     * @param expression a PropertyExpression
     * @param bean a non-null bean against which to evaluate the expression
     */
    public PropertyExpressionEvaluation(PropertyExpression expression, Object bean) {
        this.expression = expression;
        this.bean = bean;

        for (Node node = expression.getRootNode(); node != null; node = node.getNext()) {
            NodeEvaluation evaluation = new NodeEvaluation(this, node);
            if (this.root == null) {
                this.root = evaluation;
                this.leaf = evaluation;
            }
            else {
                this.leaf.setNext(evaluation);
                evaluation.setPrevious(this.leaf);
                this.leaf = evaluation;
            }
        }

        fillInTypeInformation();
    }

    /**
     * Fetches the bean which was supplied as the starting point for evaluation in the
     * constructor to this evaluation.
     * @return the bean from which evaluation starts
     */
    public Object getBean() { return bean; }

    /**
     * Fetches the root (first) node in the evaluation, which can be used to traverse
     * through the nodes in series.
     * @return the root node in the evaluation
     */
    public NodeEvaluation getRootNode() {
        return this.root;
    }

    /**
     * Fetches the expression of which this is an evaluation.
     * @return the expression being evaluated
     */
    public PropertyExpression getExpression() {
        return expression;
    }

    /**
     * Examines the expression in context of the root bean provided to determine type
     * information for each node.  Does this by traversing a node at a time and examining
     * the various sources of type information available.
     */
    void fillInTypeInformation() {
        Type type = this.bean.getClass();

        for (NodeEvaluation current = this.root; current != null; current = current.getNext()) {
            // Firstly if the current type is a wildcard type of a type variable try and
            // figure out what the real value to use is
            while (type instanceof WildcardType || type instanceof TypeVariable) {
                if (type instanceof WildcardType) {
                    type = getWildcardTypeBound((WildcardType) type);
                }
                else {
                    type = getTypeVariableValue(current, ((TypeVariable<?>) type));
                }
            }

            // If it's an array, return the component type
            if (type instanceof GenericArrayType) {
                type = ((GenericArrayType) type).getGenericComponentType();
                current.setValueType(type);
                current.setKeyType(Integer.class);
                current.setType(NodeType.ArrayEntry);
                continue;
            }
            else if (type instanceof Class && ((Class<?>) type).isArray()) {
                type = ((Class<?>) type).getComponentType();
                current.setValueType(type);
                current.setKeyType(Integer.class);
                current.setType(NodeType.ArrayEntry);
                continue;
            }

            // Else if it's parameterized and it's a List or Map, get the next type
            if (type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) type;
                Type rawType = convertToClass(type, current);

                if (rawType instanceof Class) {
                    Class<?> rawClass = (Class<?>) rawType;
                    if (List.class.isAssignableFrom(rawClass)) {
                        type = ptype.getActualTypeArguments()[0];
                        current.setValueType(type);
                        current.setKeyType(Integer.class);
                        current.setType(NodeType.ListEntry);
                        continue;
                    }
                    else if (Map.class.isAssignableFrom(rawClass)) {
                        type = ptype.getActualTypeArguments()[1];
                        current.setValueType(type);
                        current.setKeyType( convertToClass(ptype.getActualTypeArguments()[0], current) );
                        current.setType(NodeType.MapEntry);
                        continue;
                    }
                    else {
                        // Since it could be user defined type with a type parameter we'll
                        // reassign the current type to be the raw type and let processing
                        // fall through to the bean property code
                        type = rawClass;
                    }
                }
                else {
                    // XXX Raw type is not a class?  What on earth do we do now?
                    break;
                }
            }

            // Else if it's just a regular class we can try looking for a property on it. If
            // no property exists, just bail out and return null immediately
            if (type instanceof Class) {
                Class<?> clazz = (Class<?>) type;
                String property = current.getNode().getStringValue();
                type = getBeanPropertyType(clazz, property);

                // XXX What do we do if type is a generic type?
                if (type != null) {
                    current.setValueType(type);
                    current.setType(NodeType.BeanProperty);
                }
            }

            // If we haven't gotten type information by now, try filling in with instance info
            if (type == null) {
                type = getTypeViaInstances(current);
                if (type == null) {
                    // FIXME: What do we do now?
                }
            }
        }
    }

    /**
     * Fetches the type of a property with the given name on the Class of the specified type.
     * Uses the methods first to fetch the generic type if a PropertyDescriptor can be found,
     * otherwise looks for a public field and returns its generic type.
     *
     * @param beanClass the class of the JavaBean containing the property
     * @param property the name of the property
     * @return the Type if it can be determined, or null otherwise
     */
    protected Type getBeanPropertyType(Class<?> beanClass, String property) {
        PropertyDescriptor pd = ReflectUtil.getPropertyDescriptor(beanClass, property);
        if (pd != null) {
            if (pd.getReadMethod() != null) {
                return untangleBridgeMethod(pd.getReadMethod()).getGenericReturnType();
            }
            else {
                return untangleBridgeMethod(pd.getWriteMethod()).getGenericParameterTypes()[0];
            }
        }
        else {
            Field field = ReflectUtil.getField(beanClass, property);
            if (field == null) {
                return null;
            }
            else  {
                return field.getGenericType();
            }
        }
    }

    /**
     * <p>Locates and returns a non-bridge method for the method supplied. In certain cases the
     * Introspector will return PropertyDescriptors that contain bridge methods for read
     * and write methods. This usually results from classes implementing generic interfaces
     * that contain accessor method specifications with type parameters. Since the bridge
     * methods have inappropriate/unhelpful return and parameter types it is necessary to
     * locate the non-bridge method and use that instead.</p>
     *
     * <p>When supplied with a non-bridge method, the method parameter passed in is returned
     * immediately and no other work is performed.</p>
     *
     * @param m a Method instance, potentially a bridge method
     * @return a non-bridge method instance if one is locatable, otherwise the method passed in
     */
    protected Method untangleBridgeMethod(Method m) {
        if (!m.isBridge()) return m;

        try {
            // If it's a setter method the only way to really find the right method
            // is to hope that there's only one setter with the same name and a single
            // parameter!!
            if (m.getParameterTypes().length == 1) { // deal with set methods
                String name = m.getName();
                for (Method m2 : m.getDeclaringClass().getMethods()) {
                    if (name.equals(m2.getName()) && m2 != m
                            && m2.getParameterTypes().length == m.getParameterTypes().length) {
                        return m2;
                    }
                }
            }
            else { // deal with get methods
                return m.getDeclaringClass().getMethod(m.getName());
            }
        }
        catch (Exception e) { /* Suppress. */ }

        return m;
    }

    /**
     * <p>Determines the type of the supplied node and sets appropriate information on the node.
     * The type is discovered by fetching (and instantiating if necessary) all prior values
     * in the expression to determine the actual type of the prior node.  The prior node is
     * then examined to determine the type of the node provided.</p>
     *
     * <p>After this method executes either 1) all necessary type information will be set on the
     * node and the appropriate type object returned or 2) an exception will be thrown.</p>
     *
     * @param end the node to instantiate up to and determine the type of
     * @return the Type of the node if possible
     * @throws NoSuchPropertyException if the previous node is a JavaBean (i.e. non-collection)
     *         node and does not contain a property with the corresponding name
     * @throws EvaluationException if the previous node is a List or Map and does not contain
     *         enough information to determine the type
     */
    @SuppressWarnings("unchecked")
	protected Type getTypeViaInstances(NodeEvaluation end)
        throws EvaluationException, NoSuchPropertyException {
        Object previous;
        Object value = this.bean;

        // First loop through and get to the pre-cursor node using the type info we have
        for (NodeEvaluation node = this.root; node != end; node = node.getNext()) {
            PropertyAccessor accessor = node.getType().getPropertyAccessor();
            previous = value;
            value = accessor.getValue(node, previous);

            if (value == null) {
                value = getDefaultValue(node);
            }
        }

        // Then determine how to fish for the next property in line
        previous = value;
        if (value instanceof Map) {
            value = ((Map) value).get(end.getNode().getTypedValue());
            if (value != null) {
                end.setType(NodeType.MapEntry);
                end.setValueType(value.getClass());
                end.setKeyType(end.getNode().getTypedValue().getClass());
                return value.getClass();
            }
            else {
                throw new EvaluationException("Not enough type information available to " +
                    "evaluate expression. Expression: '" + expression + "'. Type information ran " +
                    "out at node '" + end.getNode().getStringValue() + "', which represents a Map " +
                    "entry. Please ensure that either the getter for the Map contains appropriate " +
                    "generic type information or that it contains a value with the key type " +
                    end.getNode().getTypedValue().getClass().getName() + " and value " +
                    end.getNode().getStringValue());
            }
        }
        else if (value instanceof List) {
            List list = (List) value;
            if (end.getNode().getTypedValue() instanceof Integer) {
                Integer index = (Integer) end.getNode().getTypedValue();
                if (index < list.size()) {
                    value = list.get(index);
                    if (value != null) {
                        end.setType(NodeType.ListEntry);
                        end.setValueType(value.getClass());
                        end.setKeyType(Integer.class);
                        return value.getClass();
                    }
                }
            }

            throw new EvaluationException("Not enough type information available to " +
                "evaluate expression. Expression: '" + expression + "'. Type information ran " +
                "out at node '" + end.getNode().getStringValue() + "', which represents a List " +
                "entry. Please ensure that either the getter for the List contains appropriate " +
                "generic type information or that the index is numeric and a value exists at " +
                "the supplied index (" + end.getNode().getStringValue() + ").");
        }
        else {
            Type type = getBeanPropertyType(value.getClass(), end.getNode().getStringValue());
            if (type != null) {
                end.setType(NodeType.BeanProperty);
                end.setValueType(type);
                return type;
            }
            else {
                throw new NoSuchPropertyException("Bean class " + previous.getClass().getName() +
                    " does not contain a property called '" + end.getNode().getStringValue() +
                    "'. As a result the following expression could not be evaluated: " +
                    this.expression);
            }
        }
    }

    /**
     * Attempts to convert the {@link Type} object into a Class object. Currently will extract the
     * raw type from a {@link ParameterizedType} and the appropriate bound from a
     * {@link WildcardType}. If the result after these operations is a Class object it will
     * be cast and returned. Otherwise will return null.
     *
     * @param type the Type object to try and render as a Class
     * @return the Class if one can be determined, otherwise null
     */
    protected Class<?> convertToClass(Type type, NodeEvaluation evaluation) {
        // First extract any candidate type from Wildcards and Parameterized types
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getRawType();
        }

        while (type instanceof WildcardType || type instanceof TypeVariable) {
            if (type instanceof WildcardType) {
                type = getWildcardTypeBound((WildcardType) type);
            }
            else if (type instanceof TypeVariable) {
                type = getTypeVariableValue(evaluation, (TypeVariable<?>) type);
            }
        }

        // And now that we should have a single type, try and get a Class
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        else {
            return null;
        }
    }

    /**
     * Scans backwards in the expression for the last node which contained a JavaBean type
     * and attempts to use the type arguments to that class to find a match for the
     * TypeParameter provided.  On it's way also collects information from any parameterized
     * types and their super-types.
     *
     * @param evaluation the current NodeEvaluation
     * @param typeVar the TypeVariable to try and find a more concrete type for
     * @return the actual type argument for the type variable if possible, or null
     */
    protected Type getTypeVariableValue(NodeEvaluation evaluation, TypeVariable<?> typeVar) {
        // Type maps from TypeVariables to the corresponding Type.  The first map contains entries
        // from parameterized types (and their super-types) discovered while going back up the
        // nodes.  The second map contains information gathered by going up the superclasses
        // from the last concrete Class in the expression
        List<HashMap<TypeVariable<?>, Type>> typemap1 = new ArrayList<HashMap<TypeVariable<?>, Type>>();
        List<HashMap<TypeVariable<?>, Type>> typemap2 = new ArrayList<HashMap<TypeVariable<?>, Type>>();

        // Scan the evaluation chain for the first class or any parameterized types.
        Class<?> lastBean = this.bean.getClass();
        for (NodeEvaluation n = evaluation.getPrevious(); n != null; n = n.getPrevious()) {
            Type type = n.getValueType();

            // Bean class found?  Stop searching.
            if (type instanceof Class) {
                lastBean = (Class<?>) n.getValueType();
                break;
            }
            // Parameterized type?  Add to the typemap along with parent parameterized types
            else if (type instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) type;

                while (ptype != null) {
                    addTypeMappings(typemap1, ptype);

                    // Now find the parent of the ptype and see if it's a ptype too!
                    Type rawtype = ptype.getRawType();
                    if (rawtype instanceof Class) {
                        Class<?> superclass = (Class<?>) rawtype;
                        Type supertype = superclass.getGenericSuperclass();
                        ptype = (supertype instanceof ParameterizedType) ?  (ParameterizedType) supertype : null;
                    }
                }
            }
        }

        // Add the bean class and all its superclasses to the typemap.
        for (Class<?> c = lastBean; c != null; c = c.getSuperclass()) {
            Type t = c.getGenericSuperclass();
            if (t instanceof ParameterizedType) {
                addTypeMappings(typemap2, (ParameterizedType) t);
            }
        }

        Class<?> declaration = (Class<?>) typeVar.getGenericDeclaration();
        Type type = null;

        // If the type variable doesn't come from a direct superclass of the
        // the last bean, check the mappings from parameterized types first
        if (!declaration.isAssignableFrom(lastBean)) {
            for (int i = typemap1.size() - 1; i >= 0; i--) {
                // Map the type variable to a type.
                if ((type = typemap1.get(i).get(typeVar)) != null) {
                    // Reached a real class?  Done.
                    if (type instanceof Class) { return type; }
                    else if (type instanceof TypeVariable) {
                        typeVar = (TypeVariable<?>) type;
                    }
                }
            }
        }

        // If we did the above traverse and still ended up at another type
        // variable, check the last bean (and parents') mappings
        for (int i = typemap2.size() - 1; i >= 0; i--) {
            // Map the type variable to a type.
            if ((type = typemap2.get(i).get(typeVar)) != null) {
                // Reached a real class?  Done.
                if (type instanceof Class) { return type; }
                else if (type instanceof TypeVariable) {
                    typeVar = (TypeVariable<?>) type;
                }
            }
        }

        return type;
    }

   /**
    * Build a map of TypeVariables to Types.  We have to traverse the class hierarchy
    * from subclass to superclass, but the mapping of TypeVariables to Types has to start
    * from the place were the type variable was originally defined (superclass) and map
    * to the place where the type is bound to an actual class (subclass).  We therefore
    * need to build up the mappings sub to super and then traverse super to sub.
    *
    * @param paramType parameterized type to add to the map.
    */
    private void addTypeMappings(List<HashMap<TypeVariable<?>, Type>> typemap, ParameterizedType paramType) {
        Type rawType = paramType.getRawType();
        if (rawType instanceof Class) {
            Class<?> rawClass = (Class<?>) rawType;
            TypeVariable<?>[] vars = rawClass.getTypeParameters();
            Type[] args = paramType.getActualTypeArguments();
            HashMap<TypeVariable<?>, Type> entry =
              new HashMap<TypeVariable<?>, Type>(vars.length);
            for (int i = 0;  i < vars.length && i < args.length; ++i) {
                entry.put(vars[i], args[i]);
            }
            typemap.add(entry);
        }
    }

    /**
     * Gets the preferred bound from the WildcardType provided. In the case of
     * '? super SomeClass' then 'SomeClass' will be returned. In the case of
     * '? extends AnotherClass' then 'AnotherClass' will be returned.
     *
     * @param wtype the WildcardType to fetch the bounds of
     * @return the appropriate bound type
     */
    protected Type getWildcardTypeBound(WildcardType wtype) {
        Type[] bounds = wtype.getLowerBounds();
        if (bounds.length == 0) {
            bounds = wtype.getUpperBounds();
        }

        if (bounds.length > 0) {
            return bounds[0];
        }

        return null;
    }

    /**
     * Fetches the type of value that can be get/set with this expression evaluation. This is
     * equivalent (though more efficient) to calling getValue().getClass().  If the type information
     * on this expression is not complete then null will be returned.
     *
     * @return the Class of object that can be set/get with this evaluation or null
     */
    public Class<?> getType() {
        return convertToClass(this.leaf.getValueType(), this.leaf);
    }

    /**
     * Returns a scalar type appropriate to the expression evaluation. When {@link #getType()}
     * returns a scalar type then this method will return the identical class. However, when
     * getType() returns an Array, a Collection or a Map this method will attempt to determine
     * the type of element stored in that Array/Collection/Map and return that Class.  If
     * getType() returns null due to insufficient type information this method will also
     * return null.  Similarly if the type of item in the Array/Collection/May cannot be determined
     * then String.class will be returned.
     *
     * @return The scalar type to which values should be converted in order to either be set
     *         using this expression or set into the Array/Collection/Map should this expression
     *         point at a non scalar property
     */
    public Class<?> getScalarType() {
        Type type = this.leaf.getValueType();
        Class<?> clazz = convertToClass(type, this.leaf);

        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        else if (Collection.class.isAssignableFrom(clazz)) {
            if (type instanceof ParameterizedType) {
                return convertToClass(((ParameterizedType) type).getActualTypeArguments()[0], this.leaf);
            }
            else {
                return String.class;
            }
        }
        else if (Map.class.isAssignableFrom(clazz)) {
            if (type instanceof ParameterizedType) {
                return convertToClass(((ParameterizedType) type).getActualTypeArguments()[1], this.leaf);
            }
            else {
                return String.class;
            }
        }
        else {
            return clazz;
        }
    }

    /**
     * <p>Fetches the value of this expression evaluated against the bean. This is equivalent
     * to calling the appropriate get() methods in sequence in order to fetch the property
     * indicated by the expression.</p>
     *
     * <p>If the property or any intermediate property in the expression is null this method
     * will return null and will not alter the state of the object graph.</p>
     *
     * @return the value stored on the bean for this expression/property
     */
    @SuppressWarnings("unchecked")
	public Object getValue() {
        Object nodeValue = this.bean;
        for (NodeEvaluation node = this.root; node != null && nodeValue != null; node = node.getNext()) {
            nodeValue = node.getType().getPropertyAccessor().getValue(node, nodeValue);
        }

        return nodeValue;
    }

    /**
     * <p>Sets the value of the expression evaluated against the bean. This is loosely equivalent
     * to calling the appropriate getter() on intermediate properties and then calling the
     * appropriate setter on the final sub-property.</p>
     *
     * <p>During set operations null intermediate nodes will be instantiated and linked into the
     * object graph in order to persistently set the desired property. When this is not possible
     * (e.g. because of a lack of default constructors) an exception will be thrown.</p>
     *
     * @param propertyValue the value to be set for the property of the bean
     * @throws EvaluationException if intermediate null properties cannot be instantiated
     */
    @SuppressWarnings("unchecked")
	public void setValue(Object propertyValue) throws EvaluationException {
        Object nodeValue = this.bean;
        for (NodeEvaluation node = this.root; node != this.leaf && nodeValue != null; node = node.getNext()) {
            PropertyAccessor accessor = node.getType().getPropertyAccessor();
            Object previous = nodeValue;
            nodeValue = accessor.getValue(node, previous);

            if (nodeValue == null) {
                nodeValue = getDefaultValue(node);
                node.getType().getPropertyAccessor().setValue(node, previous, nodeValue);
            }
        }

        this.leaf.getType().getPropertyAccessor().setValue(this.leaf, nodeValue, propertyValue);
    }

    /**
     * <p>Attempts to create a default value for a given node by either a) creating a new array
     * instance for arrays, b) fetching the first enum for enum classes, c) creating a default
     * instance for interfaces and abstract classes using ReflectUtil or d) calling a default
     * constructor.
     *
     * @param node the node for which to find a default value
     * @return an instance of the appropriate type
     * @throws EvaluationException if an instance cannot be created
     */
    @SuppressWarnings("unchecked")
	private Object getDefaultValue(NodeEvaluation node) throws EvaluationException {
        try {
            Class clazz = convertToClass(node.getValueType(), node);

            if (clazz.isArray()) {
                return Array.newInstance(clazz.getComponentType(), 0);
            }
            else if (clazz.isEnum()) {
                return clazz.getEnumConstants()[0];
            }
            else {
                return StripesFilter.getConfiguration().getObjectFactory().newInstance(clazz);
            }
        }
        catch (Exception e) {
            throw new EvaluationException("Encountered an exception while trying to create " +
            " a default instance for property '" + node.getNode().getStringValue() + "' in " +
            "expression '" + this.expression.getSource() + "'.", e);
        }
    }

    /**
     * <p>Sets the value of this expression to "null" for the bean.  In reality this is not always
     * null, but the logical interpretation of "null" for a given type. For primitives the
     * value is set to be the default value for the primitive type as used by the JVM when
     * initializing instance variables.  For Map entries the key is removed from the Map instead
     * of leaving the key present with a null value.</p>
     *
     * <p>If any intermediate properties in the expression are null this method will return
     * immediately. The sole purpose of this method is to blank out a value <i>if one is present</i>.
     * Therefore if no value is present, nothing will be changed.</p>
     *
     * @throws EvaluationException if any exceptions are thrown during the process of nulling out
     */
    @SuppressWarnings("unchecked")
	public void setToNull() throws EvaluationException {
        Object nodeValue = this.bean;
        for (NodeEvaluation node = this.root; node != this.leaf && nodeValue != null; node = node.getNext()) {
            nodeValue = node.getType().getPropertyAccessor().getValue(node, nodeValue);
        }

        if (nodeValue != null) {
            Class leafType = convertToClass(this.leaf.getValueType(), this.leaf);
            if (Map.class.isAssignableFrom(leafType) || Collection.class.isAssignableFrom(leafType)) {
                nodeValue = this.leaf.getType().getPropertyAccessor().getValue(this.leaf, nodeValue);

                if (nodeValue != null && Map.class.isAssignableFrom(leafType)) {
                    ((Map) nodeValue).clear();
                }
                else if (nodeValue != null && Collection.class.isAssignableFrom(leafType)) {
                    ((Collection) nodeValue).clear();
                }
            }
            else {
                try {
                    Object nvl = ReflectUtil.getDefaultValue(leafType);
                    this.leaf.getType().getPropertyAccessor().setValue(this.leaf, nodeValue, nvl);
                }
                catch (RuntimeException re) { throw re; }
                catch (Exception e) {
                    throw new EvaluationException("Could not set a null value for property '" +
                    this.expression + "' on bean of type " + this.bean.getClass().getName(), e);
                }
            }
        }
    }
}
