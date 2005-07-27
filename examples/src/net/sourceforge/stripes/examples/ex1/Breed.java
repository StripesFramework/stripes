package net.sourceforge.stripes.examples.ex1;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jul 22, 2005 Time: 5:42:55 PM To change this
 * template use File | Settings | File Templates.
 */
public enum Breed {
    DSH("Domestic Short Hair"),
    RUSSIAN_BLUE("Russian Blue"),
    BRITISH_BLUE("British Blue"),
    TABBY("Tabby"),
    MIX("Mix");

    private String name;

    Breed(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
