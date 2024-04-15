package dev.snipme.highlights.model

data class PhraseLocation(val start: Int, val end: Int)

// TODO Migrate to set
data class CodeStructure(
    val marks: List<PhraseLocation>,
    val punctuations: List<PhraseLocation>,
    val keywords: List<PhraseLocation>,
    val strings: List<PhraseLocation>,
    val literals: List<PhraseLocation>,
    val comments: List<PhraseLocation>,
    val multilineComments: List<PhraseLocation>,
    val annotations: List<PhraseLocation>,
    val incremental: Boolean,
) {
    fun move(position: Int) =
        CodeStructure(
            marks = marks.map { it.copy(start = it.start + position, end = it.end + position) },
            punctuations = punctuations.map {
                it.copy(
                    start = it.start + position,
                    end = it.end + position
                )
            },
            keywords = keywords.map {
                it.copy(
                    start = it.start + position,
                    end = it.end + position
                )
            },
            strings = strings.map { it.copy(start = it.start + position, end = it.end + position) },
            literals = literals.map {
                it.copy(
                    start = it.start + position,
                    end = it.end + position
                )
            },
            comments = comments.map {
                it.copy(
                    start = it.start + position,
                    end = it.end + position
                )
            },
            multilineComments = multilineComments.map {
                it.copy(
                    start = it.start + position,
                    end = it.end + position
                )
            },
            annotations = annotations.map {
                it.copy(
                    start = it.start + position,
                    end = it.end + position
                )
            },
            incremental = true,
        )

    operator fun plus(new: CodeStructure): CodeStructure =
        CodeStructure(
            marks = marks + new.marks,
            punctuations = punctuations + new.punctuations,
            keywords = keywords + new.keywords,
            strings = strings + new.strings,
            literals = literals + new.literals,
            comments = comments + new.comments,
            multilineComments = multilineComments + new.multilineComments,
            annotations = annotations + new.annotations,
            incremental = true,
        )

    operator fun minus(new: CodeStructure): CodeStructure =
        CodeStructure(
            marks = marks - new.marks,
            punctuations = punctuations - new.punctuations,
            keywords = keywords - new.keywords,
            strings = strings - new.strings,
            literals = literals - new.literals,
            comments = comments - new.comments,
            multilineComments = multilineComments - new.multilineComments,
            annotations = annotations - new.annotations,
            incremental = true,
        )

    fun printPhrases(code: String) {
        print("marks = ${marks.join(code)}")
        print("punctuations = ${punctuations.join(code)}")
        print("keywords = ${keywords.join(code)}")
        print("strings = ${strings.join(code)}")
        print("literals = ${literals.join(code)}")
        print("comments = ${comments.join(code)}")
        print("multilineComments = ${multilineComments.join(code)}")
        print("annotations = ${annotations.join(code)}")
    }

    private fun List<PhraseLocation>.join(code: String) =
        this.map { code.substring(it.start, it.end) }.joinToString(separator = " ") + "\n"
}
