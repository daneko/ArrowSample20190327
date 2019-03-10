package com.github.daneko.sample.domain

import arrow.Kind
import arrow.core.ForTry
import arrow.core.Try
import arrow.core.extensions.`try`.monadThrow.monadThrow
import arrow.core.fix
import arrow.typeclasses.MonadThrow
import com.google.common.truth.Truth
import org.junit.Test
import java.net.URI

class GetPhotoListServiceTest {

    @Test
    fun success() {

        val userRepository =
            object : UserRepository<ForTry>, MonadThrow<ForTry> by Try.monadThrow() {
                override fun findBy(id: UserId): Kind<ForTry, User> {
                    return if (id == UserId(1)) just(User(id, "test"))
                    else raiseError(Exception("not found"))
                }
            }

        val photoRepository =
            object : PhotoRepository<ForTry>, MonadThrow<ForTry> by Try.monadThrow() {
                override fun findByUserPhotos(id: UserId): Kind<ForTry, List<Photo>> {
                    return if (id == UserId(1)) just(
                        listOf(
                            Photo(
                                id = PhotoId(1),
                                owner = id,
                                title = "test",
                                url = URI("https://example.com/test_photo")
                            )
                        )
                    )
                    else raiseError(Exception("not found"))
                }

                override fun storePhotos(src: List<Photo>): Kind<ForTry, Unit> {
                    return just(Unit)
                }
            }

        val expect = Pair(
            User(UserId(1), "test"),
            listOf(
                Photo(
                    id = PhotoId(1),
                    owner = UserId(1),
                    title = "test",
                    url = URI("https://example.com/test_photo")
                )
            )
        )

        val target = object : GetPhotoListService<ForTry> {
            override val userRepository: UserRepository<ForTry> = userRepository
            override val photoRepository: PhotoRepository<ForTry> = photoRepository
            override fun monadError(): MonadThrow<ForTry> = Try.monadThrow()
        }

        val actual = target.execute(UserId(1)).fix()
        actual.fold(
            ifFailure = { Truth.assertWithMessage("not reach here").fail() },
            ifSuccess = { either ->
                either.fold(
                    ifLeft = {
                        Truth.assertWithMessage("not reach here").fail()
                    },
                    ifRight = {
                        Truth.assertThat(it).isEqualTo(expect)
                    }
                )

            }
        )
    }
}
