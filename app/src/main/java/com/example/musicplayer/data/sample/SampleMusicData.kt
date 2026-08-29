package com.example.musicplayer.data.sample

import com.example.musicplayer.domain.model.Album
import com.example.musicplayer.domain.model.Artist
import com.example.musicplayer.domain.model.MusicFolder
import com.example.musicplayer.domain.model.Playlist
import com.example.musicplayer.domain.model.Track

/**
 * Static sample data used until Phase 3+ wires in a real MediaStore /
 * Room-backed repository. Every screen in Phase 2 (Library, Albums,
 * Playlists, Folders) reads from this single source so the same six
 * sample tracks show up consistently everywhere, exactly like the
 * reference image.
 *
 * Nothing here is UI: no Compose imports. Accent colors are passed as
 * hex strings on the domain models themselves (Playlist.accentColorHex,
 * MusicFolder.accentColorHex), keeping this file free of any UI-layer
 * (Compose/theme) dependency.
 */
/** The well-known id of the built-in "Favorite Songs" playlist, kept dynamic via FavoritesRepository. */
const val FAVORITES_PLAYLIST_ID = 1L

object SampleMusicData {

    val tracks: List<Track> = listOf(
        Track(
            id = 1,
            title = "Dream It Possible",
            artist = "Delacey",
            album = "Dream It Possible",
            durationMs = 204_000,
            uri = "sample://track/1",
            albumId = 1
        ),
        Track(
            id = 2,
            title = "Believer",
            artist = "Imagine Dragons",
            album = "Evolve",
            durationMs = 201_000,
            uri = "sample://track/2",
            albumId = 2
        ),
        Track(
            id = 3,
            title = "Havana",
            artist = "Camila Cabello",
            album = "Camila",
            durationMs = 217_000,
            uri = "sample://track/3",
            albumId = 3
        ),
        Track(
            id = 4,
            title = "Cheap Thrills",
            artist = "Sia",
            album = "This Is Acting",
            durationMs = 211_000,
            uri = "sample://track/4",
            albumId = 4
        ),
        Track(
            id = 5,
            title = "Someone You Loved",
            artist = "Lewis Capaldi",
            album = "Divinely Uninspired to a Hellish Extent",
            durationMs = 182_000,
            uri = "sample://track/5",
            albumId = 5
        ),
        Track(
            id = 6,
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationMs = 200_000,
            uri = "sample://track/6",
            albumId = 6
        )
    )

    val albums: List<Album> = listOf(
        Album(id = 1, name = "Dream It Possible", artist = "Delacey", trackCount = 1),
        Album(id = 2, name = "Evolve", artist = "Imagine Dragons", trackCount = 1),
        Album(id = 3, name = "Camila", artist = "Camila Cabello", trackCount = 1),
        Album(id = 4, name = "This Is Acting", artist = "Sia", trackCount = 1),
        Album(id = 5, name = "Divinely Uninspired to a Hellish Extent", artist = "Lewis Capaldi", trackCount = 1),
        Album(id = 6, name = "After Hours", artist = "The Weeknd", trackCount = 1)
    )

    val artists: List<Artist> = listOf(
        Artist(id = 1, name = "Delacey", trackCount = 1, albumCount = 1),
        Artist(id = 2, name = "Imagine Dragons", trackCount = 1, albumCount = 1),
        Artist(id = 3, name = "Camila Cabello", trackCount = 1, albumCount = 1),
        Artist(id = 4, name = "Sia", trackCount = 1, albumCount = 1),
        Artist(id = 5, name = "Lewis Capaldi", trackCount = 1, albumCount = 1),
        Artist(id = 6, name = "The Weeknd", trackCount = 1, albumCount = 1)
    )

    // Accent colors reuse AppColors tokens (converted to hex) so playlist
    // rows stay within the established, non-garish palette rather than
    // introducing new ad-hoc bright colors.
    //
    // Phase 4: "Favorite Songs" (id = 1) is now a *dynamic* playlist —
    // its trackIds here are an empty placeholder that's never read
    // directly; PlaylistsScreen/PlaylistDetailScreen substitute the
    // real-time favorite track ids from FavoritesRepository whenever
    // they encounter this specific playlist id. See FAVORITES_PLAYLIST_ID.
    val playlists: List<Playlist> = listOf(
        Playlist(id = FAVORITES_PLAYLIST_ID, name = "Favorite Songs", trackIds = emptyList(), accentColorHex = "#E85A8A", iconKey = "heart"),
        Playlist(id = 2, name = "Workout", trackIds = listOf(2, 6), accentColorHex = "#3AD98A", iconKey = "bolt"),
        Playlist(id = 3, name = "Chill Vibes", trackIds = listOf(1, 5), accentColorHex = "#8A6AE8", iconKey = "leaf"),
        Playlist(id = 4, name = "Road Trip", trackIds = listOf(2, 3, 6), accentColorHex = "#E85A5A", iconKey = "road"),
        Playlist(id = 5, name = "Relax", trackIds = listOf(4, 5), accentColorHex = "#4AA6E8", iconKey = "moon")
    )

    val folders: List<MusicFolder> = listOf(
        MusicFolder(path = "/storage/music/downloaded", name = "Downloaded", itemCount = 125, accentColorHex = "#8A6AE8"),
        MusicFolder(path = "/storage/music/rock", name = "Rock", itemCount = 84, accentColorHex = "#6A7FE8"),
        MusicFolder(path = "/storage/music/pop", name = "Pop", itemCount = 65, accentColorHex = "#8A6AE8"),
        MusicFolder(path = "/storage/music/classic", name = "Classic", itemCount = 50, accentColorHex = "#6A7FE8"),
        MusicFolder(path = "/storage/music/podcast", name = "Podcast", itemCount = 18, accentColorHex = "#8A6AE8")
    )

    /** Default now-playing track before the user selects anything. */
    val defaultTrack: Track get() = tracks.first()
}
