package com.github.daneko.sample.domain

import arrow.Kind
import arrow.typeclasses.Monad

interface UserRepository<F> : Monad<F> {

    fun findBy(id: UserId): Kind<F, User>
}
