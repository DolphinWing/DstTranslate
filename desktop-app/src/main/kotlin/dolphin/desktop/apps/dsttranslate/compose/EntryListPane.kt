package dolphin.desktop.apps.dsttranslate.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
    enabled: Boolean = true,
) {
    Column(modifier = modifier) {
        ToolbarPane(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            filteredList = dataList,
            changedList = changedList,
            onRefresh = onRefresh,
            onSave = onSave,
            onSearch = onSearch,
            enabled = enabled,
        )
        if (enabled && (dataList?.isEmpty() == true || dataList == null)) {
            Text("no items", modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
        }
        LazyScrollableColumn(
            dataList ?: emptyList(),
            modifier = Modifier.weight(1f)
        ) { index, entry ->
            // val dst = remember { helper.dst(entry.key) }
            // val chs = remember { helper.chs(entry.key) }
            // val cht = remember { helper.cht(entry.key) }
            EntryView(
                origin = entry,
                modifier = Modifier.fillMaxWidth(),
                dst = helper.dst(entry.key), // dst,
                chs = helper.chs(entry.key), // chs,
                cht = helper.cht(entry.key), // cht,
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
    dst: WordEntry? = null,
    chs: WordEntry? = null,
    cht: WordEntry? = null,
    onItemClick: ((WordEntry) -> Unit)? = null,
    index: Int = 0,
    changed: Long = 0L,
) {
    // val entry by remember { mutableStateOf(origin) }
    val changedColor = if (changed > 0) Color.Gray else Color.LightGray
    // println("${origin.key()}: ${chs?.string()} ${cht?.string()} ${origin.string()}")

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
                origin.key(),
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
        chs?.let { source ->
            Text(source.origin(), fontSize = AppTheme.largerFontSize())
        }
        Text(
            // if (dst?.id == origin.id) dst.string() else dst?.origin() ?: origin.origin(),
            cht?.string() ?: chs?.string() ?: origin.origin(),
            color = AppTheme.AppColor.purple,
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            dst?.string() ?: chs?.string() ?: origin.string(),
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

@Preview
@Composable
private fun PreviewEntryViewOrigin() {
    DstTranslatorTheme {
        EntryView(origin = WordEntry.default())
    }
}

@Preview
@Composable
private fun PreviewEntryViewAll() {
    DstTranslatorTheme {
        EntryView(
            origin = WordEntry.default(),
            dst = PreviewDefaults.dst,
            chs = PreviewDefaults.chs,
            cht = PreviewDefaults.cht,
        )
    }
}
