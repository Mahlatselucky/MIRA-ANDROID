
package com.mira.app.model

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserSummary(
    val userId: String,
    val email: String,
    val preferredLanguage: String
)

data class AuthResponse(
    val token: String,
    val user: UserSummary
)
data class SettingsResponse(
    val preferredLanguage: String,
    val notificationsEnabled: Boolean
)

data class UpdateSettingsRequest(
    val preferredLanguage: String? = null,
    val notificationsEnabled: Boolean? = null
)
data class Room(
    val roomId: String,
    val name: String,
    val description: String,
    val colorTag: String,
    val isSensitive: Boolean,
    val crisisResourceLink: String?,
    val postCount: Int
)

data class Post(
    val postId: String,
    val roomId: String,
    val authorSessionAlias: String,
    val content: String,
    val meTooCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val userHasTappedMeToo: Boolean
)

data class PostFeedResponse(
    val page: Int,
    val limit: Int,
    val posts: List<Post>
)

data class MeTooResponse(
    val meTooCount: Int,
    val userHasTappedMeToo: Boolean
)

data class CreatePostRequest(val content: String)

data class Comment(
    val commentId: String,
    val postId: String,
    val authorSessionAlias: String,
    val content: String,
    val createdAt: String
)

data class CreateCommentRequest(val content: String)

data class PostDetail(
    val postId: String,
    val roomId: String,
    val authorSessionAlias: String,
    val content: String,
    val meTooCount: Int,
    val commentCount: Int,
    val createdAt: String,
    val userHasTappedMeToo: Boolean,
    val comments: List<Comment>
)

data class JournalEntry(
    val entryId: String,
    val linkedRoomId: String?,
    val content: String,
    val createdAt: String
)

data class CreateJournalRequest(val content: String, val linkedRoomId: String? = null)
