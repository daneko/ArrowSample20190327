package com.github.daneko.sample.infra

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.github.daneko.sample.domain.PhotoId
import com.github.daneko.sample.domain.UserId
import java.net.URI

@Database(entities = [PhotoEntity::class], version = 1)
@TypeConverters(value = [PhotoIdConverter::class, UserIdConverter::class, URIConverter::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoEntityDao(): PhotoEntityDao
}

class PhotoIdConverter {
    @TypeConverter
    fun from(src: Int): PhotoId = PhotoId(src)

    @TypeConverter
    fun to(src: PhotoId): Int = src.raw
}

class UserIdConverter {
    @TypeConverter
    fun from(src: Int): UserId = UserId(src)

    @TypeConverter
    fun to(src: UserId): Int = src.raw
}

class URIConverter {

    @TypeConverter
    fun from(src: String): URI = URI(src)

    @TypeConverter
    fun to(src: URI): String = src.toASCIIString()
}

