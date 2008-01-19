package net.sourceforge.stripes.test;

import java.lang.reflect.Method;

/**
 * A JavaBean that is a generic type.
 *
 * @author Alan Burlison
 *
 * XXX If this class is not the top level in the inheritance hierarchy,
 * Stripes cannot bind its properties.  e.g. if instead of:
 *     public class TestGenericBean<A,B> { ... }
 * we have
 *     class Class1<A,B> { ... }
 *     class Class2<B,A> extends Class1<B,A> {}
 *     class Class3<X,Y> extends Class2<X,Y> {}
 *     public class TestGenericBean<A,B> extends Class3<A,B> {}
 * Stripes will fail to bind the properties of Class1 correctly.  See STS-427
 */
public class TestGenericBean {
    public static class Class1<A,B> {
        private A genericA;
        private B genericB;
        public A getGenericA() { return genericA; }
        public void setGenericA(A genericA) { this.genericA = genericA; }
        public B getGenericB() { return genericB; }
        public void setGenericB(B genericB) { this.genericB = genericB; }
    }

    public static class Class2<B,A> extends Class1<B,A> {}
    public static class Class3<A,B> extends Class2<A,B> {}

    public static class GenericBean<G,H> extends Class3<G,H> {}
}

