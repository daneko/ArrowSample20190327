package com.github.daneko.sample.domain

import arrow.Kind
import arrow.typeclasses.Monad

interface PhotoRepository<F> : Monad<F> {

    fun findByUserPhotos(id: UserId): Kind<F, List<Photo>>

    fun storePhotos(src: List<Photo>): Kind<F, Unit>
}
