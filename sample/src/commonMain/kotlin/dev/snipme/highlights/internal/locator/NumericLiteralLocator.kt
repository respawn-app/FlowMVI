package dev.snipme.highlights.internal.locator

import dev.snipme.highlights.internal.SyntaxTokens.TOKEN_DELIMITERS
import dev.snipme.highlights.internal.indicesOf
import dev.snipme.highlights.model.PhraseLocation

private val NUMBER_START_CHARACTERS = listOf('-', '.')
private val NUMBER_TYPE_CHARACTERS = listOf('e', 'u', 'f', 'l')
private val HEX_NUMBER_CHARACTERS = listOf('a', 'b', 'c', 'd', 'e', 'f')
private val NUMBER_SPECIAL_CHARACTERS = listOf('_')

internal object NumericLiteralLocator {

    fun locate(code: String): List<PhraseLocation> {
        return findDigitIndices(code)
    }

    private fun findDigitIndices(code: String): List<PhraseLocation> {
        val foundPhrases = mutableSetOf<String>()
        val locations = mutableSetOf<PhraseLocation>()

        val delimiters = TOKEN_DELIMITERS.filterNot { it == "." }.toTypedArray()

        code.split(*delimiters) // Separate words
            .asSequence() // Manipulate on given word separately
            .filterNot { foundPhrases.contains(it) }
            .filter { it.isNotBlank() } // Filter spaces and others
            .filter {
                it.first().isDigit() || (NUMBER_START_CHARACTERS.contains(it.first())
                        && it.getOrNull(1)?.isDigit() == true)
            } // Find start of literals
            .forEach { number ->
                // For given literal find all occurrences
                val indices = code.indicesOf(number)
                for (startIndex in indices) {
                    // TODO Correct this and publish
                    if (code.isFullNumber(number, startIndex).not()) return@forEach
                    // Omit in the middle of text, probably variable name (this100)
                    if (code.isNumberFirstIndex(startIndex).not()) return@forEach
                    // Add matching occurrence to the output locations
                    val length = calculateNumberLength(number.lowercase())
                    locations.add(PhraseLocation(startIndex, startIndex + length))
                }

                foundPhrases.add(number)
            }

        return locations.toList()
    }

    // Returns if given index is the beginning of word (there is no letter before)
    private fun String.isNumberFirstIndex(index: Int): Boolean {
        if (index < 0) return false
        if (index == 0) return true
        val positionBefore = maxOf(index - 1, 0)
        val charBefore = getOrNull(positionBefore) ?: return false

        return TOKEN_DELIMITERS.contains(charBefore.toString())
    }

    private fun String.isFullNumber(number: String, startIndex: Int): Boolean {
        val numberEndingIndex = startIndex + number.length
        if (numberEndingIndex >= lastIndex) return true
        val numberEnding = getOrNull(numberEndingIndex) ?: return false

        return TOKEN_DELIMITERS.contains(numberEnding.toString())
    }

    private fun calculateNumberLength(number: String): Int {
        val letters = number.filter { it.isLetter() }

        if (number.startsWith("0x")) {
            return getLengthOfSubstringFor(number) {
                it.isDigit() || HEX_NUMBER_CHARACTERS.contains(it)
            }
        }

        if (number.contains("0b")) {
            return getLengthOfSubstringFor(number) {
                it == '0' || it == '1'
            }
        }

        // Highlight only 4f when e.g. number is like 4fff
        if (NUMBER_TYPE_CHARACTERS.any { letters.contains(it) }) {
            var length = 1 // Single letter
            length += number.count { it.isDigit() }
            length += number.count { NUMBER_START_CHARACTERS.contains(it) }
            length += number.count { NUMBER_SPECIAL_CHARACTERS.contains(it) }
            if ("e+" in number) length++
            return length
        }

        return number.filter {
            it.isDigit() ||
                    NUMBER_START_CHARACTERS.contains(it) ||
                    NUMBER_TYPE_CHARACTERS.contains(it) ||
                    NUMBER_SPECIAL_CHARACTERS.contains(it)

        }.length
    }

    private fun getLengthOfSubstringFor(number: String, condition: (Char) -> Boolean): Int {
        var hexSequenceLength = 2
        run loop@{
            number.substring(startIndex = hexSequenceLength).forEach {
                if (condition(it)) {
                    hexSequenceLength++
                } else {
                    return@loop
                }
            }
        }

        return hexSequenceLength
    }
}