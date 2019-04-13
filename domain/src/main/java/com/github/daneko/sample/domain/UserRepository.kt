package com.github.daneko.sample.domain

import arrow.Kind

interface UserRepository<F> {

    fun findBy(id: UserId): Kind<F, User>
}
