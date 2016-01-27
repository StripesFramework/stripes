/*
 * Copyright 2014 Rick Grashel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sourceforge.stripes.examples.todolist;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple Datastore used in this example application.
 */
public class Datastore {

    private static final Map< Long, Todo> OBJECT_STORE = new HashMap< Long, Todo >();

    /**
     * Populate the Datastore with some initial objects
     */
    static {
        insert(new Todo(1, "Buy more beer"));
        insert(new Todo(1, "Order pizza"));
        insert(new Todo(2, "Eat pie"));
        insert(new Todo(4, "Watch TV"));
        insert(new Todo(5, "Sleep"));
    }

    /**
     * Returns all objects in the data store
     *
     * @return All objects in datastore
     */
    public static Collection< Todo> getAll() {
        return OBJECT_STORE.values();
    }

    /**
     * Returns an object by ID.
     *
     * @param id
     * @return Todo or null if not found
     */
    public static Todo getById(Long id) {
        return OBJECT_STORE.get(id);
    }

    /**
     * Creates a new object in the Datastore.
     *
     * @param object
     * @return Newly created object with a populated ID.
     */
    public static Todo insert(Todo object) {
        object.setId(getNextAvailableId());
        OBJECT_STORE.put(object.getId(), object);
        return object;
    }

    /**
     * Creates a new object in the Datastore.
     *
     * @param object
     * @return Newly created object with a populated ID.
     */
    public static Todo update(Todo object) {
        OBJECT_STORE.put(object.getId(), object);
        return object;
    }

    /**
     * Deletes an object from the datastore by id
     *
     * @param id
     * @return Deleted object
     */
    public static Todo delete(long id) {
        return delete(getById(id));
    }

    /**
     * Deletes the passed object from the datastore.
     *
     * @param object
     * @return The object that was deleted
     */
    public static Todo delete(Todo object) {
        Todo deletedTodo = OBJECT_STORE.get(object.getId());
        OBJECT_STORE.remove(object.getId());
        return deletedTodo;
    }

    /**
     * Returns the next highest ID in the datastore.
     *
     * @return next highest ID in the datastore.
     */
    public static Long getNextAvailableId() {
        long highestId = 0L;

        for (Todo object : OBJECT_STORE.values()) {
            if (object.getId() > highestId) {
                highestId = object.getId();
            }
        }

        return highestId + 1L;
    }
}
