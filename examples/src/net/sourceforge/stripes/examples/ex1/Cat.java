package net.sourceforge.stripes.examples.ex1;

import java.util.Date;

/**
 * Created by IntelliJ IDEA. User: tfenne Date: Jun 25, 2005 Time: 8:15:49 AM To change this
 * template use File | Settings | File Templates.
 */
public class Cat {
    private String name;
    private String color;
    private int age;
    private Breed breed;
    private String[] favoriteFoods;
    private String[] activities;
    private Date birthday;

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String[] getFavoriteFoods() {
        return favoriteFoods;
    }

    public void setFavoriteFoods(String[] favoriteFoods) {
        this.favoriteFoods = favoriteFoods;
    }

    public String[] getActivities() {
        return activities;
    }

    public void setActivities(String[] activities) {
        this.activities = activities;
    }

    public Breed getBreed() {
        return breed;
    }

    public void setBreed(Breed breed) {
        this.breed = breed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
