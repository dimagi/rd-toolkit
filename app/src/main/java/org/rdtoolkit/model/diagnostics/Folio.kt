package org.rdtoolkit.model.diagnostics

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset

/**
 * A Folio is a simple wrapper object to streamline and compartmentalize translations
 * and multimedia files
 */
interface Folio {
    fun getText(key: String) : String
    fun getPamphlet() : Pamphlet
    fun setLocale(locale: String)
}

interface Pamphlet {
    fun getTitle() : String?
    fun getPages() : List<Page>
}

interface Page {
    fun getText() : String
    fun getImage() : String?
}

interface FolioContext {
    fun checkForFile(ref : String) : Boolean
    fun getStream(ref: String) : InputStream

    fun buffer(ref : String): ByteArray {
        with(getStream(ref)) {

            val buffer = ByteArrayOutputStream()
            val data = ByteArray(1024)
            var nRead: Int = this.read(data, 0, data.size)
            while (nRead != -1) {
                buffer.write(data, 0, nRead)
                nRead = this.read(data, 0, data.size)
            }

            buffer.flush()
            return buffer.toByteArray()
        }
    }

    fun spool(ref : String) : String {
        return String(buffer(ref), Charset.forName("UTF-8"))
    }
}

class FileFolioContext(val fileRoot: File) : FolioContext{

    private fun fileRef(ref : String) : File {
        return File(fileRoot, ref)
    }

    override fun checkForFile(ref: String) : Boolean {
        return fileRef(ref).exists()
    }

    override fun getStream(ref: String): InputStream {
        return fileRef(ref).inputStream()
    }

}

class JavaResourceFolioContext(var resourcePrefix: String) : FolioContext{

    override fun checkForFile(ref: String) : Boolean {
        var stream : InputStream? = null
        try {
            stream = this.javaClass.getResourceAsStream(resource(ref))
            return stream != null
        } finally {
            stream?.close()
        }
    }

    fun resource(ref: String) : String { return resourcePrefix + ref; }

    override fun getStream(ref: String): InputStream {
        val fullResource = resource(ref)
        return this.javaClass.getResourceAsStream(fullResource) ?:
            throw InvalidFolioException("Invalid resource reference $fullResource")
    }

    init {
        if (!resourcePrefix.endsWith("/")) {
            resourcePrefix = "$resourcePrefix/"
        }
        if (!resourcePrefix.startsWith("/")) {
            resourcePrefix = "/$resourcePrefix"
        }
    }
}

class InvalidFolioException(message : String?) : Exception(message) {

}

class FolioRep  (
    val dictionaryData : HashMap<String, Map<String,String>>,
    val pamphlet: PamphletRep,
    val folioContextRep: FolioContext
) : Folio {
    var currentLocale : String
    var defaultLocale : String


    override fun getText(key: String) : String {
        return dictFor(currentLocale)[key] ?: dictFor(defaultLocale)[key] ?:
          throw InvalidFolioException("Missing translation for $key")
    }

    private fun dictFor(key : String) : Map<String, String> {
        if (!dictionaryData.containsKey(key)) {
            throw InvalidFolioException("Missing locale $key")
        }
        return dictionaryData[key]!!
    }

    override fun getPamphlet(): Pamphlet {
        return pamphlet
    }

    override fun setLocale(locale: String) {
        if (dictionaryData.containsKey(locale)) {
            this.currentLocale = locale
        }
    }

    fun getFolioContext() : FolioContext {
        return folioContextRep
    }

    init {
        defaultLocale = dictionaryData.keys.first()
        currentLocale = defaultLocale
        pamphlet.seal(this)
    }
}

data class PamphletRep(
        val pagesRep : List<PageRep>
) : Pamphlet {
    private lateinit var folio : FolioRep

    override fun getTitle(): String? {
        TODO("Not yet implemented")
    }

    override fun getPages(): List<Page> {
        return pagesRep
    }

    fun seal(folio: FolioRep) {
        this.folio = folio
        for (page in pagesRep) {
            page.seal(folio)
        }
    }
}

data class PageRep(
        val textKey : String,
        val imageRef: String?
) : Page {
    private lateinit var folioRep:  FolioRep
    override fun getText() : String {
        return folioRep.getText(textKey)
    }
    override fun getImage() : String? {
        return imageRef
    }

    fun seal(folio: FolioRep) {
        this.folioRep = folio
        folioRep.getText(textKey)
    }
}
