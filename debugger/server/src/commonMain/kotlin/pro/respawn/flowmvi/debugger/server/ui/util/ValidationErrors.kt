package pro.respawn.flowmvi.debugger.server.ui.util

import androidx.compose.runtime.Composable
import pro.respawn.flowmvi.server.generated.resources.contains_digits_error_message
import pro.respawn.flowmvi.server.generated.resources.contains_error_message
import pro.respawn.flowmvi.server.generated.resources.contains_letters_error_message
import pro.respawn.flowmvi.server.generated.resources.did_not_contain_error_template
import pro.respawn.flowmvi.server.generated.resources.does_not_end_with_error_template
import pro.respawn.flowmvi.server.generated.resources.empty_input_error_message
import pro.respawn.flowmvi.server.generated.resources.has_linebreaks_error_message
import pro.respawn.flowmvi.server.generated.resources.has_no_digits_error_message
import pro.respawn.flowmvi.server.generated.resources.has_no_letters_error_message
import pro.respawn.flowmvi.server.generated.resources.has_whitespace_error_message
import pro.respawn.flowmvi.server.generated.resources.inputs_dont_match_error_message
import pro.respawn.flowmvi.server.generated.resources.invalid_value_error_message
import pro.respawn.flowmvi.server.generated.resources.length_is_not_exactly_error_template
import pro.respawn.flowmvi.server.generated.resources.max_length_error_template
import pro.respawn.flowmvi.server.generated.resources.no_uppercase_letters_error_message
import pro.respawn.flowmvi.server.generated.resources.not_alphanumeric_error_message
import pro.respawn.flowmvi.server.generated.resources.not_ascii_error_message
import pro.respawn.flowmvi.server.generated.resources.not_digits_only_error_message
import pro.respawn.flowmvi.server.generated.resources.not_letters_only_error_message
import pro.respawn.flowmvi.server.generated.resources.not_lowercase_error_message
import pro.respawn.flowmvi.server.generated.resources.not_uppercase_error_message
import pro.respawn.flowmvi.server.generated.resources.start_with_error
import pro.respawn.flowmvi.server.generated.resources.too_long_error_template
import pro.respawn.flowmvi.server.generated.resources.too_short_error_template
import pro.respawn.kmmutils.compose.resources.string
import pro.respawn.kmmutils.inputforms.ValidationError
import pro.respawn.kmmutils.inputforms.ValidationError.Contains
import pro.respawn.kmmutils.inputforms.ValidationError.ContainsDigits
import pro.respawn.kmmutils.inputforms.ValidationError.ContainsLetters
import pro.respawn.kmmutils.inputforms.ValidationError.DoesNotContain
import pro.respawn.kmmutils.inputforms.ValidationError.DoesNotEndWith
import pro.respawn.kmmutils.inputforms.ValidationError.DoesNotMatch
import pro.respawn.kmmutils.inputforms.ValidationError.DoesNotStartWith
import pro.respawn.kmmutils.inputforms.ValidationError.Empty
import pro.respawn.kmmutils.inputforms.ValidationError.Generic
import pro.respawn.kmmutils.inputforms.ValidationError.HasNoDigits
import pro.respawn.kmmutils.inputforms.ValidationError.HasNoLetters
import pro.respawn.kmmutils.inputforms.ValidationError.HasWhitespace
import pro.respawn.kmmutils.inputforms.ValidationError.IsNotEqual
import pro.respawn.kmmutils.inputforms.ValidationError.LengthIsNotExactly
import pro.respawn.kmmutils.inputforms.ValidationError.NoUppercaseLetters
import pro.respawn.kmmutils.inputforms.ValidationError.NotAlphaNumeric
import pro.respawn.kmmutils.inputforms.ValidationError.NotAscii
import pro.respawn.kmmutils.inputforms.ValidationError.NotDigitsOnly
import pro.respawn.kmmutils.inputforms.ValidationError.NotInRange
import pro.respawn.kmmutils.inputforms.ValidationError.NotLettersOnly
import pro.respawn.kmmutils.inputforms.ValidationError.NotLowercase
import pro.respawn.kmmutils.inputforms.ValidationError.NotSingleline
import pro.respawn.kmmutils.inputforms.ValidationError.NotUppercase
import pro.respawn.kmmutils.inputforms.ValidationError.TooLong
import pro.respawn.kmmutils.inputforms.ValidationError.TooShort
import pro.respawn.flowmvi.server.generated.resources.Res as R

val ValidationError.message
    @Composable get() = when (this) {
        is DoesNotContain -> R.string.did_not_contain_error_template.string(needle)
        is NotInRange -> R.string.max_length_error_template.string(range.first, range.last)
        is DoesNotStartWith -> R.string.start_with_error.string(prefix)
        is IsNotEqual -> R.string.inputs_dont_match_error_message.string()
        is ContainsDigits -> R.string.contains_digits_error_message.string()
        is ContainsLetters -> R.string.contains_letters_error_message.string()
        is DoesNotEndWith -> R.string.does_not_end_with_error_template.string(suffix)
        is DoesNotMatch, is Generic -> R.string.invalid_value_error_message.string()
        is Empty -> R.string.empty_input_error_message.string()
        is HasNoDigits -> R.string.has_no_digits_error_message.string()
        is HasNoLetters -> R.string.has_no_letters_error_message.string()
        is HasWhitespace -> R.string.has_whitespace_error_message.string()
        is NotAlphaNumeric -> R.string.not_alphanumeric_error_message.string()
        is NotAscii -> R.string.not_ascii_error_message.string()
        is NotDigitsOnly -> R.string.not_digits_only_error_message.string()
        is NotLettersOnly -> R.string.not_letters_only_error_message.string()
        is NotUppercase -> R.string.not_uppercase_error_message.string()
        is NotLowercase -> R.string.not_lowercase_error_message.string()
        is NotSingleline -> R.string.has_linebreaks_error_message.string()
        is TooLong -> R.string.too_long_error_template.string(maxLength)
        is TooShort -> R.string.too_short_error_template.string(minLength)
        is LengthIsNotExactly -> R.string.length_is_not_exactly_error_template.string(length)
        is NoUppercaseLetters -> R.string.no_uppercase_letters_error_message.string()
        is Contains -> R.string.contains_error_message.string(needle)
    }
