package com.example.smarttrash.util

import com.example.smarttrash.data.util.levenshteinDistance
import com.example.smarttrash.data.util.levenshteinSimilarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevenshteinTest {

    @Test
    fun `identical strings have distance 0`() {
        assertEquals(0, "battery".levenshteinDistance("battery"))
    }

    @Test
    fun `empty string distance equals other string length`() {
        assertEquals(7, "".levenshteinDistance("battery"))
        assertEquals(7, "battery".levenshteinDistance(""))
    }

    @Test
    fun `battry vs battery distance is 1`() {
        assertEquals(1, "battry".levenshteinDistance("battery"))
    }

    @Test
    fun `plastc vs plastic distance is 1`() {
        assertEquals(1, "plastc".levenshteinDistance("plastic"))
    }

    @Test
    fun `completely different strings have high distance`() {
        val dist = "xyz".levenshteinDistance("battery")
        assertTrue(dist > 4)
    }

    @Test
    fun `identical strings have similarity 1_0`() {
        assertEquals(1.0, "battery".levenshteinSimilarity("battery"), 0.001)
    }

    @Test
    fun `battry vs battery similarity above threshold`() {
        val similarity = "battry".levenshteinSimilarity("battery")
        assertTrue("Expected > 0.6, got $similarity", similarity > 0.6)
    }

    @Test
    fun `glass vs glss similarity above threshold`() {
        val similarity = "glss".levenshteinSimilarity("glass")
        assertTrue("Expected > 0.6, got $similarity", similarity > 0.6)
    }

    @Test
    fun `completely different words have low similarity`() {
        val similarity = "xyz".levenshteinSimilarity("battery")
        assertTrue("Expected < 0.4, got $similarity", similarity < 0.4)
    }

    @Test
    fun `case sensitive check`() {
        val lower = "battery".levenshteinSimilarity("battery")
        val mixed = "Battery".levenshteinSimilarity("battery")
        assertTrue(lower > mixed)
    }
}