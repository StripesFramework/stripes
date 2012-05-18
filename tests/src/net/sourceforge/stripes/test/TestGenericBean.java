package net.sourceforge.stripes.test;

/**
 * A JavaBean that is a generic type. Used to test some of the more gnarly aspects
 * of binding into beans with lots of generics going on. This is exercised in
 * {@link net.sourceforge.stripes.controller.GenericsBindingTests#testGenericBean()}.
 *
 * The specifics of what is being tested is described in STS-427, which is now fixed.
 *
 * @author Alan Burlison
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

