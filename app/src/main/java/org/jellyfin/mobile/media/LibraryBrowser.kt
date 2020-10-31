package org.jellyfin.mobile.media

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import org.jellyfin.apiclient.interaction.ApiClient
import org.jellyfin.apiclient.model.dto.BaseItemDto
import org.jellyfin.apiclient.model.dto.BaseItemType
import org.jellyfin.apiclient.model.dto.ImageOptions
import org.jellyfin.apiclient.model.entities.CollectionType
import org.jellyfin.apiclient.model.entities.ImageType
import org.jellyfin.apiclient.model.entities.SortOrder
import org.jellyfin.apiclient.model.playlists.PlaylistItemQuery
import org.jellyfin.apiclient.model.querying.*
import org.jellyfin.mobile.R
import org.jellyfin.mobile.utils.*
import java.net.URLEncoder
import java.util.*

class LibraryBrowser(
    private val apiClient: ApiClient
) {
    suspend fun loadLibrary(
        context: Context,
        type: LibraryContentType,
        primaryItemId: String?,
        secondaryItemId: String?
    ): List<MediaMetadataCompat> = when (type) {
        LibraryContentType.Libraries -> {
            apiClient.getUserViews(apiClient.currentUserId)?.run {
                items.asSequence()
                    .filter { item -> item.collectionType == CollectionType.Music }
                    .map { item ->
                        val itemImageUrl = apiClient.GetImageUrl(item, ImageOptions().apply {
                            imageType = ImageType.Primary
                            maxWidth = 1080
                            quality = 90
                        })
                        MediaMetadataCompat.Builder().apply {
                            setMediaId(LibraryContentType.Library.toString() + "|" + item.id)
                            setTitle(item.name)
                            setDisplayIconUri(itemImageUrl)
                        }.build()
                    }
                    .toList()
            } ?: emptyList()
        }
        LibraryContentType.Library -> {
            val libraryViews = arrayOf(
                LibraryContentType.LibraryLatest to R.string.mediaservice_library_latest,
                LibraryContentType.LibraryAlbums to R.string.mediaservice_library_albums,
                LibraryContentType.LibraryArtists to R.string.mediaservice_library_artists,
                LibraryContentType.LibraryGenres to R.string.mediaservice_library_genres,
                LibraryContentType.LibraryPlaylists to R.string.mediaservice_library_playlists,
            )
            libraryViews.map { item ->
                MediaMetadataCompat.Builder().apply {
                    setMediaId(item.first.name + "|" + primaryItemId)
                    setTitle(context.getString(item.second))
                }.build()
            }
        }
        LibraryContentType.LibraryLatest -> {
            val query = LatestItemsQuery()
            query.parentId = primaryItemId
            query.userId = apiClient.currentUserId
            query.includeItemTypes = arrayOf(BaseItemType.Audio.name)
            query.limit = 100

            val response = apiClient.getLatestItems(query)
            if (response != null) {
                processItems(type, primaryItemId, response)
            } else {
                emptyList()
            }
        }
        LibraryContentType.LibraryAlbums,
        LibraryContentType.LibraryPlaylists -> {
            val query = ItemQuery()
            query.parentId = primaryItemId
            query.userId = apiClient.currentUserId
            query.sortBy = arrayOf(ItemSortBy.SortName)
            query.sortOrder = SortOrder.Ascending
            query.recursive = true
            query.imageTypeLimit = 1
            query.enableImageTypes = arrayOf(ImageType.Primary)
            query.limit = 100

            when (type) {
                LibraryContentType.LibraryAlbums -> {
                    if (secondaryItemId != null) {
                        query.parentId = null
                        query.artistIds = arrayOf(secondaryItemId)
                    }

                    query.includeItemTypes = arrayOf(BaseItemType.MusicAlbum.name)
                }
                LibraryContentType.LibraryPlaylists -> {
                    query.includeItemTypes = arrayOf(BaseItemType.Playlist.name)
                }
            }

            processItemsResponse(type, primaryItemId, apiClient.getItems(query))
        }
        LibraryContentType.LibraryArtists -> {
            val query = ArtistsQuery()
            query.parentId = primaryItemId
            query.userId = apiClient.currentUserId
            query.sortBy = arrayOf(ItemSortBy.SortName)
            query.sortOrder = SortOrder.Ascending
            query.recursive = true
            query.imageTypeLimit = 1
            query.enableImageTypes = arrayOf(ImageType.Primary)

            processItemsResponse(type, primaryItemId, apiClient.getArtists(query))
        }
        LibraryContentType.LibraryGenres -> {
            if (secondaryItemId != null) {
                /**
                 * View for a specific genre in a library
                 */
                val query = ItemQuery()
                query.parentId = primaryItemId
                query.userId = apiClient.currentUserId
                query.sortBy = arrayOf(ItemSortBy.IsFolder, ItemSortBy.SortName)
                query.sortOrder = SortOrder.Ascending
                query.recursive = true
                query.imageTypeLimit = 1
                query.enableImageTypes = arrayOf(ImageType.Primary)
                query.includeItemTypes = arrayOf(BaseItemType.MusicAlbum.name)
                query.genreIds = arrayOf(secondaryItemId)

                processItemsResponse(type, primaryItemId, apiClient.getItems(query))
            } else {
                /**
                 * View for genres in a library
                 */
                val query = ItemsByNameQuery()
                query.parentId = primaryItemId
                query.userId = apiClient.currentUserId
                query.sortBy = arrayOf(ItemSortBy.SortName)
                query.sortOrder = SortOrder.Ascending
                query.recursive = true

                processItemsResponse(type, primaryItemId, apiClient.getGenres(query))
            }
        }

        // TODO Add shuffle

        /*val description = MediaDescriptionCompat.Builder().apply {
    setMediaId(primaryType + "|" + primaryItemId + "|" + LibraryType.Shuffle)
    setTitle(getString(R.string.mediaservice_shuffle))
}.build()
mediaItems.add(0, MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))*/

        LibraryContentType.Album -> {
            val query = ItemQuery()
            query.parentId = primaryItemId
            query.userId = apiClient.currentUserId
            query.sortBy = arrayOf(ItemSortBy.SortName)

            processItemsResponse(type, primaryItemId, apiClient.getItems(query))
        }

        LibraryContentType.Artist -> {
            val query = ItemQuery()
            query.artistIds = arrayOf(primaryItemId)
            query.userId = apiClient.currentUserId
            query.sortBy = arrayOf(ItemSortBy.SortName)
            query.sortOrder = SortOrder.Ascending
            query.recursive = true
            query.imageTypeLimit = 1
            query.enableImageTypes = arrayOf(ImageType.Primary)
            query.limit = 100
            query.includeItemTypes = arrayOf(BaseItemType.MusicAlbum.name)

            processItemsResponse(type, primaryItemId, apiClient.getItems(query))
        }

        LibraryContentType.Playlist -> {
            val query = PlaylistItemQuery()
            query.id = primaryItemId
            query.userId = apiClient.currentUserId

            processItemsResponse(type, primaryItemId, apiClient.getPlaylistItems(query))
        }
        LibraryContentType.Shuffle -> TODO()
    }

    private fun buildMediaId(parentType: LibraryContentType, parentId: String, item: BaseItemDto) = when (item.baseItemType) {
        BaseItemType.MusicAlbum -> LibraryContentType.Album.name + "|" + item.id
        BaseItemType.MusicArtist -> LibraryContentType.Artist.name + "|" + item.id
        BaseItemType.Playlist -> LibraryContentType.Playlist.name + "|" + item.id
        BaseItemType.Audio -> item.id
        else -> "${parentType.name}|$parentId|${item.id}"
    }

    private fun buildMediaMetadata(parentType: LibraryContentType, parentId: String, item: BaseItemDto): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder()
        builder.setMediaId(buildMediaId(parentType, parentId, item))
        builder.setTitle(item.name)

        val imageOptions = ImageOptions().apply {
            imageType = ImageType.Primary
            maxWidth = 1080
            quality = 90
        }
        val primaryImageUrl = when {
            item.hasPrimaryImage -> apiClient.GetImageUrl(item, imageOptions)
            item.albumId != null -> apiClient.GetImageUrl(item.albumId, imageOptions)
            else -> null
        }

        if (item.baseItemType == BaseItemType.Audio) {
            val uri = "${apiClient.serverAddress}/Audio/${item.id}/universal?" +
                "UserId=${apiClient.currentUserId}&" +
                "DeviceId=${URLEncoder.encode(apiClient.deviceId, Charsets.UTF_8.name())}&" +
                "MaxStreamingBitrate=140000000&" +
                "Container=opus,mp3|mp3,aac,m4a,m4b|aac,flac,webma,webm,wav,ogg&" +
                "TranscodingContainer=ts&" +
                "TranscodingProtocol=hls&" +
                "AudioCodec=aac&" +
                "api_key=${apiClient.accessToken}&" +
                "PlaySessionId=${UUID.randomUUID()}&" +
                "EnableRemoteMedia=true"
            builder.setMediaUri(uri)
            builder.setAlbum(item.album)
            builder.setArtist(item.artists.joinToString())
            builder.setAlbumArtist(item.albumArtist)
            primaryImageUrl?.let(builder::setAlbumArtUri)
            if (item.indexNumber != null) builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, item.indexNumber.toLong())
        } else {
            primaryImageUrl?.let(builder::setDisplayIconUri)
        }

        return builder.build()
    }

    private fun processItemsResponse(
        type: LibraryContentType,
        itemId: String,
        response: ItemsResult?
    ): List<MediaMetadataCompat> = response?.run { processItems(type, itemId, items) } ?: emptyList()

    private fun processItems(
        parentType: LibraryContentType,
        parentId: String,
        items: Array<BaseItemDto>
    ): List<MediaMetadataCompat> = items.map { item -> buildMediaMetadata(parentType, parentId, item) }.toList()
}
