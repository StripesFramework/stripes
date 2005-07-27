package net.sourceforge.stripes.examples.ex1;

import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 25, 2005 Time: 8:17:04 AM To change this
 * template use File | Settings | File Templates.
 */
public class CatController {
    private static Map<String,Cat> cats = new TreeMap<String,Cat>();

    static {
        Cat kitty = new Cat();
        kitty.setName("Grey Kitty");
        kitty.setColor("Grey");
        kitty.setAge(1);
        kitty.setBreed(Breed.RUSSIAN_BLUE);
        cats.put(kitty.getName(), kitty);

        kitty = new Cat();
        kitty.setName("Black Kitty");
        kitty.setColor("Black");
        kitty.setAge(1);
        kitty.setBreed(Breed.MIX);
        cats.put(kitty.getName(), kitty);

        kitty = new Cat();
        kitty.setName("Linux");
        kitty.setColor("Black");
        kitty.setAge(6);
        kitty.setBreed(Breed.DSH);
        cats.put(kitty.getName(), kitty);
    }

    public Collection<Cat> getCats() {
        return this.cats.values();
    }

    public Cat getCat(String name) {
        return this.cats.get(name);
    }

    public void updateCat(Cat cat) {
        cats.put(cat.getName(), cat);
    }

    public void addCat(Cat cat) {
        cats.put(cat.getName(), cat);
    }

    public void deleteCat(Cat cat) {
        cats.remove(cat.getName());
    }
}
