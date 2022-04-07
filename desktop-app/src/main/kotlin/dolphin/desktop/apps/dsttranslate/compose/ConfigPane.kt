package dolphin.desktop.apps.dsttranslate.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dolphin.desktop.apps.dsttranslate.Ini
import javax.swing.JFileChooser

data class Configs(
    val workshopDir: String = "",
    val assetsDir: String = "",
    val stringMap: String = "",
) {
    constructor(ini: Ini) : this(ini.workshopDir, ini.assetsDir, ini.stringMap)
}

@Composable
fun ConfigPane(
    configs: Configs,
    isLinux: Boolean = false,
    onConfigChange: ((configs: Configs) -> Unit)? = null,
) {
    var visible by remember { mutableStateOf(false) }

    Column {
        Text("workshop dir", style = MaterialTheme.typography.caption)
        FileChooserPane(
            file = configs.workshopDir,
            onFileChange = { file ->
                // println("workshopDir = ${file.absolutePath}")
                onConfigChange?.invoke(configs.copy(workshopDir = file.absolutePath))
            },
            selectionMode = JFileChooser.DIRECTORIES_ONLY,
        )
        Spacer(modifier = Modifier.requiredHeight(4.dp))
        Text("assets dir", style = MaterialTheme.typography.caption)
        FileChooserPane(
            file = configs.assetsDir,
            onFileChange = { file ->
                // println("assetsDir = ${file.absolutePath}")
                onConfigChange?.invoke(configs.copy(assetsDir = file.absolutePath))
            },
            selectionMode = JFileChooser.DIRECTORIES_ONLY,
        )
        if (isLinux) { // only used in linux mode
            Spacer(modifier = Modifier.requiredHeight(4.dp))
            Text(
                "strings.xml: ${configs.stringMap}",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.clickable { visible = true },
            )
            if (visible) {
                FileChooserPane(file = configs.stringMap, onFileChange = { file ->
                    // println("strings.xml = ${file.absolutePath}")
                    onConfigChange?.invoke(configs.copy(stringMap = file.absolutePath))
                })
            }
        }
    }
}

@Preview
@Composable
private fun PreviewConfigPane() {
    DstTranslatorTheme {
        ConfigPane(Configs("/home/dolphin", "/home/dolphin/assets"))
    }
}
