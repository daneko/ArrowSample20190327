package com.github.daneko.sample.infra

import arrow.Kind
import arrow.typeclasses.Monad
import com.github.daneko.sample.domain.Photo
import com.github.daneko.sample.domain.UserId

interface PhotoDao<F> : Monad<F> {

    fun store(src: List<Photo>): Kind<F, Unit>

    fun restore(key: UserId): Kind<F, List<Photo>>
}
