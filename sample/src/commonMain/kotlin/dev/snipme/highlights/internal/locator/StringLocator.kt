package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.model.PhraseLocation
import dev.snipme.highlights.internal.SyntaxTokens.STRING_DELIMITERS
import dev.snipme.highlights.internal.indicesOf

private const val START_INDEX = 0
private const val TWO_ELEMENTS = 2
private const val QUOTE_ENDING_POSITION = 1

internal object StringLocator {

    fun locate(code: String): List<PhraseLocation> = findStrings(code)

    private fun findStrings(code: String): List<PhraseLocation> {
        val locations = mutableListOf<PhraseLocation>()

        // Find index of each string delimiter like " or ' or """
        STRING_DELIMITERS.forEach {
            val textIndices = mutableListOf<Int>()
            textIndices += code.indicesOf(it)

            // For given indices find words between
            for (i in START_INDEX..textIndices.lastIndex step TWO_ELEMENTS) {
                if (textIndices.getOrNull(i + 1) != null) {
                    locations.add(
                        PhraseLocation(
                            textIndices[i],
                            textIndices[i + 1] + QUOTE_ENDING_POSITION
                        )
                    )
                }
            }
        }

        return locations
    }
}