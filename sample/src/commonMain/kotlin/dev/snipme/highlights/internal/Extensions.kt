package dev.snipme.highlights.internal

fun String.indicesOf(
    phrase: String,
): Set<Int> {
    val indices = mutableSetOf<Int>()

    // No found
    val startIndexOf = indexOf(phrase, 0)
    if (startIndexOf < 0) {
        return emptySet()
    }

    indices.add(startIndexOf)

    // The found is the only one
    if (startIndexOf == (lastIndex - phrase.length)) {
        return indices
    }

    var startingIndex = indexOf(phrase, startIndexOf + phrase.length)

    while (startingIndex > 0) {
        indices.add(startingIndex)
        startingIndex = indexOf(phrase, startingIndex + phrase.length)
    }

    return indices
}

fun Char.isNewLine(): Boolean {
    val stringChar = this.toString()
    return stringChar == "\n" || stringChar == "\r" || stringChar == "\r\n"
}

fun String.lengthToEOF(start: Int = 0): Int {
    if (all { it.isNewLine().not() }) return length - start
    var endIndex = start
    while (this.getOrNull(endIndex)?.isNewLine()?.not() == true) {
        endIndex++
    }
    return endIndex - start
}

// TODO Create unit tests for this
// Sometimes keyword can be found in the middle of word.
// This returns information if index points only to the keyword
fun String.isIndependentPhrase(
    code: String,
    index: Int,
): Boolean {
    if (index == 0) return true
    if (index == code.lastIndex) return true

    val charBefore = code[maxOf(index - 1, 0)]
    val charAfter = code[minOf(index + this.length, code.lastIndex)]

    return charBefore.isLetter().not() && charAfter.isDigit().not()
}