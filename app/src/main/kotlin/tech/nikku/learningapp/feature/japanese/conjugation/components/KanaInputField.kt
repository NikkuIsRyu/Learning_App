package tech.nikku.learningapp.feature.japanese.conjugation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import tech.nikku.learningapp.core.domain.engine.text.RomajiKana

@Composable
fun KanaInputField(
  value: String,
  onValueChange: (String) -> Unit,
  onSubmit: (String) -> Unit,
  enabled: Boolean = true,
  placeholder: String = "Type in kana or romaji (e.g. kaita)",
  modifier: Modifier = Modifier
) {
  var textFieldValue by remember(value) {
    mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
  }

  OutlinedTextField(
    value = textFieldValue,
    onValueChange = { newValue ->
      // If IME is actively composing (e.g. Japanese IME), do not interfere with live romaji conversion
      if (newValue.composition != null) {
        textFieldValue = newValue
        onValueChange(newValue.text)
      } else {
        // No active composition: perform live romaji -> kana conversion if latin characters are typed
        val converted = if (newValue.text.any { it in 'a'..'z' || it in 'A'..'Z' }) {
          RomajiKana.romajiToKana(newValue.text)
        } else {
          newValue.text
        }
        val diffLen = converted.length - newValue.text.length
        val newSelection = TextRange((newValue.selection.end + diffLen).coerceIn(0, converted.length))
        val updated = TextFieldValue(text = converted, selection = newSelection)
        textFieldValue = updated
        onValueChange(converted)
      }
    },
    enabled = enabled,
    singleLine = true,
    placeholder = { Text(placeholder) },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        // Guardrail: submit only when composition is finished
        if (textFieldValue.composition == null && textFieldValue.text.isNotBlank()) {
          onSubmit(textFieldValue.text)
        }
      }
    ),
    modifier = modifier.fillMaxWidth()
  )
}
