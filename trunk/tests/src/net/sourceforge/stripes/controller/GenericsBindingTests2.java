package net.sourceforge.stripes.controller;

import org.testng.annotations.Test;

/**
 * Basically a mirror of GenericsBindingTests except that in this case the type variable/
 * parameter information is pushed further up the hierarchy by the fact that we just extend
 * GenericsBindingTests.  So this test ensures that our type inference using type variables
 * works when the information is not directly in this class, but further up the hierarchy.
 *
 * @author Tim Fennell
 */
public class GenericsBindingTests2 extends GenericsBindingTests {
    @Test(groups="fast")
    public void testSimpleTypeVariable() throws Exception {
        super.testSimpleTypeVariable();
    }

    @Test(groups="fast")
    public void testTypeVariableLists() throws Exception {
        super.testTypeVariableLists();
    }

    @Test(groups="fast")
    public void testTypeVariableMaps() throws Exception {
        super.testTypeVariableMaps();
    }
}
