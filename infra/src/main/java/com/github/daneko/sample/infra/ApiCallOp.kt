package com.github.daneko.sample.infra

import arrow.Kind
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.data.EitherT
import arrow.effects.typeclasses.Async
import arrow.integrations.retrofit.adapter.CallK
import okhttp3.HttpUrl
import retrofit2.Response

// 今のFxとの相性を考えるとAsync→Concurrentが良さそうだが、その場合実質IOしか選べないので、Asyncのままで
interface ApiCallOp<F> : Async<F> {

    fun <A> CallK<A>.fetch(): Kind<F, A> {
        return this.async(this@ApiCallOp)
            .flatMap { response ->
                response.toEither().fold(
                    ifLeft = { it.raiseError<A>() },
                    ifRight = { just(it) }
                )
            }
    }

    fun <A> CallK<A>.fetchEitherT(): EitherT<F, ResponseError, A> {
        return EitherT(this.async(this@ApiCallOp)
            .map { it.toEither() })
    }

    private fun <A> Response<A>.toEither(): Either<ResponseError, A> {
        val requestUrl = raw().request().url()
        return if (isSuccessful) {
            body()?.right() ?: ResponseError.UnknownError(requestUrl).left()
        } else {
            val resCode = code()
            return when {
                resCode < 400 -> ResponseError.UnknownError(requestUrl).left()
                resCode < 500 -> ResponseError.ClientError(requestUrl, resCode).left()
                else -> ResponseError.ServerError(requestUrl, resCode).left()
            }
        }
    }
}

sealed class ResponseError(requestUrl: HttpUrl) : Exception() {
    override fun fillInStackTrace(): Throwable = this
    data class ClientError(val url: HttpUrl, val code: Int) : ResponseError(url)
    data class ServerError(val url: HttpUrl, val code: Int) : ResponseError(url)
    data class UnknownError(val url: HttpUrl) : ResponseError(url)
}
