package pro.respawn.flowmvi.debugger.server.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pro.respawn.kmmutils.inputforms.Input
import pro.respawn.kmmutils.inputforms.ValidationError
import pro.respawn.kmmutils.inputforms.dsl.isEmptyValue

// TODO: Implement normal error messages
@Composable
internal fun List<ValidationError>.toMessages() = map { it::class.simpleName }

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

@Composable
internal fun RTextInput(
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
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions: KeyboardActions = KeyboardActions.default(),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = { ClearFieldIcon(input, { onTextChange("") }) },
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
) {
    Column(
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = input.value,
            onValueChange = onTextChange,
            modifier = Modifier.defaultMinSize(minHeight = 40.dp, minWidth = 400.dp),
            enabled = enabled,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            textStyle = textStyle,
            visualTransformation = visualTransformation,
            label = {
                label?.let {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
            },
            placeholder = {
                placeholder?.let {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                }
            },
            singleLine = singleLine,
            isError = input is Input.Invalid,
            readOnly = readOnly,
            colors = colors,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            maxLines = maxLines,
            minLines = minLines,
            shape = MaterialTheme.shapes.large,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.animateContentSize(),
                contentAlignment = Alignment.CenterStart,
            ) {
                // errors
                this@Column.AnimatedVisibility(visible = input is Input.Invalid) {
                    if (input !is Input.Invalid) return@AnimatedVisibility
                    Text(
                        text = input.errors.toReadableString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .animateContentSize()
                            .padding(top = 4.dp, bottom = 4.dp, end = 4.dp),
                    )
                }

                // hint
                this@Column.AnimatedVisibility(
                    visible = input !is Input.Invalid && hint != null,
                ) {
                    Text(
                        text = hint ?: return@AnimatedVisibility,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .animateContentSize()
                            .padding(top = 4.dp, bottom = 4.dp, end = 4.dp),
                    )
                }
            }

            // length range
            AnimatedVisibility(visible = lengthRange != null) {
                Text(
                    text = "${input.value.length}/${lengthRange?.last ?: 0}",
                    color = if (input is Input.Invalid) MaterialTheme.colorScheme.error else LocalContentColor.current,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .animateContentSize()
                        .padding(top = 4.dp, bottom = 4.dp, start = 4.dp),
                )
            }
        }
    }
}

@Composable
internal fun ClearFieldIcon(
    input: Input,
    onClear: () -> Unit,
    enabled: Boolean = true,
) {
    AnimatedVisibility(visible = !input.isEmptyValue) {
        IconButton(
            onClick = onClear,
            enabled = !input.isEmptyValue && enabled,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
