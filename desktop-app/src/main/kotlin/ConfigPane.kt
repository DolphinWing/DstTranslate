import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dolphin.desktop.apps.dsttranslate.Ini
import kotlinx.coroutines.launch
import javax.swing.JFileChooser

@Composable
fun ConfigPane(ini: Ini) {
    val composeScope = rememberCoroutineScope()
    // configs, usually we set this one time
    var workshopDir by remember { mutableStateOf(ini.workshopDir) }
    var assetsDir by remember { mutableStateOf(ini.assetsDir) }

    LaunchedEffect(Unit) {
        ini.load() // load default configs
        workshopDir = ini.workshopDir
        assetsDir = ini.assetsDir
    }

    Column {
        Text("workshop dir", style = MaterialTheme.typography.caption)
        FileChooserPane(
            file = workshopDir,
            onFileChange = { file ->
                composeScope.launch {
                    workshopDir = file.absolutePath
                    ini.workshopDir = workshopDir
                    ini.save()
                }
            },
            selectionMode = JFileChooser.DIRECTORIES_ONLY,
        )
        Spacer(modifier = Modifier.requiredHeight(4.dp))
        Text("assets dir", style = MaterialTheme.typography.caption)
        FileChooserPane(
            file = assetsDir,
            onFileChange = { file ->
                composeScope.launch {
                    assetsDir = file.absolutePath
                    ini.assetsDir = assetsDir
                    ini.save()
                }
            },
            selectionMode = JFileChooser.DIRECTORIES_ONLY,
        )
    }
}