package com.github.daneko.sample.domain

import arrow.Kind
import com.github.daneko.sample.infra.ApiCallOp
import com.github.daneko.sample.infra.JsonPlaceholderServices
import com.github.daneko.sample.infra.ResponseMapper

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class UserRepositoryImpl<F>(
    private val api: JsonPlaceholderServices,
    private val op: ApiCallOp<F>
) : UserRepository<F>, ResponseMapper {

    override fun findBy(id: UserId): Kind<F, User> = with(op) {
        api.getUser(id.raw).fetch()
            .map { it.toDomain() }
    }
}
