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
package net.sourceforge.stripes.examples.rockandroll;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.JsonResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.RestActionBean;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.examples.bugzooky.ext.Public;
import net.sourceforge.stripes.validation.Validate;

/**
 * This Stripes REST service handles verbs related to Artists
 *
 * <code>GET</code> without parameters lists all artists in the datastore ---
 * <code>GET</code> with an ID or dasherized artist name will return the
 * corresponding artist in the datastore. If no artist is found, then a 404 will
 * be returned. --- <code>POST</code> with an artistName passed as a parameter
 * will create a new Artist and put it in the Datastore.
 */
@Public
@RestActionBean
@UrlBinding("/rockandroll/artists/{artist}")
public class ArtistRestActionBean implements ActionBean {

    @Validate(required = true, on = "post")
    private String artistName;

    private Artist artist;

    /**
     * This Stripes REST service handles GET verbs related to Artists
     *
     * <code>GET</code> without parameters lists all artists in the datastore
     * --- <code>GET</code> with an ID or dasherized artist name will return the
     * corresponding artist in the datastore. If no artist is found, then an error
     * will be returned.
     *
     * @return Single artist or list of artists.
     */
    public Resolution get() {
        if (artist != null) {
            return new JsonResolution(artist);
        } else {
            return new JsonResolution(Datastore.ARTISTS.values());
        }
    }

    /**
     * This Stripes REST service handles verbs related to Artists
     *
     * <code>POST</code> with an artistName passed as a parameter will create a
     * new Artist and put it in the Datastore.
     *
     * @return Newly created artist with a populated ID.
     */
    public Resolution post() {
        return new JsonResolution(Datastore.createArtist(getArtistName()));
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Artist getArtist() {
        return this.artist;
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
