package org.jellyfin.mobile.model.sql.dao

import androidx.room.Dao
import androidx.room.Query
import org.jellyfin.mobile.model.sql.entity.CollectionEntity
import org.jellyfin.mobile.model.sql.entity.UserCollectionEntity

@Dao
interface CollectionDao {
    @Query("SELECT c.* FROM ${CollectionEntity.TABLE_NAME} AS c JOIN ${UserCollectionEntity.TABLE_NAME} AS uc ON collection_id WHERE uc.user_id = :userId")
    fun getCollectionsForUser(userId: String): List<CollectionEntity>
}
