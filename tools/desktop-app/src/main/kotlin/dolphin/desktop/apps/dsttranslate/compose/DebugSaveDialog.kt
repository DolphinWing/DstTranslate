package dolphin.desktop.apps.dsttranslate.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DebugSaveDialog(
    onDismissRequest: () -> Unit,
    onSave: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Text(title, style = MaterialTheme.typography.h6)
        },
        buttons = {
            Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                TextButton(onClick = onDismissRequest) { Text("Cancel") }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onSave(false) }) { Text("No") }
                TextButton(
                    onClick = { onSave(true) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colors.secondary,
                    ),
                ) { Text("Yes") }
            }
        },
        modifier = modifier,
    )
}
