package com.mira.app.data

/**
 * Local SQLite copy of the user's journal, used for offline mode.
 * Entries written while offline are stored with synced = 0 and are sent
 * to the server later by JournalScreen. Entries that came from the server
 * are stored with synced = 1. See: https://developer.android.com/training/data-storage/sqlite
 */

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mira.app.model.JournalEntry

class JournalDb(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "mira_journal.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE journal (" +
                "local_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "server_id TEXT, " +
                "content TEXT NOT NULL, " +
                "created_at TEXT NOT NULL, " +
                "synced INTEGER NOT NULL DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS journal")
        onCreate(db)
    }

    /** Every entry on the phone, newest first. Unsynced ones get an id starting with "local-". */
    fun getAll(): List<JournalEntry> {
        val list = mutableListOf<JournalEntry>()
        readableDatabase.rawQuery(
            "SELECT local_id, server_id, content, created_at, synced FROM journal ORDER BY created_at DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val synced = c.getInt(4) == 1
                val id = if (synced) (c.getString(1) ?: "") else "local-" + c.getLong(0)
                list.add(JournalEntry(entryId = id, content = c.getString(2), createdAt = c.getString(3)))
            }
        }
        return list
    }

    /** Saves a new entry that has not reached the server yet. */
    fun addPending(content: String, createdAt: String) {
        val values = ContentValues().apply {
            put("content", content)
            put("created_at", createdAt)
            put("synced", 0)
        }
        writableDatabase.insert("journal", null, values)
    }

    /** Entries still waiting to be sent, oldest first, as (local id, text). */
    fun getPending(): List<Pair<Long, String>> {
        val list = mutableListOf<Pair<Long, String>>()
        readableDatabase.rawQuery(
            "SELECT local_id, content FROM journal WHERE synced = 0 ORDER BY created_at ASC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                list.add(Pair(c.getLong(0), c.getString(1)))
            }
        }
        return list
    }

    /** Marks a pending entry as sent, using the id and time the server gave it. */
    fun markSynced(localId: Long, serverId: String, createdAt: String) {
        val values = ContentValues().apply {
            put("server_id", serverId)
            put("created_at", createdAt)
            put("synced", 1)
        }
        writableDatabase.update("journal", values, "local_id = ?", arrayOf(localId.toString()))
    }

    /** Replaces the synced copies with the latest list from the server. Pending entries are kept. */
    fun replaceWithServer(entries: List<JournalEntry>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("journal", "synced = 1", null)
            for (e in entries) {
                val values = ContentValues().apply {
                    put("server_id", e.entryId)
                    put("content", e.content)
                    put("created_at", e.createdAt)
                    put("synced", 1)
                }
                db.insert("journal", null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Wipes the local journal. Called on logout so the next user never sees it. */
    fun clearAll() {
        writableDatabase.delete("journal", null, null)
    }
}
