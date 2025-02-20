package dolphin.android.apps.dsttranslate.compose

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.textfield.TextInputLayout
import dolphin.android.apps.dsttranslate.R

// See https://stackoverflow.com/a/69148766
@Composable
fun EntrySearchView(
    items: List<String>,
    modifier: Modifier = Modifier,
    selectedValue: String? = null,
    onSelect: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    Column(modifier = modifier.background(MaterialTheme.colors.surface)) {
        AndroidView(
            factory = { context ->
                val textInputLayout = TextInputLayout.inflate(
                    context,
                    R.layout.layout_auto_complete_text_field,
                    null
                ) as TextInputLayout

                // If you need to use different styled layout for light and dark themes
                // you can create two different xml layouts one for light and another one for dark
                // and inflate the one you need here.
                val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
                (textInputLayout.editText as? AutoCompleteTextView)?.apply {
                    setAdapter(adapter)
                    setText(selectedValue ?: "", false)
                    setOnItemClickListener { _, _, index, _ -> onSelect?.invoke(items[index]) }
                }

                textInputLayout
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            update = { textInputLayout ->
                // This block will be called when recomposition happens
                val adapter =
                    ArrayAdapter(
                        textInputLayout.context,
                        android.R.layout.simple_list_item_1,
                        items
                    )
                (textInputLayout.editText as? AutoCompleteTextView)?.apply {
                    setAdapter(adapter)
                    setText(selectedValue, false)
                }
            },
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onCancel?.invoke() }) {
                Text(stringResource(id = android.R.string.cancel))
            }
            Button(onClick = { onSelect?.invoke("selected") }) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewEntrySearchViewEmpty() {
    AppTheme {
        EntrySearchView(
            items = listOf("apple", "book"),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewEntrySearchView() {
    AppTheme {
        EntrySearchView(
            items = listOf("apple", "book"),
            modifier = Modifier.fillMaxSize(),
            selectedValue = "book",
        )
    }
}
