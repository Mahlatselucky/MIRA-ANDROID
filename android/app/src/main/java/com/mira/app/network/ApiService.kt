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

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @GET("api/user/settings")
    suspend fun getSettings(): Response<SettingsResponse>

    @PUT("api/user/settings")
    suspend fun updateSettings(@Body body: UpdateSettingsRequest): Response<SettingsResponse>

    @GET("api/rooms")
    suspend fun getRooms(): Response<List<Room>>

    @GET("api/rooms/{roomId}/posts")
    suspend fun getRoomPosts(
        @Path("roomId") roomId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PostFeedResponse>

    @POST("api/rooms/{roomId}/posts")
    suspend fun createPost(
        @Path("roomId") roomId: String,
        @Body body: CreatePostRequest
    ): Response<Post>

    @GET("api/posts/{postId}")
    suspend fun getPostDetail(@Path("postId") postId: String): Response<PostDetail>

    @POST("api/posts/{postId}/metoo")
    suspend fun toggleMeToo(@Path("postId") postId: String): Response<MeTooResponse>

    @POST("api/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body body: CreateCommentRequest
    ): Response<Comment>

    @GET("api/journal")
    suspend fun getJournalEntries(): Response<List<JournalEntry>>

    @POST("api/journal")
    suspend fun createJournalEntry(@Body body: CreateJournalRequest): Response<JournalEntry>
}
