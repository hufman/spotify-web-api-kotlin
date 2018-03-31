# Spotify Kotlin Wrapper
Until we get the JCenter repository running, the repository will be available on Jitpack. Link is below

[![](https://jitpack.io/v/adamint/spotify-web-api-kotlin.svg)](https://jitpack.io/#adamint/spotify-web-api-kotlin)

 
This library represents an updated and more intuitive version of thelinmichael's Spotify Wrapper, which was created in Java. This is built using Kotlin to take advantage of stlib functionality and Kotlin syntax.

### What this library does
  - Uses the **Client Credentials** authorization type for the Spotify Web API
  - Allows developers to use all non-client endpoints
### What this library does NOT do
  - Handle client OAuth (however, it does provide a method to obtain the OAuth URL for provided scopes)
# How do I get it?
You **must** have Jitpack in your repositories. An example for gradle is shown below
```
repositories {
	maven { url 'https://jitpack.io' }
}
```
Then, you can use the following (if you're using gradle - if not, click on the Jitpack link above)
```
dependencies {
	compile 'com.github.adamint:spotify-web-api-kotlin:3.0'
}
```

# How do I use this?
You must first create a `SpotifyAPI` or `SpotifyClientAPI` object by using the exposed `Builder`, as shown below. Keep in mind, you only need to create one of these!
An example for creating the API object without client credentials:
```kotlin
    val api = SpotifyAPI.Builder("clientId","clientSecret").build()
```
After you've done this, you have access to the following objects:

###Public (SpotifyAPI):
  - `SpotifyAPI.search` returns a `SearchAPI` object, allowing you to search for tracks, albums, playlists, and artists
  - `SpotifyAPI.albums` returns an `AlbumAPI` object, allowing you to retrieve albums and their tracks
  - `SpotifyAPI.artists` returns an `ArtistsAPI` object, allowing you to retrieve artists by their ids, get their albums and top tracks, and see related artists.
  - `SpotifyAPI.browse` returns a `BrowseAPI` object, allowing you to get new album releases, get featured playlists, get playlists for specific categories, and generate recommendations. The `getRecommendations` method is documented by parameter to avoid confusion.
  - `SpotifyAPI.playlists` returns a `PlaylistsAPI` object,  allowing you to retrieve playlists and their tracks
  - `SpotifyAPI.profiles` returns a `ProfilesAPI` object,  allowing you to retrieve the public user object by a user's id
  - `SpotifyAPI.tracks` returns a `TracksAPI` object,  allowing you to retrieve tracks or get an audio analysis or overview of the track's audio features.
  - `SpotifyAPI.publicFollowing` returns a `PublicFollowingAPI` object, allowing you to check if users are following a specified user/artist
  
###Private (SpotifyClientAPI)
  - `SpotifyClientAPI.personalization` gives access to the user's top tracks and artists
  - `SpotifyClientAPI.userProfile` lets you see and manage the user's profile
  - `SpotifyClientAPI.userLibrary` lets you see and manage the user's library
  - `SpotifyClientAPI.userFollowing` lets you see and manage artists and users that the current user is following
  - `SpotifyClientAPI.clientPlaylists` lets you create and manage user playlists
  - `SpotifyClientAPI.player` lets you see the user's devices and manage their playback state. This is in *beta* per Spotify documentation and methods could stop working at any time. All management endpoints only work for Premium users.

### Example using Recommendations (BrowseAPI)

```kotlin
    val api = SpotifyAPI.Builder("yourClientId","yourClientSecret").build()
    val recommendations = api.browse.getRecommendations(seedArtists = listOf("3TVXtAsR1Inumwj472S9r4"), seedGenres = listOf("pop", "country"), targets = hashMapOf(Pair("speechiness", 1.0), Pair("danceability", 1.0))))
```
