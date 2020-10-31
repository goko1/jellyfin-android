package org.jellyfin.mobile.model.repository

import org.jellyfin.apiclient.interaction.ApiClient
import org.jellyfin.mobile.model.sql.JellyfinDatabase
import org.jellyfin.mobile.utils.getUserViews

class JellyfinRepository(
    val apiClient: ApiClient,
    val database: JellyfinDatabase,
) {
    suspend fun loadUserCollections(userId: String) {
    }
}
