package com.mira.app.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserSummary(
    @SerializedName(value = "userId", alternate = ["_id", "id"])
    val userId: String = "",
    val email: String = "",
    val preferredLanguage: String = "en"
)

data class AuthResponse(
    val token: String,
    val user: UserSummary? = null
)

data class SettingsResponse(
    val preferredLanguage: String = "en",
    val notificationsEnabled: Boolean = true
)

data class UpdateSettingsRequest(
    val preferredLanguage: String? = null,
    val notificationsEnabled: Boolean? = null
)

data class Room(
    @SerializedName(value = "roomId", alternate = ["_id", "id"])
    val roomId: String = "",
    val name: String = "",
    val description: String = "",
    val colorTag: String = "",
    val isSensitive: Boolean = false,
    val crisisResourceLink: String? = null,
    val postCount: Int = 0
)

data class Comment(
    @SerializedName(value = "commentId", alternate = ["_id", "id"])
    val commentId: String = "",
    val postId: String = "",
    val authorSessionAlias: String = "Anonymous",
    @SerializedName(value = "content", alternate = ["comment", "text"])
    val content: String = "",
    val createdAt: String = ""
)

data class Post(
    @SerializedName(value = "postId", alternate = ["_id", "id"])
    val postId: String = "",
    val roomId: String = "",
    val authorSessionAlias: String = "Anonymous",
    val content: String = "",
    val meTooCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String = "",
    val userHasTappedMeToo: Boolean = false,
    val comments: List<Comment> = emptyList()
)

data class PostFeedResponse(
    val page: Int = 1,
    val limit: Int = 20,
    val posts: List<Post> = emptyList()
)

data class MeTooResponse(
    val meTooCount: Int = 0,
    val userHasTappedMeToo: Boolean = false
)

data class CreatePostRequest(val roomId: String, val content: String)

data class CreateCommentRequest(val comment: String)

data class PostDetail(
    @SerializedName(value = "postId", alternate = ["_id", "id"])
    val postId: String = "",
    val roomId: String = "",
    val authorSessionAlias: String = "Anonymous",
    val content: String = "",
    val meTooCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: String = "",
    val userHasTappedMeToo: Boolean = false,
    val comments: List<Comment> = emptyList()
)

data class JournalEntry(
    @SerializedName(value = "entryId", alternate = ["_id", "id"])
    val entryId: String = "",
    val linkedRoomId: String? = null,
    @SerializedName(value = "content", alternate = ["entry", "text"])
    val content: String = "",
    val createdAt: String = ""
)

data class CreateJournalRequest(val entry: String, val linkedRoomId: String? = null)
