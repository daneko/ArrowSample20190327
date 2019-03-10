package com.github.daneko.sample.domain

import arrow.Kind
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.typeclasses.suspended.monaderror.Fx

typealias GetPhotoResult = Pair<User, List<Photo>>

interface GetPhotoListService<F> : Fx<F> {

    val userRepository: UserRepository<F>
    val photoRepository: PhotoRepository<F>

    // ProductだったらちゃんとErrorの詳細とか見てから判断したり、そもそもfind...とかでEither返すとか考えたほうが良い
    private fun program(userId: UserId): Kind<F, GetPhotoResult> = fx {
        val user = !userRepository.findBy(userId)
            .handleErrorWith {
                GetPhotoListErrors.UserNotFound(it).raiseError()
            }

        val photoList = !photoRepository.findByUserPhotos(userId)
            .handleErrorWith { GetPhotoListErrors.UserHasNoPhoto(it).raiseError() }

        !photoRepository.storePhotos(photoList)
            .handleErrorWith { GetPhotoListErrors.StoreError(it).raiseError() }

        Pair(user, photoList)
    }

    fun execute(userId: UserId): Kind<F, Either<GetPhotoListErrors, GetPhotoResult>> = fx {
        val attempt = !program(userId).attempt()
        !attempt.fold(
            ifLeft = {
                if (it is GetPhotoListErrors) just(it.left())
                else raiseError(it)
            },
            ifRight = { just(it.right()) }
        )
    }
}

sealed class GetPhotoListErrors : Exception() {
    override fun fillInStackTrace(): Throwable = this
    data class UserNotFound(override val cause: Throwable) : GetPhotoListErrors()
    data class UserHasNoPhoto(override val cause: Throwable) : GetPhotoListErrors()
    data class StoreError(override val cause: Throwable) : GetPhotoListErrors()
}
