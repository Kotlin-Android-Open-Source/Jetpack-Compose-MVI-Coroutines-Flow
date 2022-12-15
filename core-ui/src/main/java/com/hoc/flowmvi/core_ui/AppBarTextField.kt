package com.hoc.flowmvi.core_ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBarTextField(
  value: String,
  onValueChange: (String) -> Unit,
  hint: String,
  modifier: Modifier = Modifier,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
  val colors = TextFieldDefaults.textFieldColors(containerColor = Color.Unspecified)

  val textStyle = LocalTextStyle.current
  // If color is not provided via the text style, use content color as a default
  val textColor = textStyle.color.takeOrElse { MaterialTheme.colorScheme.onSurface }
  val mergedTextStyle = textStyle.merge(TextStyle(color = textColor, lineHeight = 50.sp))

  val interactionSource = remember { MutableInteractionSource() }

  // Holds the latest internal TextFieldValue state. We need to keep it to have the correct value
  // of the composition.
  // Set the correct cursor position when this composable is first initialized
  var textFieldValueState by remember {
    mutableStateOf(
      TextFieldValue(
        text = value,
        selection = TextRange(value.length),
      ),
    )
  }

  // Holds the latest TextFieldValue that BasicTextField was recomposed with. We couldn't simply
  // pass `TextFieldValue(text = value)` to the CoreTextField because we need to preserve the
  // composition.
  val textFieldValue = textFieldValueState.copy(text = value)

  SideEffect {
    if (textFieldValue.selection != textFieldValueState.selection ||
      textFieldValue.composition != textFieldValueState.composition
    ) {
      textFieldValueState = textFieldValue
    }
  }
  // Last String value that either text field was recomposed with or updated in the onValueChange
  // callback. We keep track of it to prevent calling onValueChange(String) for same String when
  // CoreTextField's onValueChange is called multiple times without recomposition in between.
  var lastTextValue by remember(value) { mutableStateOf(value) }

  // request focus when this composable is first initialized
  val focusRequester = remember { FocusRequester() }
  SideEffect { focusRequester.requestFocus() }

  CompositionLocalProvider(LocalTextSelectionColors provides LocalTextSelectionColors.current) {
    BasicTextField(
      value = textFieldValue,
      onValueChange = { newTextFieldValueState ->
        textFieldValueState = newTextFieldValueState

        val stringChangedSinceLastInvocation = lastTextValue != newTextFieldValueState.text
        lastTextValue = newTextFieldValueState.text

        if (stringChangedSinceLastInvocation) {
          // remove newlines to avoid strange layout issues, and also because singleLine=true
          onValueChange(newTextFieldValueState.text.replace("\n", ""))
        }
      },
      modifier = modifier
        .fillMaxWidth()
        .heightIn(32.dp)
        .indicatorLine(
          enabled = true,
          isError = false,
          interactionSource = interactionSource,
          colors = colors,
        )
        .focusRequester(focusRequester),
      textStyle = mergedTextStyle,
      cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
      keyboardOptions = keyboardOptions,
      keyboardActions = keyboardActions,
      interactionSource = interactionSource,
      singleLine = true,
      maxLines = 1,
      decorationBox = @Composable { innerTextField ->
        // places text field with placeholder and appropriate bottom padding
        TextFieldDefaults.TextFieldDecorationBox(
          value = value,
          visualTransformation = VisualTransformation.None,
          innerTextField = innerTextField,
          placeholder = { Text(text = hint) },
          singleLine = true,
          enabled = true,
          interactionSource = interactionSource,
          colors = colors,
          contentPadding = PaddingValues(bottom = 4.dp)
        )
      }
    )
  }
}
