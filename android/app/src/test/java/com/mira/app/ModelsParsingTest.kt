package com.mira.app

import com.google.gson.Gson
import com.mira.app.model.Comment
import com.mira.app.model.JournalEntry
import com.mira.app.model.Post
import com.mira.app.model.Room
import com.mira.app.ui.rooms.roomAccentColor
import com.mira.app.ui.theme.Gold
import com.mira.app.ui.theme.Lavender
import com.mira.app.ui.theme.Teal
import com.mira.app.ui.theme.Terracotta
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for how the app reads the JSON sent by the MIRA REST API.
 * The backend names IDs "_id", stores comment text as "comment" and
 * journal text as "entry", so these tests protect that mapping.
 */
class ModelsParsingTest {

    private val gson = Gson()

    @Test
    fun room_readsIdFromUnderscoreId() {
        val json = """{"_id":"abc123","name":"Skin & Acne"}"""
        val room = gson.fromJson(json, Room::class.java)
        assertEquals("abc123", room.roomId)
        assertEquals("Skin & Acne", room.name)
    }

    @Test
    fun post_readsIdAndUsesDefaultAliasWhenMissing() {
        val json = """{"_id":"p1","roomId":"r1","content":"Hello","meTooCount":2}"""
        val post = gson.fromJson(json, Post::class.java)
        assertEquals("p1", post.postId)
        assertEquals("Hello", post.content)
        assertEquals(2, post.meTooCount)
        assertEquals("Anonymous", post.authorSessionAlias)
    }

    @Test
    fun comment_readsTextFromCommentField() {
        val json = """{"_id":"c1","comment":"Wow, that's a milestone"}"""
        val comment = gson.fromJson(json, Comment::class.java)
        assertEquals("c1", comment.commentId)
        assertEquals("Wow, that's a milestone", comment.content)
    }

    @Test
    fun journalEntry_readsTextFromEntryField() {
        val json = """{"_id":"j1","entry":"I feel hopeful nowadays"}"""
        val entry = gson.fromJson(json, JournalEntry::class.java)
        assertEquals("j1", entry.entryId)
        assertEquals("I feel hopeful nowadays", entry.content)
    }

    @Test
    fun roomAccentColor_matchesRoomName() {
        assertEquals(Lavender, roomAccentColor(Room(name = "Anxiety & Depression")))
        assertEquals(Terracotta, roomAccentColor(Room(name = "Skin & Acne")))
        assertEquals(Teal, roomAccentColor(Room(name = "Family Issues")))
        assertEquals(Gold, roomAccentColor(Room(name = "Gambling")))
    }
}
