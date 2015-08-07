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

import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple Datastore used in this example application.
 */
public class Datastore {

    public static final Map< Long, Artist> ARTISTS = new HashMap<Long, Artist>();
    public static final Map< Long, Song> SONGS = new HashMap<Long, Song>();

    /**
     * Populate the Datastore with some initial artists and songs.
     */
    static {
        Artist gordonLightfoot = new Artist(1L, "Gordon Lightfoot");
        gordonLightfoot.getSongs().add(new Song(1L, "If You Could Read My Mind", 4));
        gordonLightfoot.getSongs().add(new Song(2L, "Sundown", 3));
        Artist beatles = new Artist(2L, "The Beatles");
        beatles.getSongs().add(new Song(3L, "If", 3));
        beatles.getSongs().add(new Song(4L, "Yellow Submarine", 2));
        Artist pinkFloyd = new Artist(3L, "Pink Floyd");
        pinkFloyd.getSongs().add(new Song(5L, "Comfortably Numb", 3));
        pinkFloyd.getSongs().add(new Song(6L, "High Hopes", 5));

        ARTISTS.put(gordonLightfoot.getId(), gordonLightfoot);
        ARTISTS.put(beatles.getId(), beatles);
        ARTISTS.put(pinkFloyd.getId(), pinkFloyd);

        for (Artist artist : ARTISTS.values()) {
            for (Song song : artist.getSongs()) {
                SONGS.put(song.getId(), song);
            }
        }
    }

    /**
     * Returns an artist by name.
     *
     * @param artistName
     * @return Artist or null if not found
     */
    public static Artist getArtistByName(String artistName) {
        for (Artist artist : ARTISTS.values()) {
            if (dasherize(artistName).equalsIgnoreCase(artist.getName())) {
                return artist;
            }
        }

        return null;
    }

    /**
     * Returns an artist by ID.
     *
     * @param id
     * @return Artist or null if not found
     */
    public static Artist getArtistById(Long id) {
        return ARTISTS.get(id);
    }

    /**
     * Dasherizes a string name.
     *
     * @param str - String to dasherize
     * @return Dasherized string
     */
    private static String dasherize(String str) {
        return str == null ? null : str.replaceAll("([a-z])([A-Z])", "$1-$2").replaceAll("_", "-").toLowerCase();
    }

    /**
     * Creates a new song, puts it in the datastore, and associates it with the
     * passed artist.
     *
     * @param artist
     * @param song
     * @return Newly created song with a populated ID
     */
    public static Song createSong(Artist artist, Song song) {
        song.setId(getNextSongId());
        SONGS.put(song.getId(), song);
        artist.getSongs().add(song);
        return song;
    }

    /**
     * Creates a new artist in the Datastore.
     *
     * @param name
     * @return Newly created artist object with a populated ID.
     */
    public static Artist createArtist(String name) {
        Artist artist = new Artist(getNextArtistId(), name);
        ARTISTS.put(artist.getId(), artist);
        return artist;
    }

    /**
     * Returns the next highest Artist ID in the datastore.
     *
     * @return next highest Artist ID in the datastore.
     */
    public static Long getNextArtistId() {
        long highestArtistId = 0L;

        for (Artist artist : ARTISTS.values()) {
            if (artist.getId() > highestArtistId) {
                highestArtistId = artist.getId();
            }
        }

        return highestArtistId + 1L;
    }

    /**
     * Returns the next highest Song ID in the datastore.
     *
     * @return next highest Song ID in the datastore.
     */
    public static Long getNextSongId() {
        long highestSongId = 0L;

        for (Song song : SONGS.values()) {
            if (song.getId() > highestSongId) {
                highestSongId = song.getId();
            }
        }

        return highestSongId + 1L;
    }
}
