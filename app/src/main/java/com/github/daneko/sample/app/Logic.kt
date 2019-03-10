package com.github.daneko.sample.app

import android.content.Context
import androidx.room.Room
import arrow.effects.ForIO
import arrow.effects.IO
import arrow.effects.extensions.io.async.async
import arrow.effects.rx2.ForSingleK
import arrow.effects.rx2.SingleK
import arrow.effects.rx2.extensions.singlek.async.async
import arrow.effects.typeclasses.Async
import arrow.integrations.retrofit.adapter.CallKindAdapterFactory
import arrow.typeclasses.MonadThrow
import arrow.typeclasses.suspended.monaderror.Fx
import com.github.daneko.sample.domain.*
import com.github.daneko.sample.infra.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.URI

object Logic {

    @Suppress("unused")
    class ItemTypeConverter {
        @ToJson
        fun toJson(src: URI): String = src.toASCIIString()

        @FromJson
        fun fromJson(src: String): URI = URI.create(src)
    }

    private val moshi =
        Moshi.Builder()
            .add(ItemTypeConverter())
            .add(KotlinJsonAdapterFactory())
            .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://jsonplaceholder.typicode.com")
        .addCallAdapterFactory(CallKindAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private fun roomDB(appContext: Context): AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "sample_db"
    ).build()

    private val api = retrofit.create(JsonPlaceholderServices::class.java)

    private val opWithSingleK =
        @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
        object : ApiCallOp<ForSingleK>, Async<ForSingleK> by SingleK.async() {}

    private val opWithIO =
        @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
        object : ApiCallOp<ForIO>, Async<ForIO> by IO.async() {}


    fun useRxJavaAndPreference(context: Context): GetPhotoListService<ForSingleK> {

        val dao = PhotoPreferenceDao(SingleK.async(), context, moshi)

        @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
        return object : GetPhotoListService<ForSingleK>, Fx<ForSingleK> {
            override fun monadError(): MonadThrow<ForSingleK> = SingleK.async()

            override val userRepository: UserRepository<ForSingleK> =
                UserRepositoryImpl(api, opWithSingleK)
            override val photoRepository: PhotoRepository<ForSingleK> =
                PhotoRepositoryImpl(api, opWithSingleK, dao)
        }
    }

    fun useRxJavaAndRoom(context: Context): GetPhotoListService<ForSingleK> {

        val db = roomDB(context.applicationContext)
        val dao = PhotoRoomDao(SingleK.async(), Dispatchers.IO, db.photoEntityDao())

        @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
        return object : GetPhotoListService<ForSingleK>, Fx<ForSingleK> {
            override fun monadError(): MonadThrow<ForSingleK> = SingleK.async()

            override val userRepository: UserRepository<ForSingleK> =
                UserRepositoryImpl(api, opWithSingleK)
            override val photoRepository: PhotoRepository<ForSingleK> =
                PhotoRepositoryImpl(api, opWithSingleK, dao)
        }
    }

    fun useIoAndPreference(context: Context): GetPhotoListService<ForIO> {
        val dao = PhotoPreferenceDao(IO.async(), context, moshi)

        @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
        return object : GetPhotoListService<ForIO>, Fx<ForIO> {
            override fun monadError(): MonadThrow<ForIO> = IO.async()

            override val userRepository: UserRepository<ForIO> =
                UserRepositoryImpl(api, opWithIO)
            override val photoRepository: PhotoRepository<ForIO> =
                PhotoRepositoryImpl(api, opWithIO, dao)
        }
    }

    fun useIoAndRoom(context: Context): GetPhotoListService<ForIO> {
        val db = roomDB(context.applicationContext)
        val dao = PhotoRoomDao(IO.async(), Dispatchers.IO, db.photoEntityDao())

        @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
        return object : GetPhotoListService<ForIO>, Fx<ForIO> {
            override fun monadError(): MonadThrow<ForIO> = IO.async()

            override val userRepository: UserRepository<ForIO> =
                UserRepositoryImpl(api, opWithIO)
            override val photoRepository: PhotoRepository<ForIO> =
                PhotoRepositoryImpl(api, opWithIO, dao)
        }
    }
}