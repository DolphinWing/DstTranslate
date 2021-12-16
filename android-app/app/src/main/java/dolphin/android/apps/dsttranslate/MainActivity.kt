package dolphin.android.apps.dsttranslate

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import dolphin.android.apps.dsttranslate.compose.AppTheme
import dolphin.android.apps.dsttranslate.compose.EntryCountView
import dolphin.android.apps.dsttranslate.compose.EntryEditor
import dolphin.android.apps.dsttranslate.compose.EntrySearchView
import dolphin.android.apps.dsttranslate.compose.EntryView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

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

    private lateinit var helper: PoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helper = AndroidPoHelper(this).apply { prepare() }

        setContent {
            val list = dataList.observeAsState()
            val changed = changeList.observeAsState()
            val enabled = loading.observeAsState()

            AppTheme {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                ) {
                    stickyHeader {
                        EntryCountView(
                            modifier = Modifier.fillMaxWidth(),
                            filteredList = list.value,
                            changedList = changed.value,
                            onRefresh = { onRefreshClick() },
                            onSave = { onSaveClick() },
                            onSearch = { onSearchClick() },
                            enabled = enabled.value != true,
                        )
                    }
                    itemsIndexed(
                        items = list.value ?: ArrayList(),
                        key = { _, item -> item.key },
                    ) { index, entry ->
                        val origin = remember { helper.origin(entry.key) }
                        val source = remember { helper.source(entry.key) }

                        EntryView(
                            origin = entry,
                            modifier = Modifier.fillMaxWidth(),
                            old = origin,
                            src = source,
                            onItemClick = { item ->
                                showEntryEditor(item, helper.origin(entry.key))
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
                        revised = editRevise.observeAsState().value,
                        onCancel = { hideEntryEditor() },
                        onCopy = { text -> copyToClipboard(text) },
                        onSave = { key, text -> applyToDictionary(key, text) },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = .5f))
                            .fillMaxSize()
                            .padding(horizontal = 36.dp, vertical = 24.dp),
                        onTranslate = { text -> openGoogleTranslate(text) }
                    )
                }

                if (searching.observeAsState().value == true) {
                    EntrySearchView(
                        items = helper.originKeys(),
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = .5f))
                            .fillMaxSize()
                            .padding(horizontal = 36.dp, vertical = 24.dp),
                        onSelect = { key ->
                            Log.d(TAG, "key = $key")
                            hideSearchPane()
                            helper.origin(key)?.let { entry ->
                                showEntryEditor(entry, entry)
                            }
                        },
                        onCancel = { hideSearchPane() },
                    )
                }

                if (enabled.value == true) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.requiredSize(48.dp),
                            color = MaterialTheme.colors.primary,
                        )
                        Text(
                            helper.status.collectAsState().value,
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        if (allPermissionsGranted()) {
            // Handler(Looper.getMainLooper()).post { runDstTranslation() }
            runDstTranslation()
        } else {
            Toast.makeText(this, "No permission!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
        showResultToast(helper.getOutputFile(), System.currentTimeMillis() - start)
    }

    private fun onSearchClick() {
        searching.postValue(true)
    }

    private fun hideSearchPane() {
        searching.postValue(false)
    }

    private fun runDstTranslation() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "ENTER run dst translation")
            loading.postValue(true)
            val cost = helper.runTranslationProcess()
            Log.d(TAG, "cost = $cost ms")
            showChangeList()
            showResultToast(helper.getCachedFile(), cost)
            loading.postValue(false)
            Log.d(TAG, "LEAVE run dst translation")
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
    private val searching = MutableLiveData(false)
    private val editTarget = MutableLiveData(WordEntry.default())
    private val editOrigin = MutableLiveData(WordEntry.default())
    private val editSource = MutableLiveData("")
    private val editRevise = MutableLiveData("")

    private fun showChangeList() {
        val list = ArrayList<Long>()
        val filtered = helper.buildChangeList()
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
        editSource.postValue(helper.sc2tc(helper.source(entry.key)?.str ?: ""))
        editRevise.postValue(helper.revise(entry.key)?.str ?: "")
        editing.postValue(true)
    }

    private fun hideEntryEditor() {
        editing.postValue(false)
    }

    private fun showResultToast(file: File, timeCost: Long) {
        runOnUiThread {
            Toast.makeText(
                this@MainActivity,
                "DONE ${file.absolutePath} ($timeCost ms)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun copyToClipboard(text: String, label: String = TAG) {
        val clip = ClipData.newPlainText(label, text)
        (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(clip)
        Toast.makeText(this@MainActivity, "Copied!", Toast.LENGTH_SHORT).show()
    }

    private fun applyToDictionary(key: String, text: String) {
        helper.update(key, text)
        showChangeList()
        hideEntryEditor()
    }

    private fun openGoogleTranslate(text: String) {
        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            data = "https://translate.google.com.tw/?hl=zh-TW&sl=en&tl=zh-TW&text=$text".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
