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
package net.sourceforge.stripes.tag;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.localization.LocalizationUtility;
import net.sourceforge.stripes.util.bean.BeanUtil;
import net.sourceforge.stripes.util.bean.ExpressionException;
import net.sourceforge.stripes.util.bean.BeanComparator;
import net.sourceforge.stripes.util.StringUtil;
import net.sourceforge.stripes.util.CollectionUtil;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

/**
 * <p>
 * Writes a set of {@literal <option value="foo">bar</option>} tags to the page
 * based on the contents of a Collection. Each element in the collection is
 * represented by a single option tag on the page. Uses the label and value
 * attributes on the tag to name the properties of the objects in the Collection
 * that should be used to generate the body of the HTML option tag and the value
 * attribute of the HTML option tag respectively.</p>
 *
 * <p>
 * E.g. a tag declaration that looks like:</p>
 * <pre>{@literal <stripes:options-collection collection="${cats} value="catId" label="name"/>}</pre>
 *
 * <p>
 * would cause the container to look for a Collection called "cats" across the
 * various JSP scopes and set it on the tag. The tag would then proceed to
 * iterate through that collection calling getCatId() and getName() on each cat
 * to produce HTML option tags.</p>
 *
 * <p>
 * By default, the tag will attempt to localize the labels attributes of the
 * option tags that are generated. To override this default and turn off this
 * behavior, thus saving unnecessary resource bundle lookups, set the
 * localizeLabels attribute to false.</p>
 *
 * <p>
 * To do label localization, the tag will look up labels in the field resource
 * bundle using:</p>
 *
 * <ul>
 * <li>{className}.{labelPropertyValue}</li>
 * <li>{packageName}.{className}.{labelPropertyValue}</li>
 * <li>{className}.{valuePropertyValue}</li>
 * <li>{packageName}.{className}.{valuePropertyValue}</li>
 * </ul>
 *
 * <p>
 * For example for a class com.myco.Gender supplied to the options-collection
 * tag with label="description" and value="key", when rendering for an instance
 * Gender[key="M", description="Male"] the following localized properties will
 * be looked for:
 *
 * <ul>
 * <li>Gender.Male</li>
 * <li>com.myco.Gender.Male</li>
 * <li>Gender.M</li>
 * <li>com.myco.Gender.M</li>
 * </ul>
 *
 * <p>
 * If no localized label can be found, or if the localizeLabels attribute is set
 * to false, then the value of the label property will be used.</p>
 *
 * <p>
 * Optionally, the group attribute may be used to generate &lt;optgroup&gt;
 * tags. The value of this attribute is used to retrieve the corresponding
 * property on each object of the collection. A new optgroup will be created
 * each time the value changes.
 * </p>
 *
 * <p>
 * The rendered group may be localized by specifying one of the following
 * properties:</p>
 *
 * <ul>
 * <li>{className}.{groupPropertyValue}</li>
 * <li>{packageName}.{className}.{groupPropertyValue}</li>
 * </ul>
 *
 * <p>
 * All other attributes on the tag (other than collection, value, label and
 * group) are passed directly through to the InputOptionTag which is used to
 * generate the individual HTML options tags. As a result the
 * InputOptionsCollectionTag will exhibit the same re-population/selection
 * behaviour as the regular options tag.</p>
 *
 * <p>
 * Since the tag has no use for one it does not allow a body.</p>
 *
 * @author Tim Fennell
 */
public class InputOptionsCollectionTag extends HtmlTagSupport {

    /**
     * A helper for writing HTML &lt;optgroup&gt; tags.
     */
    private final HtmlTagSupport optgroupSupport = new HtmlTagSupport() {
        @Override
        public int doStartTag() throws JspException {
            return 0;
        }

        @Override
        public int doEndTag() throws JspException {
            return 0;
        }
    };

    private Collection<? extends Object> collection;
    private String value;
    private String label;
    private String sort;
    private String group;
    private Boolean localizeLabels;

    /**
     * A little container class that holds an entry in the collection of items
     * being used to generate the options, along with the determined label and
     * value (either from a property, or a localized value).
     */
    public static class Entry {

        /**
         *
         */
        public Object bean,

        /**
         *
         */
        label,

        /**
         *
         */
        value,

        /**
         *
         */
        group;

        Entry(Object bean, Object label, Object value, Object group) {
            this.bean = bean;
            this.label = label;
            this.value = value;
            this.group = group;
        }
    }

