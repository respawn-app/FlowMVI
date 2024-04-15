package dev.snipme.highlights.model

data class SyntaxTheme(
    val key: String,
    val code: Int,
    val keyword: Int,
    val string: Int,
    val literal: Int,
    val comment: Int,
    val metadata: Int,
    val multilineComment: Int,
    val punctuation: Int,
    val mark: Int
) {
    companion object {
        fun simple(key: String, code: Int, string: Int, accent: Int, value: Int) = SyntaxTheme(
            key = key,
            code = code,
            keyword = accent,
            string = string,
            literal = value,
            comment = string,
            metadata = value,
            multilineComment = string,
            punctuation = accent,
            mark = code
        )

        fun basic(key: String, code: Int, string: Int, accent: Int, value: Int, comment: Int) =
            SyntaxTheme(
                key = key,
                code = code,
                keyword = accent,
                string = string,
                literal = value,
                comment = comment,
                metadata = code,
                multilineComment = comment,
                punctuation = accent,
                mark = code
            )
    }
}