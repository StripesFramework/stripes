package net.sourceforge.stripes.controller;

import java.util.List;
import java.util.Map;

/**
 * A simple base class that is littered with Type parameters at the class level. Contains
 * no tests in and of itself, but it is necessary to be a public class in order for
 * {@link GenericsBindingTests} to extend it and have the methods be accessible.
 *
 * @author Tim Fennell 
 */
public class GenericsBindingTestsBaseClass<N,E,K,V> {
    N number;
    List<E> list;
    Map<K,V> map;

    public N getNumber() { return number; }
    public void setNumber(N number) { this.number = number; }

    public List<E> getList() { return list; }
    public void setList(List<E> list) { this.list = list; }

    public Map<K, V> getMap() { return map; }
    public void setMap(Map<K, V> map) { this.map = map; }
}
