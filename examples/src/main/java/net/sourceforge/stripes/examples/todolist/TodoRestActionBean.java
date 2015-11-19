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

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.JsonResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.RestActionBean;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

/**
 * This Stripes REST service handles verbs related to Artists
 *
 * <code>GET</code> without parameters lists all artists in the datastore ---
 * <code>GET</code> with an ID will return the corresponding todo in the
 * datastore. If no todo is found, then a 404 will be returned. ---
 * <code>POST</code> with an description passed as a parameter will create a new
 * Todo and put it in the Datastore.
 */
@Public
@RestActionBean
@UrlBinding("/todolist/todos/{todo}")
public class TodoRestActionBean implements ActionBean {

    @Validate(required = true, on = {"put"})
    @ValidateNestedProperties(
            @Validate)
    private Todo todo;

    /**
     * This Stripes REST service handles GET verbs related to Todos
     *
     * <code>GET</code> without parameters lists all todos in the datastore ---
     * <code>GET</code> with an ID will return the corresponding todo in the
     * datastore. If no todo is found, then an error will be returned.
     *
     * @return Single todo or list of todos.
     */
    public Resolution get() {
        if (todo != null && todo.getId() != null) {
            return new JsonResolution(todo);
        } else {
            return new JsonResolution(Datastore.getAll());
        }
    }

    /**
     * This Stripes REST service handles verbs related to Todos
     *
     * <code>POST</code> with a description and priority passed as a parameter
     * will create a new Todo and put it in the Datastore.
     *
     * @return Newly created todo with a populated ID.
     */
    public Resolution post() {
        if (todo == null || todo.getId() == null) {
            return new JsonResolution(Datastore.insert(todo));
        } else {
            return put();
        }
    }

    /**
     * This Stripes REST service handles verbs related to Todos
     *
     * <code>PUT</code> with a description and priority passed as a parameter
     * will update an existing Todo in the datastore.
     *
     * @return Newly update todo with a populated ID.
     */
    public Resolution put() {
        return new JsonResolution(Datastore.update(todo));
    }

    /**
     * This Stripes REST service handles verbs related to Todos
     *
     * <code>DELETE</code> with an ID passed as a parameter will delete a new
     * todo from the datastore
     *
     * @return Newly created todo with a populated ID.
     */
    public Resolution delete() {
        return new JsonResolution(Datastore.delete(todo));
    }

    public void setTodo(Todo todo) {
        this.todo = todo;
    }

    public Todo getTodo() {
        return this.todo;
    }

    @Override
    public ActionBeanContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
    private ActionBeanContext context;
}
