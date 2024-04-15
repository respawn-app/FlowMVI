package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.model.PhraseLocation
import dev.snipme.highlights.internal.SyntaxTokens
import dev.snipme.highlights.internal.indicesOf
import dev.snipme.highlights.internal.isIndependentPhrase

internal object TokenLocator {
    fun locate(code: String): List<PhraseLocation> {
        val locations = mutableSetOf<PhraseLocation>()
        code.split(*SyntaxTokens.TOKEN_DELIMITERS.toTypedArray()) // Separate words
            .asSequence() // Manipulate on given word separately
            .filter { it.isNotBlank() } // Filter spaces and others
            .forEach { token ->
                code.indicesOf(token)
                    .filter { token.isIndependentPhrase(code, it) }
                    .forEach { startIndex ->
                        locations.add(PhraseLocation(startIndex, startIndex + token.length))
                    }
            }

        return locations.toList()
    }
}