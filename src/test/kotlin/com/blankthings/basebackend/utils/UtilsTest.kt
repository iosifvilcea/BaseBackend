package com.blankthings.basebackend.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Base64

class UtilsTest {

    @Test
    fun `generateSecureToken returns a non-blank string`() {
        val token = Utils.generateSecureToken()
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `generateSecureToken returns a valid URL-safe Base64 string of 32 bytes`() {
        val token = Utils.generateSecureToken()
        val decoded = Base64.getUrlDecoder().decode(token)
        assertEquals(32, decoded.size)
    }

    @Test
    fun `generateSecureToken returns unique values on each call`() {
        val tokens = (1..100).map { Utils.generateSecureToken() }.toSet()
        assertEquals(100, tokens.size)
    }

    @Test
    fun `hashToken returns a 64-character hex string`() {
        val hash = Utils.hashToken("some-token")
        assertEquals(64, hash.length)
        assertTrue(hash.matches(Regex("[0-9a-f]+")))
    }

    @Test
    fun `hashToken returns the same hash for the same input`() {
        val input = "consistent-input"
        assertEquals(Utils.hashToken(input), Utils.hashToken(input))
    }

    @Test
    fun `hashToken returns different hashes for different inputs`() {
        assertNotEquals(Utils.hashToken("token-a"), Utils.hashToken("token-b"))
    }

    @Test
    fun `hashToken output does not contain the raw input`() {
        val raw = "super-secret-token"
        val hash = Utils.hashToken(raw)
        assertFalse(hash.contains(raw))
    }
}
