package org.jellyfin.mobile.model.sql.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.jellyfin.mobile.model.sql.entity.CollectionEntity.Key.SERVER_ID
import org.jellyfin.mobile.model.sql.entity.CollectionEntity.Key.TABLE_NAME

@Entity(
    tableName = TABLE_NAME,
    foreignKeys = [
        ForeignKey(entity = ServerEntity::class, parentColumns = [ServerEntity.ID], childColumns = [SERVER_ID])
    ],
)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = ID) val id: Long,
    @ColumnInfo(name = SERVER_ID) val serverId: Long,
    @ColumnInfo(name = COLLECTION_ID) val collectionId: String,
    @ColumnInfo(name = NAME) val name: String,
    @ColumnInfo(name = TYPE) val type: String,
    @ColumnInfo(name = IMAGE_TAG) val imageTag: String? = null,
) {
    companion object Key {
        const val TABLE_NAME = "Collection"
        const val ID = "id"
        const val SERVER_ID = "server_id"
        const val COLLECTION_ID = "collection_id"
        const val TYPE = "type"
        const val NAME = "name"
        const val IMAGE_TAG = "image_tag"
    }
}
