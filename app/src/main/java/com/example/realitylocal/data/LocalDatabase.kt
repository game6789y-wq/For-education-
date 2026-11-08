package com.example.realitylocal.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Private, installation-local storage. It is not connected to REALITY servers. */
class LocalDatabase(context: Context) : SQLiteOpenHelper(context, "reality_local.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE local_messages (id INTEGER PRIMARY KEY AUTOINCREMENT, body TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE local_settings (key TEXT PRIMARY KEY, value TEXT NOT NULL)")
        db.execSQL("CREATE TABLE local_profile (id INTEGER PRIMARY KEY CHECK(id=1), nickname TEXT NOT NULL, bio TEXT NOT NULL)")
        db.insert("local_profile", null, ContentValues().apply { put("id", 1); put("nickname", "My Local Profile"); put("bio", "Stored on this device") })
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }

    fun addMessage(body: String) {
        writableDatabase.insert("local_messages", null, ContentValues().apply {
            put("body", body); put("created_at", System.currentTimeMillis())
        })
    }
    fun messages(): List<String> = readableDatabase.rawQuery(
        "SELECT body FROM local_messages ORDER BY created_at DESC", null
    ).use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }

    fun profile(): Pair<String, String> = readableDatabase.rawQuery(
        "SELECT nickname, bio FROM local_profile WHERE id=1", null
    ).use { c -> if (c.moveToFirst()) c.getString(0) to c.getString(1) else "" to "" }

    fun saveProfile(nickname: String, bio: String) {
        writableDatabase.update("local_profile", ContentValues().apply {
            put("nickname", nickname); put("bio", bio)
        }, "id=1", null)
    }

    private fun putSetting(key: String, value: String) {
        writableDatabase.insertWithOnConflict("local_settings", null, ContentValues().apply {
            put("key", key); put("value", value)
        }, SQLiteDatabase.CONFLICT_REPLACE)
    }
    private fun setting(key: String): String? = readableDatabase.rawQuery(
        "SELECT value FROM local_settings WHERE key=?", arrayOf(key)
    ).use { if (it.moveToFirst()) it.getString(0) else null }

    private fun derivePinHash(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun saveAdminPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iterations = 120_000
        val digest = derivePinHash(pin, salt, iterations)
        putSetting("admin_pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
        putSetting("admin_pin_hash", Base64.encodeToString(digest, Base64.NO_WRAP))
        putSetting("admin_pin_iterations", iterations.toString())
    }

    fun hasAdminPin(): Boolean = setting("admin_pin_hash") != null &&
        setting("admin_pin_salt") != null && setting("admin_pin_iterations") != null

    fun checkAdminPin(pin: String): Boolean {
        val saltText = setting("admin_pin_salt") ?: return false
        val expected = setting("admin_pin_hash") ?: return false
        val iterations = setting("admin_pin_iterations")?.toIntOrNull() ?: return false
        if (iterations !in 100_000..1_000_000) return false
        val salt = Base64.decode(saltText, Base64.NO_WRAP)
        val actual = derivePinHash(pin, salt, iterations)
        return MessageDigest.isEqual(actual, Base64.decode(expected, Base64.NO_WRAP))
    }
}
