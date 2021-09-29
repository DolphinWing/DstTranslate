package dolphin.android.apps.dsttranslate.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dolphin.android.apps.dsttranslate.WordEntry


@Composable
fun EntryCountView(
    modifier: Modifier = Modifier,
    filteredList: List<WordEntry>? = null,
    changedList: List<Long>? = null,
    onRefresh: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
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
            "source",
            modifier = Modifier
                .background(colorResource(id = android.R.color.holo_blue_dark))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            "origin",
            modifier = Modifier
                .background(colorResource(id = android.R.color.holo_red_light))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            "target",
            modifier = Modifier
                .background(colorResource(id = android.R.color.holo_green_dark))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            fontSize = AppTheme.largerFontSize(),
        )
        Spacer(modifier = Modifier.requiredWidth(8.dp))
        IconButton(onClick = { onRefresh?.invoke() }) {
            Icon(
                Icons.Default.Refresh,
                tint = MaterialTheme.colors.onPrimary,
                contentDescription = null,
            )
        }
        IconButton(onClick = { onSave?.invoke() }) {
            Icon(
                Icons.Default.Save,
                tint = MaterialTheme.colors.onPrimary,
                contentDescription = null,
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
//            if (origin.newly) {
//                Text(
//                    text = "NEW",
//                    modifier = Modifier.padding(horizontal = 4.dp),
//                    color = Color.Red,
//                    fontSize = 12.sp,
//                )
//            }
            Text(
                index.toString(), // String.format("%05d", index),
                color = if (origin.newly) Color.Red else Color.LightGray,
                fontSize = 14.sp,
            )
        }
        src?.let { source ->
            Text(source.origin(), fontSize = AppTheme.largerFontSize())
        }
        Text(
            if (old?.id == origin.id) old.string() else old?.origin() ?: origin.origin(),
            color = colorResource(id = android.R.color.holo_blue_dark),
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            old?.string() ?: src?.string() ?: origin.string(),
            color = colorResource(id = android.R.color.holo_red_light),
            fontSize = AppTheme.largerFontSize(),
        )
        Text(
            origin.string(),
            color = colorResource(id = android.R.color.holo_green_dark),
            fontSize = AppTheme.largerFontSize(),
        )
    }
}

@Preview(widthDp = 640, heightDp = 480)
@Composable
private fun PreviewEntryView() {
    AppTheme {
        Column(modifier = Modifier.background(Color.White)) {
            EntryCountView(changedList = listOf(0L, 0L))

            // new
            EntryView(
                WordEntry(
                    "STRINGS.ACTIONS.ACTIVATE.CLIMB",
                    "STRINGS.ACTIONS.ACTIVATE.CLIMB",
                    "Climb",
                    "\"Climb STR\"",
                    newly = true,
                ),
            )

            // different id
            EntryView(
                origin = WordEntry(
                    "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                    "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                    "Assess Happiness",
                    "\"Assess Happiness STR\"",
                ),
                old = WordEntry(
                    "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                    "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                    "Assess Happy",
                    "\"Assess Happiness STR\"",
                ),
                changed = 10L,
            )
        }
    }
}
