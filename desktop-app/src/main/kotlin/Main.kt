// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed
// by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dolphin.android.apps.dsttranslate.WordEntry
import dolphin.desktop.apps.dsttranslate.DesktopPoHelper
import dolphin.desktop.apps.dsttranslate.Ini
import dolphin.desktop.apps.dsttranslate.SuspectMap
import dolphin.desktop.apps.dsttranslate.compose.ConfigPane
import dolphin.desktop.apps.dsttranslate.compose.Configs
import dolphin.desktop.apps.dsttranslate.compose.DebugSaveDialog
import dolphin.desktop.apps.dsttranslate.compose.DstTranslatorTheme
import dolphin.desktop.apps.dsttranslate.compose.EditorPane
import dolphin.desktop.apps.dsttranslate.compose.EditorSpec
import dolphin.desktop.apps.dsttranslate.compose.EntryListPane
import dolphin.desktop.apps.dsttranslate.compose.SearchPane
import dolphin.desktop.apps.dsttranslate.compose.SuspectPane
import dolphin.desktop.apps.dsttranslate.compose.ToastUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URL

@ExperimentalMaterialApi
@Composable
@Preview
fun App(
    helper: DesktopPoHelper,
    onCopyTo: (String) -> Unit,
    debug: Boolean = false,
    appVersion: String = "x.x.x",
) {
    DstTranslatorTheme {
        val composeScope = rememberCoroutineScope()
        var configs by remember { mutableStateOf(Configs()) }
        var isLinux by remember { mutableStateOf(false) }
        // data list
        var dataList by remember { mutableStateOf(emptyList<WordEntry>()) }
        var changedList by remember { mutableStateOf(emptyList<Long>()) }
        // editor
        var editing by remember { mutableStateOf(false) }
        var editorData by remember { mutableStateOf(EditorSpec()) }
        // search
        var searching by remember { mutableStateOf(false) }
        var toasted by remember { mutableStateOf("") }
        var cached by remember { mutableStateOf(false) }
        // suspect
        var analysis by remember { mutableStateOf(false) }
        var suspectMap by remember { mutableStateOf(SuspectMap()) }

        // global status
        val enabled = helper.loading.collectAsState()

        // toast
        val toastJob = remember { mutableStateOf<Job?>(null) }
        fun toast(message: String) {
            toastJob.value?.cancel()
            toastJob.value = composeScope.launch {
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

        fun loadIniAndPo() {
            composeScope.launch {
                helper.loadXml() // setup replacement at launch
                configs = Configs(helper.ini)
                isLinux = helper.ini.isLinux
                helper.runTranslationProcess() // setup replacement at launch
                updateEntryList() // setup replacement at launch
            }
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
            // println("edit: ${entry.key}")
            // if (!entry.newly) println("origin: ${dst?.id}")
            editorData = EditorSpec(
                entry,
                dst,
                helper.sc2tc(helper.chs(entry.key)?.str ?: ""),
                helper.cht(entry.key)?.str,
            )
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
                val result = helper.writeTranslationFile(exported)
                val cost = System.currentTimeMillis() - start
                if (result) {
                    toast("write ${exported.absolutePath} cost $cost ms")
                } else {
                    toast("write failed!")
                }
            }
        }

        fun analyzeTextMap() {
            composeScope.launch {
                val start = System.currentTimeMillis()
                val (result, suspects) = helper.analyzeText()
                suspectMap = suspects
                analysis = true
                val cost = System.currentTimeMillis() - start
                toast("found $result, cost $cost ms")
            }
        }

        LaunchedEffect(Unit) {
            loadIniAndPo() // LaunchedEffect
        }

        Box(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
            Text(
                appVersion,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.align(Alignment.TopEnd),
                color = Color.LightGray,
            )

            Column(modifier = Modifier.fillMaxSize()) {
                ConfigPane(configs = configs, isLinux = isLinux, onConfigChange = { newConfigs ->
                    composeScope.launch {
                        helper.ini.apply(
                            workingDir = newConfigs.workshopDir,
                            assetsDir = newConfigs.assetsDir,
                            stringMap = newConfigs.stringMap,
                        )
                        loadIniAndPo() // refresh when configs changed
                    }
                })
                EntryListPane(
                    helper,
                    modifier = Modifier.weight(1f),
                    dataList = dataList,
                    changedList = changedList,
                    onRefresh = { showEntryList() },
                    onEdit = { entry -> showEntryEditor(entry) },
                    onSave = { if (debug) cached = true else saveEntryList() },
                    onSearch = { showSearchPane() },
                    onAnalyze = { analyzeTextMap() },
                    enabled = enabled.value.not(),
                    debug = debug,
                )
            }

            Text(
                helper.status.collectAsState().value,
                style = MaterialTheme.typography.caption,
                modifier = Modifier.align(Alignment.BottomStart),
            )

            if (searching) {
                SearchPane(
                    items = helper.allValues(),
                    modifier = Modifier.fillMaxSize(),
                    onSelect = { key ->
                        // println("edit key = $key")
                        // hideSearchPane()
                        helper.dst(key)?.let { entry ->
                            showEntryEditor(entry)
                        }
                    },
                    onCancel = { hideSearchPane() },
                )
            }

            if (analysis) {
                SuspectPane(
                    suspectMap,
                    onEdit = { entry -> showEntryEditor(entry) },
                    onHide = { analysis = false },
                )
            }

            if (editing) {
                EditorPane(
                    data = editorData,
                    modifier = Modifier.fillMaxSize(),
                    onSave = { key, text ->
                        helper.update(key, text)
                        updateEntryList()
                        hideEntryEditor()
                    },
                    onCancel = { hideEntryEditor() },
                    onCopy = { text ->
                        onCopyTo.invoke(text)
                        toast(text)
                    },
                    onTranslate = { text -> translateByGoogle(text) },
                )
            }

            if (cached) {
                DebugSaveDialog(
                    onDismissRequest = { cached = false },
                    onSave = { saveEntryList(it) },
                    title = "write to ${helper.getCachedFile()}?",
                    modifier = Modifier.fillMaxWidth(.5f),
                )
            }

            ToastUi(toasted)
        }
    }
}

@ExperimentalMaterialApi
fun main(args: Array<String>) = application {
    var version = args.find { it.startsWith("v=") }?.drop(2) ?: "x.x.x"

//    val osName: String = System.getProperties().getProperty("os.name")
//    println("os.name = $osName")

    val workingDir: String = System.getProperties().getProperty("user.dir")
    println("workingDir = $workingDir")

    val debug = File(workingDir, "build").exists() // has build dir
    println("debug = $debug")
    if (debug) version += "D" // check if it is a debug version

//    val tempDir: String = System.getProperty("java.io.tmpdir")
//    println("tempDir = $tempDir")
//
//    val homeDir: String = System.getProperty("user.home")
//    println("homeDir = $homeDir")

    val helper = DesktopPoHelper(Ini(workingDir), debug = debug)
    helper.prepare()

    Window(onCloseRequest = ::exitApplication, title = "DST Translate") {
        App(helper, onCopyTo = ::copyToSystemClipboard, debug = debug, appVersion = version)
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

/**
 * Open Google Translate and translate the text to chinese.
 * See https://stackoverflow.com/a/10967469
 *
 * @param text target text
 */
fun translateByGoogle(text: String) {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else return
    if (desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            val encoded = text.replace(" ", "%20")
            val url = "https://translate.google.com.tw/?hl=zh-TW&sl=en&tl=zh-TW&text=$encoded"
            desktop.browse(URL(url).toURI())
        } catch (e: Exception) {
            println("translateByGoogle: ${e.message}")
        }
    } else {
        println("unable to search $text")
        copyToSystemClipboard(text) // workaround
    }
}
