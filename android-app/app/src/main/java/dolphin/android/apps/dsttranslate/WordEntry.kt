package dolphin.android.apps.dsttranslate

data class WordEntry(
    val key: String,
    val text: String,
    val id: String,
    val str: String,
    val newly: Boolean = false
) {
    companion object {
        fun from(line1: String, line2: String, line3: String, line4: String): WordEntry? {
            val key = if (line1.startsWith("#.") or line1.startsWith("#:")) {
                line1.substring(3)
            } else line1
            val txt = if (line2.startsWith("msgctxt")) {
                line2.substring(8)
            } else line2
            val id = if (line3.startsWith("msgid")) {
                line3.substring(6)
            } else line3
            val str = if (line4.startsWith("msgstr")) {
                line4.substring(7)
            } else line4
            return if (key.isNotEmpty() and id.isNotEmpty() and str.isNotEmpty()) {
                WordEntry(key, txt, id, str)
            } else null
        }

//        fun from(line1: String, line3: String, line4: String): WordEntry? {
//            return from(line1, "", line3, line4)
//        }
    }

    override fun hashCode(): Int = id.hashCode() + str.hashCode()

    override fun equals(other: Any?): Boolean {
        val that = other as? WordEntry
        return (that?.key == this.key && that.id == this.id && that.str == this.str)
    }
}