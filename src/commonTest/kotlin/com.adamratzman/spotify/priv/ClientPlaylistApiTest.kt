/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2021; Original author: Adam Ratzman */
package com.adamratzman.spotify.priv

import com.adamratzman.spotify.AbstractTest
import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.SpotifyException
import com.adamratzman.spotify.endpoints.client.SpotifyPlayablePositions
import com.adamratzman.spotify.models.Playlist
import com.adamratzman.spotify.models.SimplePlaylist
import com.adamratzman.spotify.models.toTrackUri
import com.adamratzman.spotify.runBlockingTest
import com.adamratzman.spotify.utils.Platform
import com.adamratzman.spotify.utils.currentApiPlatform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class ClientPlaylistApiTest : AbstractTest<SpotifyClientApi>() {
    var createdPlaylist: Playlist? = null
    var playlistsBefore: List<SimplePlaylist>? = null

    private suspend fun init() {
        if (api != null) {
            playlistsBefore = api!!.playlists.getClientPlaylists().getAllItemsNotNull()
            createdPlaylist = api!!.playlists.createClientPlaylist("this is a test playlist", "description")
        }
    }

    suspend fun testPrereqOverride(): Boolean {
        if (api == null) return false
        init()
        return true
    }

    private suspend fun tearDown() {
        if (createdPlaylist != null) {
            coroutineScope {
                api!!.playlists.getClientPlaylists().getAllItemsNotNull()
                    .filter { it.name == "this is a test playlist" }
                    .map {
                        async {
                            if (api!!.following.isFollowingPlaylist(it.id)) {
                                api!!.playlists.deleteClientPlaylist(it.id)
                            }
                        }
                    }
                    .awaitAll()
            }
        }

        coroutineScope {
            api!!.playlists.getClientPlaylists(limit = 50).getAllItemsNotNull()
                .filter { it.name == "test playlist" }
                .map { playlist -> async { api!!.playlists.deleteClientPlaylist(playlist.id) } }
                .awaitAll()
        }
    }

    @Test
    fun testGetClientPlaylists() {
        return runBlockingTest {
            super.build<SpotifyClientApi>()
            if (!testPrereqOverride()) return@runBlockingTest else api!!

            assertEquals(
                api!!.playlists.getClientPlaylists().getAllItemsNotNull().size - 1,
                playlistsBefore!!.size
            )

            tearDown()
        }
    }

    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testAddAndRemoveChunkedTracks() {
        return runBlockingTest {
            super.build<SpotifyClientApi>()
            if (!testPrereqOverride()) return@runBlockingTest else api!!

            val usTop50Uri = "spotify:playlist:37i9dQZEVXbLRQDuF5jeBp"
            val globalTop50Uri = "spotify:playlist:37i9dQZEVXbMDoHDwVN2tF"
            val globalViral50Uri = "spotify:playlist:37i9dQZEVXbLiRSasKsNU9"

            val tracks = listOf(
                async { api!!.playlists.getPlaylist(usTop50Uri)!!.tracks.getAllItemsNotNull() },
                async { api!!.playlists.getPlaylist(globalTop50Uri)!!.tracks.getAllItemsNotNull() },
                async { api!!.playlists.getPlaylist(globalViral50Uri)!!.tracks.getAllItemsNotNull() }
            ).awaitAll().flatten().mapNotNull { it.track?.uri?.uri }

            api!!.spotifyApiOptions.allowBulkRequests = true

            /*val playlistSize = { api!!.playlists.getClientPlaylist(createdPlaylist!!.id)!!.tracks.total }
            val sizeBefore = playlistSize()
            api!!.playlists.addPlayablesToClientPlaylist(createdPlaylist!!.id, tracks=*tracks.toTypedArray())
            assertEquals(sizeBefore.plus(tracks.size), playlistSize())
            api!!.playlists.removeTracksFromClientPlaylist(createdPlaylist!!.id, tracks=*tracks.toTypedArray())
            assertEquals(sizeBefore, playlistSize())*/

            api!!.spotifyApiOptions.allowBulkRequests = false

            tearDown()
        }
    }

    @Test
    fun testEditPlaylists() {
        if (/*currentApiPlatform != Platform.NATIVE*/true) {
            return runBlockingTest {
                super.build<SpotifyClientApi>()
                if (!testPrereqOverride()) return@runBlockingTest else api!!

                api!!.playlists.changeClientPlaylistDetails(
                    createdPlaylist!!.id, "test playlist", public = false,
                    collaborative = true, description = "description 2"
                )

                api!!.playlists.addPlayablesToClientPlaylist(
                    createdPlaylist!!.id,
                    "3WDIhWoRWVcaHdRwMEHkkS".toTrackUri(),
                    "7FjZU7XFs7P9jHI9Z0yRhK".toTrackUri()
                )

                api!!.playlists.uploadClientPlaylistCover(
                    createdPlaylist!!.id,
                    imageUrl = "http://www.personal.psu.edu/kbl5192/jpg.jpg"
                )

                var updatedPlaylist = api!!.playlists.getClientPlaylist(createdPlaylist!!.id)!!
                val fullPlaylist = updatedPlaylist.toFullPlaylist()!!

                assertTrue(
                    updatedPlaylist.collaborative && updatedPlaylist.public == false &&
                            updatedPlaylist.name == "test playlist" && fullPlaylist.description == "description 2"
                )

                assertTrue(updatedPlaylist.tracks.total == 2 && updatedPlaylist.images.isNotEmpty())

                api!!.playlists.reorderClientPlaylistPlayables(updatedPlaylist.id, 1, insertionPoint = 0)

                updatedPlaylist = api!!.playlists.getClientPlaylist(createdPlaylist!!.id)!!

                assertTrue(updatedPlaylist.toFullPlaylist()?.tracks?.items?.get(0)?.track?.id == "7FjZU7XFs7P9jHI9Z0yRhK")

                api!!.playlists.removeAllClientPlaylistPlayables(updatedPlaylist.id)

                updatedPlaylist = api!!.playlists.getClientPlaylist(createdPlaylist!!.id)!!

                assertTrue(updatedPlaylist.tracks.total == 0)

                tearDown()
            }
        }
    }

    @Test
    fun testRemovePlaylistPlayables() {
        if (currentApiPlatform != Platform.NATIVE) {
            return runBlockingTest {
                super.build<SpotifyClientApi>()
                if (!testPrereqOverride()) return@runBlockingTest else api!!

                val playableUriOne = "3WDIhWoRWVcaHdRwMEHkkS".toTrackUri()
                val playableUriTwo = "7FjZU7XFs7P9jHI9Z0yRhK".toTrackUri()
                api!!.playlists.addPlayablesToClientPlaylist(
                    createdPlaylist!!.id,
                    playableUriOne,
                    playableUriOne,
                    playableUriTwo,
                    playableUriTwo
                )

                assertTrue(api!!.playlists.getPlaylistTracks(createdPlaylist!!.id).items.size == 4)

                api!!.playlists.removePlayableFromClientPlaylist(createdPlaylist!!.id, playableUriOne)

                assertEquals(
                    listOf(playableUriTwo, playableUriTwo),
                    api!!.playlists.getPlaylistTracks(createdPlaylist!!.id).items.map { it.track?.uri })

                api!!.playlists.addPlayableToClientPlaylist(createdPlaylist!!.id, playableUriOne)

                api!!.playlists.removePlayableFromClientPlaylist(
                    createdPlaylist!!.id,
                    playableUriTwo,
                    SpotifyPlayablePositions(1)
                )

                assertEquals(
                    listOf(playableUriTwo, playableUriOne),
                    api!!.playlists.getPlaylistTracks(createdPlaylist!!.id).items.map { it.track?.uri })

                api!!.playlists.setClientPlaylistPlayables(
                    createdPlaylist!!.id,
                    playableUriOne,
                    playableUriOne,
                    playableUriTwo,
                    playableUriTwo
                )

                api!!.playlists.removePlayablesFromClientPlaylist(createdPlaylist!!.id, playableUriOne, playableUriTwo)

                assertTrue(api!!.playlists.getPlaylistTracks(createdPlaylist!!.id).items.isEmpty())

                api!!.playlists.setClientPlaylistPlayables(
                    createdPlaylist!!.id,
                    playableUriTwo,
                    playableUriOne,
                    playableUriTwo,
                    playableUriTwo,
                    playableUriOne
                )

                api!!.playlists.removePlayablesFromClientPlaylist(
                    createdPlaylist!!.id, Pair(playableUriOne, SpotifyPlayablePositions(4)),
                    Pair(playableUriTwo, SpotifyPlayablePositions(0))
                )

                assertEquals(
                    listOf(playableUriOne, playableUriTwo, playableUriTwo),
                    api!!.playlists.getPlaylistTracks(createdPlaylist!!.id).items.map { it.track?.uri })

                assertFailsWith<SpotifyException.BadRequestException> {
                    api!!.playlists.removePlayablesFromClientPlaylist(
                        createdPlaylist!!.id,
                        Pair(playableUriOne, SpotifyPlayablePositions(3))
                    )
                }

                tearDown()
            }
        }
    }
}
