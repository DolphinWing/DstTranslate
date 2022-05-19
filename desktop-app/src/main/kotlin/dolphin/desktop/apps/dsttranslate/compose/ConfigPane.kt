package dolphin.desktop.apps.dsttranslate.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dolphin.desktop.apps.dsttranslate.Ini
import java.io.File
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
    onConfigChange: ((configs: Configs) -> Unit)? = null,
) {
    var visible by remember { mutableStateOf(false) }
    val githubRoot = if (configs.workshopDir.contains("DstTranslate")) {
        configs.workshopDir.substring(0, configs.workshopDir.indexOf("DstTranslate"))
    } else ""

    Column {
        Text("GitHub root", style = MaterialTheme.typography.caption)
        FileChooserPane(
            file = githubRoot,
            onFileChange = { file ->
                println("github root = ${file.absolutePath}")
                val s = File.separator
                onConfigChange?.invoke(
                    configs.copy(
                        workshopDir = "${file.absolutePath}${s}workshop-1993780385",
                        assetsDir = "${file.absolutePath}${s}android-app${s}app${s}src${s}main${s}assets",
                        stringMap = "${file.absolutePath}${s}desktop-app${s}resources${s}common${s}strings.xml",
                    )
                )
            },
            selectionMode = JFileChooser.DIRECTORIES_ONLY,
        )
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
        Spacer(modifier = Modifier.requiredHeight(4.dp))
        Text(
            "strings.xml: ${configs.stringMap}",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.clickable { visible = true }.padding(8.dp),
            color = if (configs.stringMap.isEmpty()) Color.Red else MaterialTheme.typography.caption.color,
        )
        if (visible) {
            FileChooserPane(file = configs.stringMap, onFileChange = { file ->
                // println("strings.xml = ${file.absolutePath}")
                onConfigChange?.invoke(configs.copy(stringMap = file.absolutePath))
            })
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
