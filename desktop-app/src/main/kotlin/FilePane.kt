import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

@Composable
fun FileChooserPane(
    file: String?,
    onFileChange: (File) -> Unit,
    modifier: Modifier = Modifier,
    filter: FileFilter? = null,
    selectionMode: Int = JFileChooser.FILES_ONLY,
) {
    val chooser = JFileChooser().apply {
        fileSelectionMode = selectionMode
        fileFilter = filter
        selectedFile = if (file != null) File(file) else null
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = file ?: "",
            onValueChange = { },
            readOnly = true,
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            // textStyle = MaterialTheme.typography.body2,
        )
        IconButton(onClick = {
            val ret = chooser.showOpenDialog(ComposeWindow())
            if (ret == JFileChooser.APPROVE_OPTION) {
                onFileChange.invoke(chooser.selectedFile)
            }
        }) {
            Icon(
                Icons.Rounded.Folder,
                contentDescription = "Folder",
                tint = MaterialTheme.colors.primary,
            )
        }
    }
}
