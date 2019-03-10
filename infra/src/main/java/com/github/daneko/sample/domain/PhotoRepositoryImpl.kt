package com.github.daneko.sample.domain

import arrow.Kind
import arrow.effects.typeclasses.MonadDefer
import arrow.effects.typeclasses.suspended.monaddefer.Fx
import arrow.typeclasses.Monad
import com.github.daneko.sample.infra.ApiCallOp
import com.github.daneko.sample.infra.JsonPlaceholderServices
import com.github.daneko.sample.infra.PhotoDao
import com.github.daneko.sample.infra.ResponseMapper

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class PhotoRepositoryImpl<F>(
    private val api: JsonPlaceholderServices,
    private val op: ApiCallOp<F>,
    private val dao: PhotoDao<F>
) : PhotoRepository<F>, Monad<F> by op, Fx<F>, ResponseMapper {
    override fun monadDefer(): MonadDefer<F> = op

    override fun findByUserPhotos(id: UserId): Kind<F, List<Photo>> = with(this) {
        fx {
            val candidate = !dao.restore(id)
            if (candidate.isEmpty()) !fetchPhotos(id)
            else candidate
        }
    }

    private fun fetchPhotos(id: UserId): Kind<F, List<Photo>> = with(op) {
        fx {
            val albums = !api.getUserAlbums(id.raw).fetch()
            val albumIds = albums.map { it.id }
            val photos = !api.getPhotos(albumIds).fetch()
            photos.map { it.toDomain(id) }
        }
    }

    override fun storePhotos(src: List<Photo>): Kind<F, Unit> {
        return dao.store(src)
    }
}