    /**
     * Internal list of entries that is assembled from the items in the
     * collection.
     */
    private List<Entry> entries = new LinkedList<Entry>();

    /**
     * <p>
     * Sets the collection that will be used to generate options. In this case
     * the term collection is used in the loosest possible sense - it means
     * either a bonafide instance of {@link java.util.Collection}, or an
     * implementation of {@link Iterable} other than a Collection, or an array
     * of Objects or primitives.</p>
     *
     * <p>
     * In the case of any input which is not an {@link java.util.Collection} it
     * is converted to a Collection before storing it.</p>
     *
     * @param in either a Collection, an Iterable or an Array
     */
    @SuppressWarnings("unchecked")
    public void setCollection(Object in) {
        if (in == null) {
            this.collection = null;
        } else if (in instanceof Collection) {
            this.collection = (Collection) in;
        } else if (in instanceof Iterable) {
            this.collection = CollectionUtil.asList((Iterable) in);
        } else if (in.getClass().isArray()) {
            this.collection = CollectionUtil.asList(in);
        } else {
            throw new IllegalArgumentException("A 'collection' was supplied that is not of a supported type: " + in.getClass());
        }
    }

    /**
     * Returns the value set by {@link #setCollection(Object)}. In the case that
     * a {@link java.util.Collection} was supplied, the same collection will be
     * returned. In all other cases a new collection created to hold the
     * supplied elements will be returned.
     * @return 
     */
    public Object getCollection() {
        return this.collection;
    }

    /**
     * Sets the name of the property that will be fetched on each bean in the
     * collection in order to generate the value attribute of each option.
     *
     * @param value the name of the attribute
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the property name set with setValue().
     * @return 
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the name of the property that will be fetched on each bean in the
     * collection in order to generate the body of each option (i.e. what is
     * seen by the user).
     *
     * @param label the name of the attribute
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the property name set with setLabel().
     * @return 
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets a comma separated list of properties by which the beans in the
     * collection will be sorted prior to rendering them as options. 'label' and
     * 'value' are special case properties that are used to indicate the
     * generated label and value of the option.
     *
     * @param sort the name of the attribute(s) used to sort the collection of
     * options
     */
    public void setSort(String sort) {
        this.sort = sort;
    }

    /**
     * Gets the comma separated list of properties by which the collection is
     * sorted.
     * @return 
     */
    public String getSort() {
        return sort;
    }

    /**
     * Sets the flag that indicates whether or not attempts to localize labels
     * should be made.
     * @param localizeLabels
     */
    public void setLocalizeLabels(Boolean localizeLabels) {
        this.localizeLabels = localizeLabels;
    }

    /**
     * Gets the flag that indicates whether or not attempts to localize labels
     * should be made.
     * @return 
     */
    public Boolean getLocalizeLabels() {
        return localizeLabels;
    }

    /**
     *
     * @return
     */
    protected boolean isAttemptToLocalizeLabels() {
        return (localizeLabels == null) || (localizeLabels != null && localizeLabels.booleanValue());
    }

    /**
     * Adds an entry to the internal list of items being used to generate
     * options.
     *
     * @param item the object represented by the option
     * @param label the actual label for the option
     * @param value the actual value for the option
     */
    protected void addEntry(Object item, Object label, Object value) {
        this.entries.add(new Entry(item, label, value, null));
    }

    /**
     * Adds an entry to the internal list of items being used to generate
     * options.
     *
     * @param item the object represented by the option
     * @param label the actual label for the option
     * @param value the actual value for the option
     * @param group the value to be used for optgroups
     */
    protected void addEntry(Object item, Object label, Object value, Object group) {
        this.entries.add(new Entry(item, label, value, group));
    }

