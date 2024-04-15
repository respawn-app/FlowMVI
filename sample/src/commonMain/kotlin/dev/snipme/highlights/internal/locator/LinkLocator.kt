package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.internal.SyntaxTokens.TOKEN_DELIMITERS
import dev.snipme.highlights.model.PhraseLocation

private val EXCLUDED_URL_CHARACTERS = listOf(":", ".", "=")

internal object LinkLocator {
    fun locate(code: String): List<PhraseLocation> =
        code.split(*TOKEN_DELIMITERS.minus(EXCLUDED_URL_CHARACTERS).toTypedArray())
            .filter { isUrl(it) }
            .map {
                val start = code.indexOf(it)
                val end = start + it.length
                PhraseLocation(start, end)
            }

    private fun isUrl(phrase: String): Boolean =
        phrase.startsWith("http://") || phrase.startsWith("https://")
}