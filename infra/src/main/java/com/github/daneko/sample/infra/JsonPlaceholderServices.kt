package com.github.daneko.sample.infra

import arrow.integrations.retrofit.adapter.CallK
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * use https://jsonplaceholder.typicode.com
 */
interface JsonPlaceholderServices {

    @GET("/users/{id}")
    fun getUser(@Path("id") id: Int): CallK<User>

    @GET("/albums")
    fun getUserAlbums(@Query("userId") userId: Int): CallK<List<Album>>

    @GET("/photos")
    fun getPhotos(@Query("albumId") albums: List<Int>): CallK<List<Photo>>
}

// もっといっぱいPropertyあるけどめんどいので省略
data class User(
    val id: Int,
    val name: String
)

data class Album(
    val id: Int,
    val title: String
)

data class Photo(
    val id: Int,
    val title: String,
    val url: String
)