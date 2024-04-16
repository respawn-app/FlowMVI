package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.internal.SyntaxTokens.TOKEN_DELIMITERS
import dev.snipme.highlights.internal.indicesOf
import dev.snipme.highlights.model.PhraseLocation

internal object AnnotationLocator {

    fun locate(code: String): List<PhraseLocation> {
        val foundAnnotations = emptyList<String>()
        val locations = mutableSetOf<PhraseLocation>()
        code.split(delimiters = TOKEN_DELIMITERS.toTypedArray())
            .asSequence()
            .filter { it.isNotEmpty() }
            .filter { foundAnnotations.contains(it).not() }
            .filter { it.contains('@') }
            .forEach { annotation ->
                code.indicesOf(annotation).forEach { phraseStartIndex ->
                    val symbolLocation = annotation.indexOf('@')
                    val startIndex = phraseStartIndex + symbolLocation

                    locations.add(
                        PhraseLocation(
                            startIndex,
                            startIndex + annotation.length - symbolLocation
                        )
                    )
                }
            }

        return locations.toList()
    }
}
