package com.ahmanpg.beitracker.util

import java.security.MessageDigest

object HashUtils {
    /**
     * Generates a stable MD5 hash for a given string.
     * Useful for creating Firestore document IDs from URLs.
     */
    fun md5(input: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
