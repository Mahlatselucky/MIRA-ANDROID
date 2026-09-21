package com.mira.app.network

import com.mira.app.model.AuthResponse
import com.mira.app.model.Comment
import com.mira.app.model.CreateCommentRequest
import com.mira.app.model.CreateJournalRequest
import com.mira.app.model.CreatePostRequest
import com.mira.app.model.JournalEntry
import com.mira.app.model.LoginRequest
import com.mira.app.model.MeTooResponse
import com.mira.app.model.Post
import com.mira.app.model.PostDetail
import com.mira.app.model.PostFeedResponse
import com.mira.app.model.RegisterRequest
import com.mira.app.model.Room
import com.mira.app.model.SettingsResponse
import com.mira.app.model.UpdateSettingsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PUT("settings")
    suspend fun updateSettings(@Body body: UpdateSettingsRequest): Response<SettingsResponse>

    @GET("rooms")
    suspend fun getRooms(): Response<List<Room>>

    @GET("rooms/{roomId}/posts")
    suspend fun getRoomPosts(
        @Path("roomId") roomId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<com.mira.app.model.Post>>

    @POST("posts")
    suspend fun createPost(
        @Body body: CreatePostRequest
    ): Response<Post>

    @GET("posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: String): Response<PostDetail>

    @PUT("posts/{postId}/metoo")
    suspend fun toggleMeToo(@Path("postId") postId: String): Response<Post>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body body: CreateCommentRequest
    ): Response<Post>

    @GET("journal")
    suspend fun getJournalEntries(): Response<List<JournalEntry>>

    @POST("journal")
    suspend fun createJournalEntry(@Body body: CreateJournalRequest): Response<JournalEntry>
}
