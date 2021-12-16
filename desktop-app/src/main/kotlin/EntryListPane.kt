import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dolphin.android.apps.dsttranslate.PoHelper
import dolphin.android.apps.dsttranslate.WordEntry

@Composable
fun EntryListPane(
    helper: PoHelper,
    modifier: Modifier = Modifier,
    dataList: List<WordEntry>? = null,
    changedList: List<Long>? = null,
    onRefresh: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onEdit: ((WordEntry) -> Unit)? = null,
) {
    Column(modifier = modifier) {
        ToolbarPane(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            filteredList = dataList,
            changedList = changedList,
            onRefresh = onRefresh,
            onSave = onSave,
            onSearch = onSearch,
        )
        LazyScrollableColumn(
            dataList ?: emptyList(),
            modifier = Modifier.weight(1f)
        ) { index, entry ->
            val origin = remember { helper.origin(entry.key) }
            val source = remember { helper.source(entry.key) }

            EntryView(
                origin = entry,
                modifier = Modifier.fillMaxWidth(),
                old = origin,
                src = source,
                onItemClick = { item -> onEdit?.invoke(item) },
                index = index,
                changed = changedList?.get(index) ?: 0L,
            )
        }
    }
}

@Composable
fun EntryView(
    origin: WordEntry,
    modifier: Modifier = Modifier,
    old: WordEntry? = null,
    src: WordEntry? = null,
    onItemClick: ((WordEntry) -> Unit)? = null,
    index: Int = 0,
    changed: Long = 0L,
) {
    // val entry by remember { mutableStateOf(origin) }
    val changedColor = if (changed > 0) Color.Gray else Color.LightGray

    Column(
        modifier = modifier
            // .background(if (changed > 0) Color.Yellow.copy(alpha = .1f) else Color.Transparent)
            .clickable(onClick = { onItemClick?.invoke(origin) })
            .padding(2.dp)
            .border(if (changed > 0) 2.dp else 1.dp, changedColor, RoundedCornerShape(4.dp))
            .padding(vertical = 2.dp, horizontal = 6.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                origin.key,
                color = Color.DarkGray,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp, // AppTheme.fontSize(),
            )
            Text(
                (index + 1).toString(), // String.format("%05d", index),
                color = if (origin.newly) Color.Red else Color.LightGray,
                fontSize = 14.sp,
            )
        }
        src?.let { source ->
            Text(source.origin(), fontSize = AppTheme.largerFontSize())
        }
        Text(
            if (old?.id == origin.id) old.string() else old?.origin() ?: origin.origin(),
            color = AppTheme.AppColor.blue,
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            old?.string() ?: src?.string() ?: origin.string(),
            color = AppTheme.AppColor.orange,
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            origin.string(),
            color = AppTheme.AppColor.green,
            fontSize = AppTheme.largerFontSize(),
        )
    }
}
