package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.internal.SyntaxTokens.PUNCTUATION_CHARACTERS
import dev.snipme.highlights.internal.SyntaxTokens.TOKEN_DELIMITERS
import dev.snipme.highlights.internal.indicesOf
import dev.snipme.highlights.model.PhraseLocation

internal object PunctuationLocator {
    fun locate(code: String): List<PhraseLocation> {
        val locations = mutableSetOf<PhraseLocation>()
        code.asSequence()
            .map { it.toString().trim() }
            .filter { it in TOKEN_DELIMITERS }
            .filter { it.isNotBlank() }
            .filter { it in PUNCTUATION_CHARACTERS }
            .forEach {
                val indices = code.indicesOf(it)
                for (index in indices) {
                    if (code[index].isWhitespace()) return@forEach
                    locations.add(PhraseLocation(index, index + 1))
                }
            }

        return locations.toList()
    }
}