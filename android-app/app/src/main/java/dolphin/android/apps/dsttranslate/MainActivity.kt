package dolphin.android.apps.dsttranslate

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import dolphin.android.apps.dsttranslate.compose.AppTheme
import dolphin.android.apps.dsttranslate.compose.EntryCountView
import dolphin.android.apps.dsttranslate.compose.EntryEditor
import dolphin.android.apps.dsttranslate.compose.EntryView
import kotlinx.android.synthetic.main.layout_main.*
import java.io.*
import java.util.concurrent.Executors

@ExperimentalFoundationApi
class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "dst"

        // This is an array of all the permission specified in the manifest.
        private val REQUIRED_PERMISSIONS =
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var helper: PoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helper = PoHelper(this)

        setContent {
            val list = dataList.observeAsState()
            val changed = changeList.observeAsState()

            AppTheme {
                LazyColumn {
                    stickyHeader {
                        EntryCountView(
                            modifier = Modifier.fillMaxWidth(),
                            list = changed.value,
                            onRefresh = { onRefreshClick() },
                            onSave = { onSaveClick() },
                        )
                    }
                    itemsIndexed(list.value ?: ArrayList(), key = { _, item ->
                        item.key
                    }) { index, entry ->
                        val origin = remember { helper.originMap[entry.key] }
                        val source = remember { helper.sourceMap[entry.key] }

                        EntryView(
                            origin = entry,
                            modifier = Modifier.fillMaxWidth(),
                            old = origin,
                            src = source,
                            onItemClick = { item ->
                                showEntryEditor(item, helper.originMap[item.key])
                            },
                            index = index,
                            changed = changed.value?.get(index) ?: 0L,
                        )
                    }
                }

                if (editing.observeAsState().value == true) {
                    EntryEditor(
                        target = editTarget.observeAsState().value ?: WordEntry.default(),
                        origin = editOrigin.observeAsState().value,
                        source = editSource.observeAsState().value,
                        onCancel = { hideEntryEditor() },
                        onCopy = { text -> copyToClipboard(text) },
                        onSave = { key, text -> applyToDictionary(key, text) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = .5f))
                            .fillMaxSize()
                            .padding(horizontal = 36.dp, vertical = 24.dp),
                    )
                }

                if (loading.observeAsState().value == true) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.requiredSize(48.dp))
                    }
                }
            }
        }
        if (allPermissionsGranted()) {
            Handler(Looper.getMainLooper()).post { runDstTranslation() }
        } else {
            Toast.makeText(this, "No permission!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.button1 -> onRefreshClick()
            android.R.id.button2 -> onSaveClick()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onRefreshClick() {
        if (allPermissionsGranted()) {
            runDstTranslation()
        } else {
            startAppInfoActivity()
        }
    }

    private fun onSaveClick() {
        val start = System.currentTimeMillis()
        helper.writeEntryToFile()
        showResultToast(helper.targetFile, System.currentTimeMillis() - start)
    }

    private fun runDstTranslation() {
        executor.submit {
            loading.postValue(true)
            helper.runTranslation { timeCost ->
                showChangeList()
                showResultToast(helper.cacheFile, timeCost)
                loading.postValue(false)
            }
        }
    }

    private fun startAppInfoActivity() {
        // https://stackoverflow.com/a/32983128/2673859
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val dataList = MutableLiveData<List<WordEntry>>()
    private val changeList = MutableLiveData<List<Long>>()
    private val loading = MutableLiveData(false)
    private val editing = MutableLiveData(false)
    private val editTarget = MutableLiveData(WordEntry.default())
    private val editOrigin = MutableLiveData(WordEntry.default())
    private val editSource = MutableLiveData("")

    private fun showChangeList() {
        val list = ArrayList<Long>()
        val filtered = helper.wordList.filter { entry ->
            val origin = helper.originMap[entry.key]
            (entry.newly || origin?.id != entry.id || origin.str != entry.str || entry.changed > 0)
                    && entry.str.length > 2
        }
        filtered.forEach { item -> list.add(item.changed) }
        // Log.d(TAG, "filtered data = ${filtered.size}")
        dataList.postValue(filtered)
        changeList.postValue(list)
    }

    private fun showEntryEditor(entry: WordEntry, origin: WordEntry?) {
        if (editing.value == true) return // already editing

        Log.d(TAG, "entry: ${entry.id} ${entry.str}")
        if (!entry.newly) Log.d(TAG, "origin: ${origin?.id} ${origin?.str}")

        editTarget.postValue(entry)
        editOrigin.postValue(origin)
        editSource.postValue(helper.sc2tc(helper.sourceMap[entry.key]?.str ?: ""))
        editing.postValue(true)
    }

    private fun hideEntryEditor() {
        editing.postValue(false)
    }

    private fun showResultToast(file: File, timeCost: Long) {
        Toast.makeText(
            this@MainActivity,
            "DONE ${file.absolutePath} ($timeCost ms)",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun copyToClipboard(text: String, label: String = TAG) {
        val clip = ClipData.newPlainText(label, text)
        (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(clip)
        Toast.makeText(this@MainActivity, "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun applyToDictionary(key: String, text: String) {
        helper.wordList.find { entry -> entry.key == key }?.apply {
            str = text
            Log.d(TAG, "set new $key to $str")
            changed = System.currentTimeMillis() // set new change time
        }
        showChangeList()
        hideEntryEditor()
    }
}
