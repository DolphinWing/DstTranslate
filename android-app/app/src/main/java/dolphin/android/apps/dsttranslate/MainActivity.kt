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
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import kotlinx.android.synthetic.main.layout_main.*
import java.io.*
import java.util.concurrent.Executors


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
    private lateinit var replaceList: Array<String>
    private lateinit var replace3dot: String
    private lateinit var replace6dot: String
    private lateinit var container: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        replaceList = resources.getStringArray(R.array.replacement_list)
        replace3dot = getString(R.string.replacement_3dot)
        replace6dot = "$replace3dot$replace3dot"
        setContentView(R.layout.activity_main)
        prepareEntryEditor()
        if (allPermissionsGranted()) {
            Handler().post { runDstTranslation() }
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
                writeEntryToFile(targetFile, wordList)
                showResultToast(targetFile, System.currentTimeMillis() - start)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun runDstTranslation() {
        executor.submit {
            runTranslation { timeCost ->
                showChangeList()
                showResultToast(cacheFile, timeCost)
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

    private val sourceMap = HashMap<String, WordEntry>()
    private val originMap = HashMap<String, WordEntry>()
    private val wordList = ArrayList<WordEntry>()

    private fun runTranslation(postAction: ((timeCost: Long) -> Unit)? = null) {
        Log.d(TAG, "run translation")
        val start = System.currentTimeMillis()

        val s = loadAssetFile("chinese_s.po")
        sourceMap.clear()
        s.forEach { entry ->
            // entry.str = sc2tc(entry.str).trim() // translate to traditional
            sourceMap[entry.key] = entry
        }
        val stop1 = System.currentTimeMillis()
        Log.d(TAG, "original SC size: ${s.size} (${stop1 - start} ms)")

        originMap.clear()
        loadAssetFile("dst_cht.po").filter { entry ->
            entry.id != "\"\"" && entry.str != "\"\""
        }.forEach { entry ->
            originMap[entry.key] = entry
        }
        val stop3 = System.currentTimeMillis()
        Log.d(TAG, "previous data size: ${originMap.size} (${stop3 - stop1} ms)")

        wordList.clear()
        s.forEach { entry ->
            var newly = false
            var str = ""
            if (originMap.containsKey(entry.key)) {
                val str1 = originMap[entry.key]?.str ?: ""
                if (str1.isNotEmpty()) str = str1.trim()
            } else {
                Log.d(TAG, "add ${entry.key}")
            }
            if (str.isEmpty()) {//not in the translated po
                newly = true
                str = sc2tc(entry.str).trim()
                //Log.d(TAG, ">> use $str")
            }
            str = getReplacement(str)
            wordList.add(WordEntry(entry.key, entry.text, entry.id, str, newly))
        }
        val stop4 = System.currentTimeMillis()
        Log.d(TAG, "new list size: ${wordList.size} (${stop4 - stop3} ms)")

        writeEntryToFile(cacheFile, wordList)
        val cost = System.currentTimeMillis() - start
        Log.d(TAG, "write data done. $cost ms")
        if (postAction != null) runOnUiThread { postAction(cost) }
    }

    private fun sc2tc(str: String): String =
        ChineseConverter.convert(str, ConversionType.S2TW, this)

    private fun getReplacement(src: String): String {
        var str = src.replace("...", replace3dot)
        str = if (str != "\"$replace6dot\"") str.replace(replace6dot, replace3dot) else str
        replaceList.forEach {
            val pair = it.split("|")
            str = str.replace(pair[0], pair[1])
        }
        if (str.contains("\\\"")) {
            var i = 0
            val str1 = str.replace("\\\"", "%@%").replace("%@%".toRegex()) {
                //Log.d(TAG, "$i ${it.value}")
                getString(if (i++ % 2 == 0) R.string.replacement_left_bracket else R.string.replacement_right_bracket)
            }
            if (i % 2 == 0) {
                Log.d(TAG, "$str ==> $str1")
                str = str1 //only replace the paired string
            }
        }
        return str
    }

    @Suppress("DEPRECATION")
    private val sdcard =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    private val targetFile: File
        get() = File(sdcard, "dst_cht.po")

    private val cacheFile: File
        get() = File(cacheDir, "dst_cht.po")

    @Suppress("RemoveExplicitTypeArguments")
    private fun loadSdCardFile(name: String): ArrayList<WordEntry> {
        Log.d(TAG, "load sd card: $name")
        val file = File(sdcard, name)
        val list: ArrayList<WordEntry> = if (file.exists()) try {
            val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
            loadFromReader(reader, true)
        } catch (e: Exception) {
            ArrayList<WordEntry>()
        } else {
            ArrayList()
        }
        Log.d(TAG, "SD: done with $name (${list.size})")
        return list
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun loadAssetFile(name: String, line2Enabled: Boolean = true): ArrayList<WordEntry> {
        var list: ArrayList<WordEntry> = loadSdCardFile(name)
        if (list.isEmpty()) {
            Log.d(TAG, "load asset: $name")
            val reader = BufferedReader(InputStreamReader(assets.open(name)))
            list = try {
                loadFromReader(reader, line2Enabled)
            } catch (e: Exception) {
                ArrayList<WordEntry>()
            }
            Log.d(TAG, "A: done with $name (${list.size})")
        }
        return list
    }

    private fun loadFromReader(
        reader: BufferedReader,
        line2Enabled: Boolean = true
    ): ArrayList<WordEntry> {
        val list = ArrayList<WordEntry>()
        try {
            // do reading, usually loop until end of file reading
            var line: String? = ""//reader.readLine()
            while (line != null) {
                //Log.d(TAG, "${line.length}: $line")
                val line1 = reader.readLine()
                if (!line1.startsWith("#")) {
                    Log.d(TAG, "bypass $line1")
                    continue //bypass some invalid header
                }
                val line2 = if (line2Enabled) reader.readLine() else ""
                val line3 = reader.readLine()
                val line4 = reader.readLine()
                val entry = WordEntry.from(line1, line2, line3, line4)
                if (entry != null) {
                    //Log.d(TAG, "entry: ${entry.id}")
                    list.add(entry)
                } else {
                    Log.e(TAG, "invalid input: $line1")
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                Log.e(TAG, "close: ${e.message}")
            }
        }
        return list
    }

    private fun writeEntryToFile(dst: File, list: ArrayList<WordEntry>): Boolean {
        val writer: BufferedWriter?
        try { //http://stackoverflow.com/a/1053474
            writer = BufferedWriter(FileWriter(dst))
            var content = "\"Language: zh-tw\"\n\"POT Version: 2.0\"\n"
            writer.write(content, 0, content.length)
            list.forEach { entry ->
                content = "\n"
                content += "#. ${entry.key}\n"
                content += "msgctxt ${entry.text}\n"
                content += "msgid ${entry.id}\n"
                content += "msgstr ${entry.str}\n"
                writer.write(content, 0, content.length)
            }
            writer.close()
            //writer = null
        } catch (e: Exception) {
            //e.printStackTrace()
            Log.e(TAG, "writeStringToFile: ${e.message}")
            return false
        }
        Log.d(TAG, "write to ${dst.absolutePath} with ${dst.length()} done")
        return true
    }

    private class ItemViewHolder(
        view: View?,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
    ) : FlexibleViewHolder(view, adapter) {
        val key: TextView? = view?.findViewById(android.R.id.title)
        val srcText: TextView? = view?.findViewById(android.R.id.custom)
        val text1: TextView? = view?.findViewById(android.R.id.text1)
        val text2: TextView? = view?.findViewById(android.R.id.text2)
        val message: TextView? = view?.findViewById(android.R.id.message)
    }

    private class EntryItemView(
        val origin: WordEntry,
        val old: WordEntry? = null,
        val src: WordEntry? = null
    ) : AbstractFlexibleItem<ItemViewHolder>() {
        // make a copy here that we have chance to revert back
        val entry: WordEntry = origin.copy()

        override fun bindViewHolder(
            adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?,
            holder: ItemViewHolder?,
            position: Int,
            payloads: MutableList<Any>?
        ) {
            holder?.apply {
                key?.text = entry.key
                srcText?.text = src?.id
                // val same = old == null || old == entry
                // text1?.visibility = if (same) View.GONE else View.VISIBLE
                text1?.text = if (old?.id == entry.id) old.str else old?.id ?: entry.id
                text2?.text = entry.str
                message?.text = origin.str
                // if (same || old?.str == entry.str) origin.str else entry.id
            }
        }

        override fun equals(other: Any?): Boolean {
            return (other as? EntryItemView)?.entry?.key == this.entry.key
        }

        override fun createViewHolder(
            view: View?,
            adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>?
        ): ItemViewHolder = ItemViewHolder(view, adapter)

        override fun getLayoutRes(): Int = R.layout.item_entry

        override fun hashCode(): Int = entry.hashCode()
    }

    private val list = ArrayList<EntryItemView>()
    private var dataAdapter: FlexibleAdapter<*>? = null

    private fun showChangeList() {
        list.clear()
        wordList.filter { entry ->
            val origin = originMap[entry.key]
            (entry.newly || origin?.id != entry.id || origin.str != entry.str)
                    && entry.str.length > 2
        }.forEach { entry ->
            list.add(
                EntryItemView(
                    origin = entry,
                    old = originMap[entry.key],
                    src = sourceMap[entry.key]
                )
            )
        }
        dataAdapter = FlexibleAdapter(list, object : FlexibleAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int): Boolean {
                //Log.d(TAG, "click ${(view?.tag as? WordEntry)?.key}")
                editIndex = position
                showEntryEditor(list[position].entry, list[position].old)
                return true
            }
        })
        findViewById<RecyclerView>(android.R.id.list)?.apply {
            adapter = dataAdapter
            setHasFixedSize(true)
            layoutManager = SmoothScrollLinearLayoutManager(this@MainActivity)
        }
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
            wordList.find { entry -> entry.key == key }?.apply {
                str = textField.editText?.text?.toString() ?: str
            }
            dataAdapter?.notifyItemChanged(editIndex)
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

    private var editIndex: Int = 0

    private fun showEntryEditor(entry: WordEntry, origin: WordEntry?) {
        Log.d(TAG, "entry: ${entry.id} ${entry.str}")
        Log.d(TAG, "origin: ${origin?.id} ${origin?.str}")
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
            text = sc2tc(sourceMap[entry.key]?.str ?: "")
            isEnabled = text?.isNotEmpty() == true
            tag = text
        }
        val str = wordList.find { it.key == entry.key }?.str ?: entry.str // use dictionary
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