    /**
     * Iterates through the collection and generates the list of Entry objects
     * that can then be sorted and rendered into options. It is assumed that
     * each element in the collection has non-null values for the properties
     * specified for generating the label and value.
     *
     * @return SKIP_BODY in all cases
     * @throws JspException if either the label or value attributes specify
     * properties that are not present on the beans in the collection
     */
    @Override
    public int doStartTag() throws JspException {
        if (this.collection == null) {
            return SKIP_BODY;
        }

        String labelProperty = getLabel();
        String valueProperty = getValue();
        String groupProperty = getGroup();

        try {
            Locale locale = getPageContext().getRequest().getLocale();
            boolean attemptToLocalizeLabels = isAttemptToLocalizeLabels();

            for (Object item : this.collection) {
                Class<? extends Object> clazz = item.getClass();

                // Lookup the bean properties for the label, value and group
                Object label = (labelProperty == null) ? item : BeanUtil.getPropertyValue(labelProperty, item);
                Object value = (valueProperty == null) ? item : BeanUtil.getPropertyValue(valueProperty, item);
                Object group = (groupProperty == null) ? null : BeanUtil.getPropertyValue(groupProperty, item);

                if (attemptToLocalizeLabels) {
                    // Try to localize the label
                    String packageName = clazz.getPackage() == null ? "" : clazz.getPackage().getName();
                    String simpleName = LocalizationUtility.getSimpleName(clazz);
                    String localizedLabel = null;
                    if (label != null) {
                        localizedLabel = LocalizationUtility.getLocalizedFieldName(simpleName + "." + label, packageName, null, locale);
                    }
                    if (localizedLabel == null && value != null) {
                        localizedLabel = LocalizationUtility.getLocalizedFieldName(simpleName + "." + value, packageName, null, locale);
                    }
                    if (localizedLabel != null) {
                        label = localizedLabel;
                    }

                    // Try to localize the group
                    if (group != null) {
                        String localizedGroup = LocalizationUtility.getLocalizedFieldName(
                                simpleName + "." + group, packageName, null, locale);
                        if (localizedGroup != null) {
                            group = localizedGroup;
                        }
                    }
                }
                addEntry(item, label, value, group);
            }
        } catch (ExpressionException ee) {
            throw new StripesJspException("A problem occurred generating an options-collection. "
                    + "Most likely either [" + labelProperty + "] or [" + valueProperty + "] is not a "
                    + "valid property of the beans in the collection: " + this.collection, ee);
        }

        return SKIP_BODY;
    }

    /**
     * Optionally sorts the assembled entries and then renders them into a
     * series of option tags using an instance of InputOptionTag to do the
     * rendering work.
     *
     * @return EVAL_PAGE in all cases.
     * @throws jakarta.servlet.jsp.JspException
     */
    @Override
    public int doEndTag() throws JspException {
        // Determine if we're going to be sorting the collection
        List<Entry> sortedEntries = new LinkedList<Entry>(this.entries);
        if (this.sort != null) {
            String[] props = StringUtil.standardSplit(this.sort);
            for (int i = 0; i < props.length; ++i) {
                if (!props[i].equals("label") && !props[i].equals("value")) {
                    props[i] = "bean." + props[i];
                }
            }

            Collections.sort(sortedEntries,
                    new BeanComparator(getPageContext().getRequest().getLocale(), props));
        }

        InputOptionTag tag = new InputOptionTag();
        tag.setParent(this);
        tag.setPageContext(getPageContext());

        Object lastGroup = null;

        JspWriter out = getPageContext().getOut();
        for (Entry entry : sortedEntries) {
            // Set properties common to all options
            tag.getAttributes().putAll(getAttributes());

            // Set properties for this tag
            tag.setLabel(entry.label == null ? null : entry.label.toString());
            tag.setValue(entry.value);
            try {
                if (entry.group != null && !entry.group.equals(lastGroup)) {
                    if (lastGroup != null) {
                        optgroupSupport.writeCloseTag(out, "optgroup");
                    }

                    optgroupSupport.set("label", String.valueOf(entry.group));
                    optgroupSupport.writeOpenTag(out, "optgroup");

                    lastGroup = entry.group;
                }

                tag.doStartTag();
                tag.doInitBody();
                tag.doAfterBody();
                tag.doEndTag();
            } catch (Throwable t) {
                /**
                 * Catch whatever comes back out of the doCatch() method and
                 * deal with it
                 */
                try {
                    tag.doCatch(t);
                } catch (Throwable t2) {
                    if (t2 instanceof JspException) {
                        throw (JspException) t2;
                    }
                    if (t2 instanceof RuntimeException) {
                        throw (RuntimeException) t2;
                    } else {
                        throw new StripesJspException(t2);
                    }
                }
            } finally {
                tag.doFinally();
            }
        }

        if (lastGroup != null) {
            optgroupSupport.writeCloseTag(out, "optgroup");
        }

        // Clean up any temporary state
        this.entries.clear();

        return EVAL_PAGE;
    }

    /**
     * Sets the name of the property that will be fetched on each bean in the
     * collection in order to generate optgroups. A new optgroup will be created
     * each time the value changes.
     *
     * @param group the name of the group attribute
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Gets the property name set with setGroup().
     * @return 
     */
    public String getGroup() {
        return group;
    }
}
