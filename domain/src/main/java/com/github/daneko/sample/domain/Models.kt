package com.github.daneko.sample.domain

import java.net.URI

data class UserId(val raw: Int)

data class User(
    val id: UserId,
    val name: String
)

data class PhotoId(val raw: Int)

data class Photo(
    val id: PhotoId,
    val owner: UserId,
    val title: String,
    val url: URI
)