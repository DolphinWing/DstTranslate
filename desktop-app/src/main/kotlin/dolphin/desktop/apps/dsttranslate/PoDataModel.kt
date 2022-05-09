package dolphin.desktop.apps.dsttranslate

import dolphin.android.apps.dsttranslate.WordEntry
import dolphin.desktop.apps.dsttranslate.compose.Configs
import dolphin.desktop.apps.dsttranslate.compose.EditorSpec
import dolphin.desktop.apps.dsttranslate.compose.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

class PoDataModel(val helper: DesktopPoHelper) {
    val configs = MutableStateFlow(Configs())
    val filteredList = MutableStateFlow(emptyList<WordEntry>())
    val changedList = MutableStateFlow(emptyList<Long>())
    val searchType = MutableStateFlow(SearchType.Key)
    val searchText = MutableStateFlow("")
    val searchList = MutableStateFlow(emptyList<WordEntry>())
    val suspectMap = MutableStateFlow(SuspectMap())

    /**
     * Load Ini and Po files from disk
     */
    suspend fun loadIniAndPo() = withContext(Dispatchers.IO) {
        helper.loadXml() // setup replacement at launch
        configs.emit(Configs(helper.ini))
        helper.runTranslationProcess() // setup replacement at launch
        refreshDataSource() // loadIniAndPo
        searchList.emit(helper.allValues()) // loadIniAndPo
    }

    /**
     * Save configs to disk
     *
     * @param configs new config
     */
    suspend fun saveConfig(configs: Configs) = withContext(Dispatchers.IO) {
        helper.ini.apply(
            workingDir = configs.workshopDir,
            assetsDir = configs.assetsDir,
            stringMap = configs.stringMap,
        )
        loadIniAndPo()
    }

    /**
     * Run translation process
     *
     * @return cost time of translation process
     */
    suspend fun translate(): Long = withContext(Dispatchers.IO) {
        val cost = helper.runTranslationProcess() // setup replacement at launch
        refreshDataSource() // translate
        return@withContext cost
    }

    /**
     * Edit the value
     *
     * @param key entry key
     * @param value new entry value
     */
    suspend fun edit(key: String, value: String) {
        helper.update(key, value)
        refreshDataSource() // edit
    }

    private suspend fun refreshDataSource() {
        val list = ArrayList<Long>()
        val filtered = helper.buildChangeList()
        filtered.forEach { item -> list.add(item.changed) }
        filteredList.emit(filtered)
        changedList.emit(list)
    }

    /**
     * Export translation file
     *
     * @param cacheIt true if we want just to cache the file
     * @return output path and time cost. if export file failed, time cost will be negative.
     */
    suspend fun save(cacheIt: Boolean = false): Pair<String, Long> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val exported = helper.getOutputFile(cacheIt)
        val result = helper.writeTranslationFile(exported)
        val cost = System.currentTimeMillis() - start
        return@withContext Pair(exported.absolutePath, if (result) cost else -1)
    }

    /**
     * Change search type
     *
     * @param type new search type
     */
    suspend fun searchType(type: SearchType) {
        searchType.emit(type)
        search(searchText.value)
    }

    /**
     * Search text in map
     *
     * @param text target text
     * @param type search type
     */
    suspend fun search(text: String, type: SearchType = searchType.value) {
        searchText.emit(text)
        searchList.emit(helper.allValues().filter { item ->
            when (type) {
                SearchType.Origin -> item.origin()
                SearchType.Key -> item.key()
                SearchType.Text -> item.string()
            }.contains(text, ignoreCase = true)
        })
    }

    /**
     * Analyze the dictionary
     *
     * @return the number of suspects
     */
    suspend fun analyze(): Int {
        val (result, suspects) = helper.analyzeText()
        suspectMap.emit(suspects)
        return result
    }

    /**
     * Make a new [EditorSpec] to editor
     *
     * @param entry target word
     * @return new entry to editor
     */
    fun requestEdit(entry: WordEntry): EditorSpec = EditorSpec(
        entry,
        helper.dst(entry.key),
        helper.sc2tc(helper.chs(entry.key)?.str ?: ""),
        helper.cht(entry.key)?.str,
    )
}