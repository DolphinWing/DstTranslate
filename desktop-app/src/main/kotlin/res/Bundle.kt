package res

import java.util.ListResourceBundle

class Bundle : ListResourceBundle() {
    override fun getContents(): Array<Array<Any>> = arrayOf(
        arrayOf("hello", "Hello"),
    )
}