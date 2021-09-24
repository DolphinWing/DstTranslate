package dolphin.android.apps.dsttranslate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun EntryCountView(list: List<WordEntry>, modifier: Modifier = Modifier) {
    Text(
        "total size = ${list.size}",
        modifier = modifier
            .padding(4.dp)
            .background(Color.White),
    )
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

    Column(
        modifier = modifier
            .background(if (changed > 0) Color.Yellow.copy(alpha = .1f) else Color.Transparent)
            .clickable(onClick = { onItemClick?.invoke(origin) })
            .padding(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                origin.key,
                color = Color.DarkGray,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                index.toString(), // String.format("%05d", index),
                color = Color.LightGray,
                // fontSize = 12.sp,
            )
        }
        src?.let { source ->
            Text(source.id)
        }
        Text(if (old?.id == origin.id) old.str else old?.id ?: origin.id, color = Color.Blue)
        Text(old?.str ?: origin.str, color = Color.Red)
        Text(origin.str, color = Color.Green)
    }
}

@Preview
@Composable
private fun PreviewEntryView() {
    Column {
        // new
        EntryView(
            WordEntry(
                "STRINGS.ACTIONS.ACTIVATE.CLIMB",
                "STRINGS.ACTIONS.ACTIVATE.CLIMB",
                "Climb",
                "Climb STR",
            ),
        )
        // different id
        EntryView(
            origin = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happiness",
                "Assess Happiness STR",
            ),
            old = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happy",
                "Assess Happiness STR",
            ),
        )
    }
}
