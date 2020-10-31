package org.jellyfin.mobile.media

import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

/**
 * List of different views when browsing media
 * Libraries is the initial view
 */
enum class LibraryContentType(val flags: Int) {
    /**
     * List of music libraries that the user can access (referred to as "user views" in Jellyfin)
     */
    Libraries(FLAG_BROWSABLE),

    /**
     * A single music library
     */
    Library(FLAG_BROWSABLE),

    /**
     * A list of recently added tracks
     */
    LibraryLatest(FLAG_PLAYABLE),

    /**
     * A list of albums
     */
    LibraryAlbums(FLAG_BROWSABLE),

    /**
     * A list of artists
     */
    LibraryArtists(FLAG_BROWSABLE),

    /**
     * A list of playlists
     */
    LibraryPlaylists(FLAG_BROWSABLE),

    /**
     * A list of genres
     */
    LibraryGenres(FLAG_BROWSABLE),

    // Content types for individual library items - albums, artists, playlists and a shuffle meta item

    Album(FLAG_BROWSABLE or FLAG_PLAYABLE),
    Artist(FLAG_BROWSABLE or FLAG_PLAYABLE),
    Playlist(FLAG_BROWSABLE or FLAG_PLAYABLE),
    Shuffle(FLAG_PLAYABLE),
    Recent(FLAG_PLAYABLE),
}
