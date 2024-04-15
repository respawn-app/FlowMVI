package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.internal.SyntaxTokens.TOKEN_DELIMITERS
import dev.snipme.highlights.internal.indicesOf
import dev.snipme.highlights.internal.isIndependentPhrase
import dev.snipme.highlights.model.PhraseLocation

internal object KeywordLocator {

    fun locate(
        code: String,
        keywords: List<String>,
        ignoreRanges: List<PhraseLocation> = emptyList(),
    ): List<PhraseLocation> {
        val locations = mutableListOf<PhraseLocation>()
        val foundKeywords = findKeywords(code, keywords)

        val interpretedKeywords = foundKeywords.filterNot { keyword ->
            val index = code.indexOf(keyword)
            val length = keyword.length
            ignoreRanges.any { it.start <= index && it.end >= index + length }
        }

        interpretedKeywords.forEach { keyword ->
            val indices = code
                .indicesOf(keyword)
                .filter { keyword.isIndependentPhrase(code, it) }

            indices.forEach { index ->
                locations.add(PhraseLocation(index, index + keyword.length))
            }
        }

        return locations.toList()
    }

    private fun findKeywords(code: String, keywords: List<String>): Set<String> =
        TOKEN_DELIMITERS.toTypedArray().let { delimiters ->
            code.split(*delimiters, ignoreCase = true) // Split into words
                .asSequence() // Reduce amount of operations
                .filter { it.isNotBlank() } // Remove empty
                .map { it.trim() } // Remove whitespaces from phrase
                .map { it.lowercase() } // Standardize
                .filter { it in keywords } // Get supported
                .toSet() // Filter duplicates
        }
}