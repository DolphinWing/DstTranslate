// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dolphin.android.apps.dsttranslate.WordEntry
import dolphin.desktop.apps.dsttranslate.DesktopPoHelper
import dolphin.desktop.apps.dsttranslate.Ini
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard

import java.awt.datatransfer.StringSelection


object AppTheme {
    @Composable
    fun largerFontSize(): TextUnit = 18.sp

    object AppColor {
        val purple = Color(156, 39, 176)
        val blue = Color(33, 150, 243)
        val orange = Color(255, 87, 34)
        val green = Color(76, 175, 80)
        val primary = Color(96, 125, 139)
        val secondary = Color(0, 150, 136)
    }
}

@Composable
@Preview
fun App(helper: DesktopPoHelper, onCopyTo: (String) -> Unit) {
    MaterialTheme(
        colors = lightColors(
            primary = AppTheme.AppColor.primary,
            secondary = AppTheme.AppColor.secondary,
        ),
    ) {
        val composeScope = rememberCoroutineScope()
        // data list
        var dataList by remember { mutableStateOf(emptyList<WordEntry>()) }
        var changedList by remember { mutableStateOf(emptyList<Long>()) }
        // editor
        var editing by remember { mutableStateOf(false) }
        var editTarget by remember { mutableStateOf(WordEntry.default()) }
        var editOrigin by remember { mutableStateOf<WordEntry?>(WordEntry.default()) }
        var editSource by remember { mutableStateOf("") }
        var editRevise by remember { mutableStateOf("") }
        // search
        var searching by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            helper.setupXml() // setup replacement
        }

        fun showEntryList() {
            composeScope.launch {
                val cost = helper.runTranslationProcess()
                println("cost $cost ms")
                val list = ArrayList<Long>()
                val filtered = helper.buildChangeList()
                filtered.forEach { item -> list.add(item.changed) }
                dataList = filtered
                changedList = list
            }
        }

        fun showEntryEditor(entry: WordEntry) {
            val origin = helper.origin(entry.key)
            println("edit: ${entry.key} ${origin?.key}")
            if (!entry.newly) println("origin: ${origin?.id} ${origin?.str}")
            editTarget = entry
            editOrigin = origin
            editSource = helper.sc2tc(helper.source(entry.key)?.str ?: "")
            editRevise = helper.revise(entry.key)?.str ?: ""
            editing = true
        }

        fun hideEntryEditor() {
            editing = false
        }

        fun showSearchPane() {
            searching = true
        }

        fun hideSearchPane() {
            searching = false
        }

        fun saveEntryList() {
            composeScope.launch {
                val start = System.currentTimeMillis()
                helper.writeEntryToFile()
                val cost = System.currentTimeMillis() - start
                println("write cost $cost ms")
            }
        }

        Box(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                ConfigPane(ini = helper.ini)
                EntryListPane(
                    helper,
                    modifier = Modifier.weight(1f),
                    dataList = dataList,
                    changedList = changedList,
                    onRefresh = { showEntryList() },
                    onEdit = { entry -> showEntryEditor(entry) },
                    onSave = { saveEntryList() },
                    onSearch = { showSearchPane() },
                )
            }
            if (editing) {
                EditorPane(
                    target = editTarget,
                    modifier = Modifier.fillMaxSize(),
                    origin = editOrigin,
                    source = editSource,
                    revised = editRevise,
                    onSave = { key, text ->
                        helper.update(key, text)
                        hideEntryEditor()
                    },
                    onCancel = { hideEntryEditor() },
                    onCopy = onCopyTo,
                    onTranslate = onCopyTo,
                )
            }
            if (searching) {
                SearchPane(
                    items = helper.originKeys(),
                    modifier = Modifier.fillMaxSize(),
                    onSelect = { key ->
                        println("key = $key")
                        hideSearchPane()
                        helper.origin(key)?.let { entry ->
                            showEntryEditor(entry)
                        }
                    },
                    onCancel = { hideSearchPane() },
                )
            }
        }
    }
}

fun main() = application {
    val workingDir: String = System.getProperty("user.dir")
    println("workingDir=$workingDir")

    val helper = DesktopPoHelper(Ini(workingDir))
    helper.prepare()

    Window(onCloseRequest = ::exitApplication) {
        App(helper, onCopyTo = ::copyToSystemClipboard)
    }
}

/**
 * Copying text to the clipboard using Java
 * See https://stackoverflow.com/a/6713290
 */
fun copyToSystemClipboard(text: String) {
    val stringSelection = StringSelection(text)
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(stringSelection, null)
}
