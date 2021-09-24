package dolphin.android.apps.dsttranslate

import android.Manifest
import android.annotation.SuppressLint
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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
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

    private lateinit var container: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        helper = PoHelper(this)

        setContentView(R.layout.activity_main)
        findViewById<ComposeView>(android.R.id.list)?.setContent {
            val list = dataList.observeAsState()
            val changed = changeList.observeAsState()

            if (loading.observeAsState().value == true) {
                CircularProgressIndicator(modifier = Modifier.requiredSize(48.dp))
            }

            LazyColumn {
                stickyHeader {
                    EntryCountView(list.value ?: ArrayList(), modifier = Modifier.fillMaxWidth())
                }
                itemsIndexed(list.value ?: ArrayList(), key = { _, item ->
                    item.key
                }) { index, entry ->
                    EntryView(
                        origin = entry,
                        modifier = Modifier.fillMaxWidth(),
                        old = helper.originMap[entry.key],
                        src = helper.sourceMap[entry.key],
                        onItemClick = { item ->
                            showEntryEditor(item, helper.originMap[item.key])
                        },
                        index = index,
                        changed = changed.value?.get(index) ?: 0L,
                    )
                }
            }
        }
        prepareEntryEditor()
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
            android.R.id.button1 ->
                if (allPermissionsGranted()) {
                    runDstTranslation()
                } else {
                    startAppInfoActivity()
                }
            android.R.id.button2 -> {
                val start = System.currentTimeMillis()
                helper.writeEntryToFile()
                showResultToast(helper.targetFile, System.currentTimeMillis() - start)
            }

        }
        return super.onOptionsItemSelected(item)
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
        //https://stackoverflow.com/a/32983128/2673859
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

    @SuppressLint("ClickableViewAccessibility")
    private fun prepareEntryEditor() {
        container = findViewById(R.id.container)
        container.setOnTouchListener { _, _ ->
            container.visibility = View.GONE
            true
        }
        editor.setOnTouchListener { _, _ -> true }
        button1.setOnClickListener { view ->
            textField.editText?.setText(view.tag.toString())
            //container.visibility = View.VISIBLE
        }
        button2.setOnClickListener { view ->
            textField.editText?.setText(view.tag.toString())
            //container.visibility = View.VISIBLE
        }
        button3.setOnClickListener { view ->
            val key = view.tag.toString()
            helper.wordList.find { entry -> entry.key == key }?.apply {
                str = textField.editText?.text?.toString() ?: str
                Log.d(TAG, "set new $key to $str")
                changed = System.currentTimeMillis() // set new change time
            }
            showChangeList()
            container.visibility = View.GONE
        }
        button4.setOnClickListener { view ->
            textField.editText?.setText(view.tag.toString())
            //container.visibility = View.VISIBLE
        }
        text1.setOnClickListener {
            copyToClipboard(it.tag.toString())
            Toast.makeText(this@MainActivity, "Copied!", Toast.LENGTH_SHORT).show()
        }
        text2.setOnClickListener {
            copyToClipboard(it.tag.toString())
            Toast.makeText(this@MainActivity, "Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEntryEditor(entry: WordEntry, origin: WordEntry?) {
        Log.d(TAG, "entry: ${entry.id} ${entry.str}")
        if (!entry.newly) Log.d(TAG, "origin: ${origin?.id} ${origin?.str}")
        entry_id.text = entry.key
        text1.text = origin?.id ?: entry.id
        text1.tag = (origin?.id ?: entry.id).drop(1).dropLast(1)
        button1.text = origin?.str ?: entry.str
        button1.tag = origin?.str ?: entry.str
        button1.isEnabled = origin != null && origin.str != entry.str
        text2.text = entry.id
        text2.tag = entry.id.drop(1).dropLast(1)
        button2.text = entry.str
        button2.tag = entry.str
        button4.apply {
            text = helper.sc2tc(helper.sourceMap[entry.key]?.str ?: "")
            isEnabled = text?.isNotEmpty() == true
            tag = text
        }
        val str = helper.wordList.find { it.key == entry.key }?.str ?: entry.str // use dictionary
        textField.editText?.setText(str)
        container.visibility = View.VISIBLE
        button3.tag = entry.key
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
    }
}
