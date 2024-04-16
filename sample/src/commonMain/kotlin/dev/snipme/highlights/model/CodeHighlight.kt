package dev.snipme.highlights.model

sealed class CodeHighlight(open val location: PhraseLocation)
data class BoldHighlight(override val location: PhraseLocation) : CodeHighlight(location)
data class ColorHighlight(
    override val location: PhraseLocation,
    val rgb: Int
) : CodeHighlight(location)
