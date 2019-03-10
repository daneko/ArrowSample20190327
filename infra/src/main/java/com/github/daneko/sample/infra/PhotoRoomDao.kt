package com.github.daneko.sample.infra

import androidx.room.*
import arrow.Kind
import arrow.effects.typeclasses.Async
import com.github.daneko.sample.domain.Photo
import com.github.daneko.sample.domain.PhotoId
import com.github.daneko.sample.domain.UserId
import java.net.URI
import kotlin.coroutines.CoroutineContext

// よく考えたら今setQueryExecutorあるからAsyncじゃなくても良かったかもしれない…
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class PhotoRoomDao<F>(
    AC: Async<F>,
    private val runCtx: CoroutineContext,
    private val photoEntityDao: PhotoEntityDao
) : PhotoDao<F>, Async<F> by AC {
    override fun store(src: List<Photo>): Kind<F, Unit> {
        return delay(runCtx) {
            photoEntityDao.upsert(src.map(PhotoEntity.Companion::from))
        }
    }

    override fun restore(key: UserId): Kind<F, List<Photo>> {
        return delay(runCtx) {
            photoEntityDao.findPhotoByUser(key).map { it.toDomain() }
        }
    }
}

@Entity(indices = [Index("userId")], tableName = "photo")
data class PhotoEntity(
    @PrimaryKey
    val id: PhotoId,
    val userId: UserId,
    val title: String,
    val url: URI
) {
    companion object {
        fun from(src: Photo): PhotoEntity = PhotoEntity(
            id = src.id,
            userId = src.owner,
            title = src.title,
            url = src.url
        )
    }

    fun toDomain(): Photo = Photo(
        id = id,
        owner = userId,
        title = title,
        url = url
    )
}

@Dao
interface PhotoEntityDao {

    @Query("SELECT * FROM photo WHERE userId == :userId")
    fun findPhotoByUser(userId: UserId): List<PhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(photoList: List<PhotoEntity>)
}

