package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pro.respawn.flowmvi.debugger.server.ui.icons.Close
import pro.respawn.flowmvi.debugger.server.ui.icons.Icons
import pro.respawn.flowmvi.debugger.server.ui.theme.Opacity
import pro.respawn.flowmvi.debugger.server.ui.theme.RespawnTheme
import pro.respawn.flowmvi.debugger.server.ui.util.message
import pro.respawn.kmmutils.compose.bringIntoViewOnFocus
import pro.respawn.kmmutils.compose.modifier.thenIfNotNull
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.Input.Invalid
import pro.respawn.kmmutils.inputforms.ValidationError
import pro.respawn.kmmutils.inputforms.default.Forms
import pro.respawn.kmmutils.inputforms.dsl.empty
import pro.respawn.kmmutils.inputforms.dsl.input
import pro.respawn.kmmutils.inputforms.dsl.isEmptyValue
import pro.respawn.kmmutils.inputforms.dsl.isInvalid

@Composable
internal fun List<ValidationError>.toMessages() = map { it.message }

@Composable
internal fun List<ValidationError>.toReadableString() = toMessages().joinToString("\n")

@Composable
internal fun KeyboardActions.Companion.default(
    onOther: (KeyboardActionScope.() -> Unit)? = null,
) = LocalFocusManager.current.run {
    remember {
        KeyboardActions(
            onDone = { clearFocus() },
            onNext = { moveFocus(FocusDirection.Next) },
            onPrevious = { moveFocus(FocusDirection.Previous) },
            onGo = onOther,
            onSearch = onOther,
            onSend = onOther,
        )
    }
}

private const val AutoFocusDelay = 200L

@Composable
@Suppress("ComposableParametersOrdering")
fun RTextInput(
    input: Input,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    /**
     * Pass non-null if the keyboard should be shown when entering composition or when the property changes
     * Only ONE field per screen should have this as true!
     */
    focusRequester: FocusRequester? = null,
    lengthRange: IntRange? = null,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.default(),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = {
        ClearFieldIcon(
            input = input,
            onClear = { onTextChange("") }
        )
    },
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) = RTextInput(
    input = input,
    modifier = modifier,
    hint = hint,
    labelStyle = labelStyle,
    focusRequester = focusRequester
) { isFocused ->
    OutlinedTextFieldContent(
        input = input,
        onValueChange = onTextChange,
        labelStyle = labelStyle,
        isFocused = isFocused,
        lengthRange = lengthRange,
        label = label,
        placeholder = placeholder,
        singleLine = singleLine,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        maxLines = maxLines,
        minLines = minLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = colors
    )
}

@Composable
fun RTextInput(
    input: Input,
    modifier: Modifier = Modifier,
    hint: String? = null,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    /**
     * Pass non-null if the keyboard should be shown when entering composition or when the property changes
     * Only ONE field per screen should have this as true!
     */
    focusRequester: FocusRequester? = null,
    textField: @Composable (isFocused: Boolean) -> Unit,
) {
    LaunchedEffect(focusRequester) {
        if (focusRequester == null) return@LaunchedEffect
        delay(AutoFocusDelay)
        // only focus when field is empty, may indicate that the person wants to make other changes first
        if (input.isEmptyValue) focusRequester.requestFocus()
    }
    var isFocused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .bringIntoViewOnFocus()
            .thenIfNotNull(focusRequester) { focusRequester(it) }
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        textField(isFocused)
        AnimatedVisibility(
            visible = hint != null || input.isInvalid,
            modifier = Modifier
                .animateContentSize()
                .padding(vertical = 8.dp),
        ) {
            when {
                input is Invalid -> Text(
                    text = input.errors.toReadableString(),
                    style = labelStyle,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Start,
                )
                hint != null -> Text(
                    text = hint,
                    style = labelStyle,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

@Composable
fun ClearFieldIcon(
    input: Input,
    onClear: () -> Unit,
    enabled: Boolean = true,
    hideKeyboard: Boolean = true,
) = AnimatedVisibility(visible = !input.isEmptyValue) {
    val focusManager = LocalFocusManager.current
    RIcon(
        icon = Icons.Close,
        onClick = {
            focusManager.clearFocus(hideKeyboard)
            onClear()
        },
        modifier = Modifier.requiredSize(22.dp),
        enabled = !input.isEmptyValue && enabled,
    )
}

@Composable
private fun LabelWithCounter(
    lengthRange: IntRange?,
    label: String?,
    inputLength: Int,
    textStyle: TextStyle,
    isFocused: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.animateContentSize()
    ) {
        AnimatedVisibility(
            visible = label != null,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            Crossfade(label) {
                Text(
                    text = it ?: return@Crossfade,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        AnimatedVisibility(lengthRange != null && isFocused) {
            if (lengthRange == null) return@AnimatedVisibility
            val color = if (inputLength > lengthRange.last)
                MaterialTheme.colorScheme.error
            else
                LocalContentColor.current
            Text(
                text = "($inputLength/${lengthRange.last})",
                color = color,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = textStyle,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Suppress("ComposableParametersOrdering")
@Composable
fun OutlinedTextFieldContent(
    input: Input,
    onValueChange: (String) -> Unit,
    labelStyle: TextStyle,
    isFocused: Boolean,
    lengthRange: IntRange? = null,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.default(),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = input.value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minWidth = 400.dp),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        label = { LabelWithCounter(lengthRange, label, input.value.length, labelStyle, isFocused) },
        placeholder = placeholder?.let { value ->
            {
                Crossfade(targetState = value) {
                    Text(
                        text = it,
                        style = textStyle,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = true,
                        modifier = Modifier.alpha(Opacity.disabled)
                    )
                }
            }
        },
        singleLine = singleLine,
        isError = input is Invalid,
        readOnly = readOnly,
        colors = colors,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        maxLines = maxLines,
        minLines = minLines,
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
@Preview
private fun RTextInputPreview() = RespawnTheme {
    var text by remember { mutableStateOf("support@respawn...") }

    Column {
        RTextInput(
            input = Forms.Email().invoke(text),
            onTextChange = { text = it },
            lengthRange = 3..55,
            label = "blah",
            placeholder = "SomePlaceHolder",
        )

        RTextInput(
            input = text.input(),
            onTextChange = { text = it },
            lengthRange = 3..55,
            label = "blah",
            placeholder = "SomePlaceHolder",
            hint = "Hint",
        )

        RTextInput(
            input = empty(),
            onTextChange = { },
            lengthRange = 3..55,
            label = "blah",
            hint = "hint",
            placeholder = "SomePlaceHolder",
            leadingIcon = { RIcon(Icons.Close) }
        )
    }
}
