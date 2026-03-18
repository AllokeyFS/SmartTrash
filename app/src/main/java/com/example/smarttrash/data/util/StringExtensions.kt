package com.example.smarttrash.data.util

fun String.levenshteinDistance(other: String): Int {
    if (this.isEmpty()) return other.length
    if (other.isEmpty()) return this.length
    if (this == other) return 0

    val shorter = if (this.length <= other.length) this else other
    val longer  = if (this.length <= other.length) other else this

    var previousRow = IntArray(shorter.length + 1) { it }
    var currentRow  = IntArray(shorter.length + 1)

    for (i in 1..longer.length) {
        currentRow[0] = i
        for (j in 1..shorter.length) {
            val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
            currentRow[j] = minOf(
                currentRow[j - 1] + 1,
                previousRow[j] + 1,
                previousRow[j - 1] + cost
            )
        }
        val temp = previousRow
        previousRow = currentRow
        currentRow = temp
    }

    return previousRow[shorter.length]
}

fun String.levenshteinSimilarity(other: String): Double {
    if (this.isEmpty() && other.isEmpty()) return 1.0
    val maxLength = maxOf(this.length, other.length)
    if (maxLength == 0) return 1.0
    return 1.0 - (this.levenshteinDistance(other).toDouble() / maxLength)
}