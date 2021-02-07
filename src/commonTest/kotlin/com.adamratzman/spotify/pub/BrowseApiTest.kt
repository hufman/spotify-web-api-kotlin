/* Spotify Web API, Kotlin Wrapper; MIT License, 2017-2021; Original author: Adam Ratzman */
package com.adamratzman.spotify.pub

import com.adamratzman.spotify.GenericSpotifyApi
import com.adamratzman.spotify.SpotifyException
import com.adamratzman.spotify.assertFailsWithSuspend
import com.adamratzman.spotify.buildSpotifyApi
import com.adamratzman.spotify.endpoints.public.TuneableTrackAttribute
import com.adamratzman.spotify.runBlockingTest
import com.adamratzman.spotify.utils.Locale
import com.adamratzman.spotify.utils.Market
import com.adamratzman.spotify.utils.getCurrentTimeMs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class BrowseApiTest {
    lateinit var api: GenericSpotifyApi

    init {
        runBlockingTest {
            buildSpotifyApi()?.let { api = it }
        }
    }

    fun testPrereq() = ::api.isInitialized

    @Test
    fun testGenreSeeds() {
        runBlockingTest {
            if (!testPrereq()) return@runBlockingTest
            assertTrue(api.browse.getAvailableGenreSeeds().isNotEmpty())
        }
    }

    @Test
    fun testGetCategoryList() {
        runBlockingTest {
            if (!testPrereq()) return@runBlockingTest

            assertNotSame(
                api.browse.getCategoryList(locale = Locale.ar_AE).items[0],
                api.browse.getCategoryList().items[0]
            )
            assertTrue(api.browse.getCategoryList(4, 3, market = Market.CA).items.isNotEmpty())
            assertTrue(api.browse.getCategoryList(4, 3, locale = Locale.fr_FR, market = Market.CA).items.isNotEmpty())
        }
    }

    @Test
    fun testGetCategory() {
        runBlockingTest {
            if (!testPrereq()) return@runBlockingTest
            assertNotNull(api.browse.getCategory("pop"))
            assertNotNull(api.browse.getCategory("pop", Market.FR))
            assertNotNull(api.browse.getCategory("pop", Market.FR, locale = Locale.en_US))
            assertNotNull(api.browse.getCategory("pop", Market.FR, locale = Locale.sr_ME))
            assertFailsWithSuspend<SpotifyException.BadRequestException> { api.browse.getCategory("no u", Market.US) }
        }
    }

    @Test
    fun testGetPlaylistsByCategory() {
        runBlockingTest {
            if (!testPrereq()) return@runBlockingTest
            assertFailsWithSuspend<SpotifyException.BadRequestException> {
                api.browse.getPlaylistsForCategory(
                    "no u",
                    limit = 4
                )
            }
            assertTrue(api.browse.getPlaylistsForCategory("pop", 10, 0, Market.FR).items.isNotEmpty())
        }
    }

    @Test
    fun testGetFeaturedPlaylists() {
        runBlockingTest {
            if (!testPrereq()) return@runBlockingTest

            assertTrue(
                api.browse.getFeaturedPlaylists(
                    5,
                    4,
                    market = Market.US,
                    timestamp = getCurrentTimeMs() - 10000000
                ).playlists.total > 0
            )
            assertTrue(api.browse.getFeaturedPlaylists(offset = 32).playlists.total > 0)
        }
    }

    @Test
    fun testGetNewReleases() {
        runBlockingTest {
            if (!testPrereq()) return@runBlockingTest

            assertTrue(api.browse.getNewReleases(market = Market.CA).items.isNotEmpty())
            assertTrue(api.browse.getNewReleases(limit = 1, offset = 3).items.isNotEmpty())
            assertTrue(api.browse.getNewReleases(limit = 6, offset = 44, market = Market.US).items.isNotEmpty())
        }
    }

    @Test
    fun testGetRecommendations() {
        runBlockingTest {
            if (!testPrereq()) return@runBlockingTest

            assertFailsWithSuspend<SpotifyException.BadRequestException> { api.browse.getTrackRecommendations() }

            assertFailsWithSuspend<SpotifyException.BadRequestException> {
                api.browse.getTrackRecommendations(seedArtists = listOf("abc"))
            }
            api.browse.getTrackRecommendations(seedArtists = listOf("1kNQXvepPjaPgUfeDAF2h6"))

            assertFailsWithSuspend<SpotifyException.BadRequestException> {
                api.browse.getTrackRecommendations(seedTracks = listOf("abc"))
            }
            api.browse.getTrackRecommendations(seedTracks = listOf("3Uyt0WO3wOopnUBCe9BaXl")).tracks
            api.browse.getTrackRecommendations(
                seedTracks = listOf(
                    "6d9iYQG2JvTTEgcndW81lt",
                    "3Uyt0WO3wOopnUBCe9BaXl"
                )
            ).tracks

            api.browse.getTrackRecommendations(seedGenres = listOf("abc"))
            api.browse.getTrackRecommendations(seedGenres = listOf("pop"))

            api.browse.getTrackRecommendations(
                seedGenres = listOf(
                    "pop",
                    "latinx"
                )
            )

            api.browse.getTrackRecommendations(
                seedArtists = listOf("2C2sVVXanbOpymYBMpsi89"),
                seedTracks = listOf("6d9iYQG2JvTTEgcndW81lt", "3Uyt0WO3wOopnUBCe9BaXl"),
                seedGenres = listOf("pop")
            )

            assertFailsWithSuspend<IllegalArgumentException> {
                api.browse.getTrackRecommendations(
                    targetAttributes = listOf(
                        TuneableTrackAttribute.Acousticness.asTrackAttribute(
                            3f
                        )
                    )
                )
            }
            assertTrue(
                api.browse.getTrackRecommendations(
                    targetAttributes = listOf(
                        TuneableTrackAttribute.Acousticness.asTrackAttribute(1f)
                    ),
                    seedGenres = listOf("pop")
                ).tracks.isNotEmpty()
            )

            assertFailsWithSuspend<IllegalArgumentException> {
                api.browse.getTrackRecommendations(
                    minAttributes = listOf(
                        TuneableTrackAttribute.Acousticness.asTrackAttribute(
                            3f
                        )
                    )
                )
            }
            assertTrue(
                api.browse.getTrackRecommendations(
                    minAttributes = listOf(
                        TuneableTrackAttribute.Acousticness.asTrackAttribute(0.5f)
                    ),
                    seedGenres = listOf("pop")
                ).tracks.isNotEmpty()
            )

            assertFailsWithSuspend<SpotifyException.BadRequestException> {
                api.browse.getTrackRecommendations(
                    maxAttributes = listOf(
                        TuneableTrackAttribute.Speechiness.asTrackAttribute(
                            0.9f
                        )
                    )
                )
            }
            assertTrue(
                api.browse.getTrackRecommendations(
                    maxAttributes = listOf(
                        TuneableTrackAttribute.Acousticness.asTrackAttribute(0.9f),
                        TuneableTrackAttribute.Danceability.asTrackAttribute(0.9f)
                    ),
                    seedGenres = listOf("pop")
                ).tracks.isNotEmpty()
            )

            assertTrue(TuneableTrackAttribute.values().first().asTrackAttribute(0f).value == 0f)
        }
    }

    @Test
    fun testTuneableTrackAttributeTypes() {
        val float1: TuneableTrackAttribute<*> = TuneableTrackAttribute.Speechiness
        val float2: TuneableTrackAttribute<*> = TuneableTrackAttribute.Acousticness
        val int1: TuneableTrackAttribute<*> = TuneableTrackAttribute.Key
        val int2: TuneableTrackAttribute<*> = TuneableTrackAttribute.Popularity

        assertEquals(float1.typeClass, float2.typeClass)
        assertEquals(int1.typeClass, int2.typeClass)
        assertNotEquals(float1.typeClass, int1.typeClass)
    }
}
