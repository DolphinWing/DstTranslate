// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.io.File

object AppTheme {
    @Composable
    fun largerFontSize(): TextUnit = 18.sp

    object AppColor {
        val purple = Color(156, 39, 176)
        val blue = Color(33, 150, 243)
        val orange = Color(255, 87, 34)
        val green = Color(76, 175, 80)
        val primary = Color(96, 125, 139)
        val secondary = Color(233, 30, 99)
    }
}

@ExperimentalMaterialApi
@Composable
@Preview
fun App(helper: DesktopPoHelper, onCopyTo: (String) -> Unit, debug: Boolean = false) {
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
        var editorNow by remember { mutableStateOf(WordEntry.default()) }
        var editorDst by remember { mutableStateOf<WordEntry?>(WordEntry.default()) }
        var editorChs by remember { mutableStateOf("") }
        var editorCht by remember { mutableStateOf("") }
        // search
        var searching by remember { mutableStateOf(false) }
        var toasted by remember { mutableStateOf("") }
        var cached by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            helper.loadXml() // setup replacement
        }

        fun toast(message: String) {
            composeScope.launch {
                toasted = message
                delay(2000)
                toasted = ""
            }
        }

        fun updateEntryList() {
            val list = ArrayList<Long>()
            val filtered = helper.buildChangeList()
            filtered.forEach { item -> list.add(item.changed) }
            dataList = filtered
            changedList = list
        }

        fun showEntryList() {
            composeScope.launch {
                val cost = helper.runTranslationProcess()
                // println("cost $cost ms")
                updateEntryList()
                toast("cost $cost ms")
            }
        }

        fun showEntryEditor(entry: WordEntry) {
            val dst = helper.dst(entry.key)
            println("edit: ${entry.key}")
            if (!entry.newly) println("origin: ${dst?.id}")
            editorNow = entry
            editorDst = dst
            editorChs = helper.sc2tc(helper.chs(entry.key)?.str ?: "")
            editorCht = helper.cht(entry.key)?.str ?: ""
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

        fun saveEntryList(cacheIt: Boolean = false) {
            composeScope.launch {
                cached = false // hide debug dialog
                val start = System.currentTimeMillis()
                val exported = helper.getOutputFile(cacheIt)
                val result = helper.writeEntryToFile(exported)
                val cost = System.currentTimeMillis() - start
                if (result) {
                    toast("write ${exported.absolutePath} cost $cost ms")
                } else {
                    toast("write failed!")
                }
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
                    onSave = { if (debug) cached = true else saveEntryList() },
                    onSearch = { showSearchPane() },
                )
            }

            Text(
                helper.status.collectAsState().value,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.align(Alignment.BottomStart),
            )

            if (editing) {
                EditorPane(
                    target = editorNow,
                    modifier = Modifier.fillMaxSize(),
                    dst = editorDst,
                    chs = editorChs,
                    cht = editorCht,
                    onSave = { key, text ->
                        helper.update(key, text)
                        updateEntryList()
                        hideEntryEditor()
                    },
                    onCancel = { hideEntryEditor() },
                    onCopy = { text ->
                        onCopyTo.invoke(text)
                        toast("Copied! $text")
                    },
                    onTranslate = { text ->
                        onCopyTo.invoke(text)
                        toast("Copied! $text")
                    },
                )
            }
            if (searching) {
                SearchPane(
                    items = helper.dstValues(),
                    modifier = Modifier.fillMaxSize(),
                    onSelect = { key ->
                        println("key = $key")
                        hideSearchPane()
                        helper.dst(key)?.let { entry ->
                            showEntryEditor(entry)
                        }
                    },
                    onCancel = { hideSearchPane() },
                )
            }

            if (cached) {
                AlertDialog(
                    onDismissRequest = { cached = false },
                    text = { Text("write to ${helper.getCachedFile()}") },
                    buttons = {
                        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
                            TextButton(onClick = { cached = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { saveEntryList(false) }) { Text("No") }
                            TextButton(
                                onClick = { saveEntryList(true) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colors.secondary,
                                ),
                            ) { Text("Yes") }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(.4f),
                )
            }

            ToastUi(toasted)
        }
    }
}

@ExperimentalMaterialApi
fun main() = application {
    val workingDir: String = System.getProperty("user.dir")
    println("workingDir = $workingDir")

    val debug = File(workingDir, "build").exists() // has build dir
    println("debug = $debug")
    val helper = DesktopPoHelper(Ini(workingDir), debug = debug)
    helper.prepare()

    Window(onCloseRequest = ::exitApplication, title = "DST Translate") {
        App(helper, onCopyTo = ::copyToSystemClipboard, debug = debug)
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
