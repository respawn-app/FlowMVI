package pro.respawn.flowmvi.logging

private const val MaxLogcatMessageLength = 4000
private const val MinLogcatMessageLength = 3000

/**
 * Credits to Ktor for the implementation
 */
internal tailrec fun chunkedLog(
    message: String,
    maxLength: Int = MaxLogcatMessageLength,
    minLength: Int = MinLogcatMessageLength,
    delegate: (String) -> Unit
) {
    // String to be logged is longer than the max...
    if (message.length > maxLength) {
        var msgSubstring = message.substring(0, maxLength)
        var msgSubstringEndIndex = maxLength

        // Try to find a substring break at a newline char.
        msgSubstring.lastIndexOf('\n').let { lastIndex ->
            if (lastIndex >= minLength) {
                msgSubstring = msgSubstring.substring(0, lastIndex)
                // skip over new line char
                msgSubstringEndIndex = lastIndex + 1
            }
        }

        // Log the substring.
        delegate(msgSubstring)

        // Recursively log the remainder.
        chunkedLog(message.substring(msgSubstringEndIndex), maxLength, minLength, delegate)
    } else {
        delegate(message)
    } // String to be logged is shorter than the max...
}
