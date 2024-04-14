package pro.respawn.flowmvi.sample.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import pro.respawn.flowmvi.sample.util.bringIntoViewOnFocus
import pro.respawn.flowmvi.sample.util.default
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.Input.Invalid
import pro.respawn.kmmutils.inputforms.dsl.isEmptyValue
import pro.respawn.kmmutils.inputforms.dsl.isInvalid

private const val AutoFocusDelay = 200L

@Composable
fun RTextInput(
    input: Input,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    lengthRange: IntRange? = null,
    label: String? = null,
    placeholder: String? = null,
    hint: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    /**
     * Whether a keyboard should be shown when entering composition or when the property changes
     * Only ONE field per screen should have this as true!
     */
    autoFocus: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.default(),
    focusRequester: FocusRequester = remember { FocusRequester() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = { ClearFieldIcon(input, { onTextChange("") }) },
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    LaunchedEffect(autoFocus) {
        if (!autoFocus) return@LaunchedEffect
        delay(AutoFocusDelay)
        focusRequester.requestFocus()
    }
    var isFocused by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .bringIntoViewOnFocus()
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        OutlinedTextField(
            value = input.value,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .bringIntoViewOnFocus(),
            enabled = enabled,
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
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
        AnimatedVisibility(
            visible = hint != null || input.isInvalid,
            modifier = Modifier
                .animateContentSize()
                .padding(vertical = 8.dp),
        ) {
            when {
                input is Invalid -> Text(
                    text = input.errors.joinToString("\n") { requireNotNull(it::class.simpleName) },
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
) {
    AnimatedVisibility(visible = !input.isEmptyValue) {
        RIcon(
            icon = Icons.Rounded.Close,
            onClick = onClear,
            enabled = !input.isEmptyValue && enabled,
            size = 16.dp,
        )
    }
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
