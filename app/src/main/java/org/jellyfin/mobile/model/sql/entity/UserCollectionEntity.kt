package org.jellyfin.mobile.model.sql.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import org.jellyfin.mobile.model.sql.entity.UserCollectionEntity.Key.COLLECTION_ID
import org.jellyfin.mobile.model.sql.entity.UserCollectionEntity.Key.TABLE_NAME
import org.jellyfin.mobile.model.sql.entity.UserCollectionEntity.Key.USER_ID

@Entity(
    tableName = TABLE_NAME,
    primaryKeys = [USER_ID, COLLECTION_ID],
    indices = [Index(USER_ID), Index(COLLECTION_ID)],
    foreignKeys = [
        ForeignKey(entity = CollectionEntity::class, parentColumns = [CollectionEntity.ID], childColumns = [COLLECTION_ID]),
        ForeignKey(entity = UserEntity::class, parentColumns = [UserEntity.ID], childColumns = [USER_ID]),
    ]
)
data class UserCollectionEntity(
    @ColumnInfo(name = COLLECTION_ID) val collectionId: Long,
    @ColumnInfo(name = USER_ID) val userId: Long,
) {
    companion object Key {
        const val TABLE_NAME = "UserCollection"
        const val COLLECTION_ID = "collection_id"
        const val USER_ID = "user_id"
    }
}
