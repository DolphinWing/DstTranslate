package dolphin.android.apps.dsttranslate.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dolphin.android.apps.dsttranslate.WordEntry
import dolphin.android.apps.dsttranslate.dropQuote

@Composable
fun EntryEditor(
    target: WordEntry,
    modifier: Modifier = Modifier,
    origin: WordEntry? = null,
    source: String? = null,
    revised: String? = null,
    onSave: ((String, String) -> Unit)? = null,
    onCopy: ((String) -> Unit)? = null,
    onTranslate: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    var text by remember { mutableStateOf(target.string()) }
    var targetVisible by remember { mutableStateOf(true) }
    var originVisible by remember { mutableStateOf(true) }
    var sourceVisible by remember { mutableStateOf(true) }
    var reviseVisible by remember { mutableStateOf(true) }
    val holoBlue = colorResource(id = android.R.color.holo_blue_dark)
    val holoRed = colorResource(id = android.R.color.holo_red_light)
    val holoGreen = colorResource(id = android.R.color.holo_green_dark)
    val holoPurple = colorResource(id = android.R.color.holo_purple)
    val alpha = .25f

    Column(
        modifier = modifier
            .background(Color.White)
            // .verticalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(target.key(), modifier = Modifier.weight(1f), overflow = TextOverflow.Ellipsis)
            IconButton(onClick = { targetVisible = !targetVisible }) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (targetVisible) holoGreen else holoGreen.copy(alpha = alpha)
                )
            }
            origin?.let { // new item has no previous for reference, need to check source
                IconButton(onClick = { originVisible = !originVisible }) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (originVisible) holoRed else holoRed.copy(alpha = alpha)
                    )
                }
            }
            IconButton(onClick = { sourceVisible = !sourceVisible }) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (sourceVisible) holoBlue else holoBlue.copy(alpha = alpha)
                )
            }
            IconButton(onClick = { reviseVisible = !reviseVisible }) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = if (reviseVisible) holoPurple else holoPurple.copy(alpha = alpha)
                )
            }
        }

        if (sourceVisible) {
            Button(
                onClick = { text = source?.dropQuote() ?: "" },
                modifier = Modifier.fillMaxWidth(),
                enabled = source?.isNotEmpty() == true,
                colors = ButtonDefaults.buttonColors(backgroundColor = holoBlue),
            ) {
                Text(source?.dropQuote() ?: "", fontSize = AppTheme.largerFontSize())
            }
        }
        if (reviseVisible) {
            Button(
                onClick = { text = revised?.dropQuote() ?: "" },
                modifier = Modifier.fillMaxWidth(),
                enabled = revised?.isNotEmpty() == true,
                colors = ButtonDefaults.buttonColors(backgroundColor = holoPurple),
            ) {
                Text(revised?.dropQuote() ?: "", fontSize = AppTheme.largerFontSize())
            }
        }

        origin?.let { old ->
            if (originVisible) {
                Row {
                    TextButton(
                        onClick = { onTranslate?.invoke(old.origin()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = holoRed),
                    ) {
                        Text(
                            old.origin(),
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = AppTheme.largerFontSize(),
                        )
                    }
                    IconButton(onClick = { onCopy?.invoke(old.origin()) }) {
                        Icon(Icons.Default.CopyAll, contentDescription = null)
                    }
                }
                Button(
                    onClick = { text = old.string() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = holoRed),
                ) {
                    Text(old.string(), fontSize = AppTheme.largerFontSize())
                }
            }
        }

        if (targetVisible) {
            Row {
                TextButton(
                    onClick = { onTranslate?.invoke(target.origin()) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(contentColor = holoGreen),
                ) {
                    Text(
                        target.origin(),
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = AppTheme.largerFontSize(),
                    )
                }
                IconButton(onClick = { onCopy?.invoke(target.origin()) }) {
                    Icon(Icons.Default.CopyAll, contentDescription = null)
                }
            }
            Button(
                onClick = { text = target.string() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = holoGreen),
            ) {
                Text(target.string(), fontSize = AppTheme.largerFontSize())
            }
        }

        TextField(
            value = text,
            onValueChange = { str -> text = str },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            textStyle = TextStyle.Default.copy(fontSize = AppTheme.largerFontSize()),
            // singleLine = true,
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = { onCancel?.invoke() },
                modifier = Modifier.weight(2f),
            ) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.requiredWidth(16.dp))
            Button(
                onClick = { onSave?.invoke(target.key, "\"$text\"") },
                modifier = Modifier.weight(3f),
            ) {
                Text("Apply")
            }
        }
    }
}

@Preview(widthDp = 640, heightDp = 480)
@Composable
private fun PreviewEntryEditor() {
    AppTheme {
        EntryEditor(
            target = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happiness",
                "\"Assess Happiness STR\"",
            ),
            origin = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happy",
                "\"Assess Happiness STR\"",
            ),
            source = "\"Climb STR SC\"",
            revised = "\"Climb STR TC\"",
        )
    }
}

@Preview(widthDp = 640, heightDp = 480)
@Composable
private fun PreviewEntryEditorForNew() {
    AppTheme {
        EntryEditor(
            target = WordEntry(
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "STRINGS.ACTIONS.ASSESSPLANTHAPPINESS.GENERIC",
                "Assess Happiness",
                "\"Assess Happiness STR\"",
                newly = true,
            ),
            source = "\"Climb STR SC\"",
            revised = "\"Climb STR TC\"",
        )
    }
}
