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
 *
 * @author Rick_Grashel
 */
@Public
@RestActionBean
@UrlBinding("/rockandroll/songs")
public class SongRestActionBean implements ActionBean {

    @Validate(required = true, on = "post")
    private Artist artist;

    @Validate(required = true, on = "post")
    private String songTitle;

    public Resolution post() {
        Song song = new Song(null, songTitle, 0);
        song = Datastore.createSong(artist, song);
        return new JsonResolution(song);
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Artist getArtist() {
        return this.artist;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongTitle() {
        return this.songTitle;
    }

    public ActionBeanContext getContext() {
        return this.context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }
    private ActionBeanContext context;
}
