package com.github.daneko.sample.infra

import android.content.Context
import arrow.Kind
import arrow.core.toOption
import arrow.typeclasses.MonadThrow
import com.github.daneko.sample.domain.Photo
import com.github.daneko.sample.domain.UserId
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class PhotoPreferenceDao<F>(
    FT: MonadThrow<F>,
    context: Context,
    private val moshi: Moshi
) : PhotoDao<F>,
    MonadThrow<F> by FT {

    private val pref = context.getSharedPreferences("photo_dao",
        Context.MODE_PRIVATE
    )

    override fun store(src: List<Photo>): Kind<F, Unit> {
        // NonEmptyList使えという話はある
        return if (src.isEmpty()) raiseError(IllegalArgumentException("empty"))
        else {

            val type = Types.newParameterizedType(
                List::class.java,
                Photo::class.java
            )
            val adapter = moshi.adapter<List<Photo>>(type)
            val json = adapter.toJson(src)
            val key = src.first().owner.raw.toString()

            pref.edit()
                .putString(key, json)
                .apply()

            just(Unit)
        }
    }

    override fun restore(key: UserId): Kind<F, List<Photo>> {
        return pref.getString(key.raw.toString(), null).toOption().fold(
            ifEmpty = { just(emptyList()) },

            ifSome = {
                val type = Types.newParameterizedType(
                    List::class.java,
                    Photo::class.java
                )
                val adapter = moshi.adapter<List<Photo>>(type)
                try {
                    just(adapter.fromJson(it).orEmpty())
                } catch (e: IOException) {
                    raiseError(e)
                }
            }
        )
    }
}