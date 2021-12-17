import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private enum class SearchType {
    StartWith, Contains,
}

// See https://stackoverflow.com/a/69148766
@Composable
fun SearchPane(
    items: List<String>,
    modifier: Modifier = Modifier,
    selectedValue: String? = null,
    onSelect: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    var data by remember { mutableStateOf(selectedValue) }
    var filteredItems by remember { mutableStateOf(emptyList<String>()) }
    var searchType by remember { mutableStateOf(SearchType.Contains) }

    Column(modifier = modifier.background(MaterialTheme.colors.surface)) {
        TextField(
            data ?: "",
            onValueChange = { text ->
                data = text
                filteredItems = items.filter { item ->
                    when (searchType) {
                        SearchType.StartWith -> item.startsWith(text, ignoreCase = true)
                        SearchType.Contains -> item.contains(text, ignoreCase = true)
                    }
                }
                println("found ${filteredItems.size}")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            placeholder = { Text("resource key") }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            SearchType.values().forEach { type ->
                RadioButton(selected = type == searchType, onClick = { searchType = type })
                Text(
                    type.name,
                    modifier = Modifier.clickable { searchType = type }.padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        LazyScrollableColumn(filteredItems, modifier = Modifier.weight(1f)) { _, text ->
            TextButton(onClick = { data = text }) {
                Text(text, modifier = Modifier.fillMaxWidth())
            }
        }
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onCancel?.invoke() }, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(onClick = { onSelect?.invoke(data ?: "") }, modifier = Modifier.weight(1f)) {
                Text("Edit")
            }
        }
    }
}
