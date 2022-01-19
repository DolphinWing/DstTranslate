package dolphin.desktop.apps.dsttranslate.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dolphin.android.apps.dsttranslate.WordEntry

@Composable
fun ToolbarPane(
    modifier: Modifier = Modifier,
    filteredList: List<WordEntry>? = null,
    changedList: List<Long>? = null,
    onRefresh: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colors.primary)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val changed = changedList?.filter { it > 0L } ?: arrayListOf()
        Text(
            "all: ${filteredList?.size ?: 0}, changed: ${changed.size}",
            modifier = Modifier.weight(1f),
            fontSize = AppTheme.largerFontSize(),
            color = MaterialTheme.colors.onPrimary,
        )
        Text(
            "chs",
            modifier = Modifier
                .background(AppTheme.AppColor.blue)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            "cht",
            modifier = Modifier
                .background(AppTheme.AppColor.purple)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            fontSize = AppTheme.largerFontSize(),
            color = Color.White,
        )
        Text(
            "dst",
            modifier = Modifier
                .background(AppTheme.AppColor.orange)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            "now",
            modifier = Modifier
                .background(AppTheme.AppColor.green)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            fontSize = AppTheme.largerFontSize(),
        )
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        IconButton(onClick = { onRefresh?.invoke() }, enabled = enabled) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
        }
        IconButton(onClick = { onSearch?.invoke() }, enabled = enabled) {
            Icon(Icons.Rounded.Search, contentDescription = null)
        }
        IconButton(onClick = { onSave?.invoke() }, enabled = enabled) {
            Icon(Icons.Rounded.Save, contentDescription = null)
        }
    }
}

@Preview
@Composable
private fun PreviewToolbarPane() {
    DstTranslatorTheme {
        ToolbarPane(filteredList = listOf(WordEntry.default()), changedList = listOf(0))
    }
}
