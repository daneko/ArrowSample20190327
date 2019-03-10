package com.github.daneko.sample.infra

import com.github.daneko.sample.domain.PhotoId
import com.github.daneko.sample.domain.UserId
import java.net.URI
import com.github.daneko.sample.domain.Photo as DomainPhoto
import com.github.daneko.sample.domain.User as DomainUser

interface ResponseMapper {

    // memo もちろんこれくらい単純だったらそもそもMoshiのAdapter追加とかでも良いと思う
    fun User.toDomain(): DomainUser = DomainUser(
        id = UserId(id),
        name = name
    )

    fun Photo.toDomain(owner: UserId): DomainPhoto = DomainPhoto(
        id = PhotoId(id),
        owner = owner,
        title = title,
        url = URI.create(url)
    )
}