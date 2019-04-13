package com.github.daneko.sample.domain

import arrow.Kind

interface PhotoRepository<F> {

    fun findByUserPhotos(id: UserId): Kind<F, List<Photo>>

    fun storePhotos(src: List<Photo>): Kind<F, Unit>
}
