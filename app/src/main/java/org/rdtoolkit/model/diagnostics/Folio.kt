package org.rdtoolkit.model.diagnostics

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * A Folio is a simple wrapper object to streamline and compartmentalize translations
 * and multimedia files
 */
interface Folio {
    fun getText(key: String) : String
    fun getPamphlet() : Pamphlet
    fun setLocale(locale: String)
    fun validate()
}

interface Pamphlet {
    fun getTitle() : String?
    fun getPages() : List<Page>
}

interface Page {
    fun getText() : String
    fun getConfirmationText() : String?
    fun getImageStream() : InputStream?
}

interface FolioContext {
    fun checkForFile(ref : String) : Boolean
    fun getStream(ref: String) : InputStream

    fun buffer(ref : String): ByteArray {
        return getStream(ref).use {

            val buffer = ByteArrayOutputStream()
            val data = ByteArray(1024)
            var nRead: Int = it.read(data, 0, data.size)
            while (nRead != -1) {
                buffer.write(data, 0, nRead)
                nRead = it.read(data, 0, data.size)
            }

            buffer.flush()
            return buffer.toByteArray()
        }
    }

    fun spool(ref : String) : String {
        return String(buffer(ref), Charset.forName("UTF-8"))
    }
}

class FileFolioContext(private val fileRoot: File) : FolioContext {

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

class ZipStreamFolioContext(private val wrappedContext: FolioContext,
                            private val wrappedContextRef : String) : FolioContext {

    fun zipStream(ref: String) : ZipInputStream {
        return ZipInputStream(wrappedContext.getStream(wrappedContextRef))
    }

    override fun checkForFile(ref: String) : Boolean {
        return zipStream(ref).use {
            var nextEntry = it.nextEntry
            while(nextEntry != null) {
                if(ref.equals(nextEntry.name)) {
                    return true
                }
                nextEntry = it.nextEntry
            }
            return false
        }
    }

    override fun getStream(ref: String): InputStream {
        val stream = zipStream(ref)
        var returned = false
        try {
            var nextEntry = stream.nextEntry
            while(nextEntry != null) {
                if(ref.equals(nextEntry.name)) {
                    returned = true
                    return stream
                }
                nextEntry = stream.nextEntry
            }
            throw InvalidFolioException("Invalid resource reference $ref in wrapped zip folio into $wrappedContextRef")
        } finally {
            if(!returned) {
                stream.close()
            }
        }
    }
}

class ZipFileFolioContext(private val root : ZipFile) : FolioContext {

    override fun checkForFile(ref: String) : Boolean {
        return root.getEntry(ref) != null
    }

    override fun getStream(ref: String): InputStream {
        return root.getInputStream(root.getEntry(ref))
    }
}

class JavaResourceFolioContext(private var resourcePrefix: String) : FolioContext {

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

interface PamphletSource {
    fun getMatchingPamphlets(category : String, tags: List<String>) : List<Pamphlet>
}


class InvalidFolioException(message : String?) : Exception(message) {

}

class FolioRep  (
        private val dictionaryData : HashMap<String, Map<String,String>>,
        private val pamphlet: PamphletRep,
        private val folioContextRep: FolioContext
) : Folio {
    private var currentLocale : String
    private val defaultLocale : String = dictionaryData.keys.first()


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

    override fun validate() {
        pamphlet.validate()
    }

    init {
        currentLocale = defaultLocale
        pamphlet.seal(this)
    }
}

data class PamphletRep(
        private val pagesRep : List<PageRep>
) : Pamphlet {
    private lateinit var folio : FolioRep

    override fun getTitle(): String? {
        TODO("Not yet implemented")
    }

    override fun getPages(): List<Page> {
        return pagesRep
    }

    fun validate() {
        for (page in pagesRep) {
            page.validate()
        }
    }

    fun seal(folio: FolioRep) {
        this.folio = folio
        for (page in pagesRep) {
            page.seal(folio)
        }
    }
}

data class PageRep(
        private val textKey : String,
        private val imageRef: String?
) : Page {
    private lateinit var folioRep:  FolioRep
    override fun getText() : String {
        return folioRep.getText(textKey)
    }
    override fun getImageStream() : InputStream? {
        return imageRef?.let { folioRep.getFolioContext().getStream(imageRef) }
    }

    override fun getConfirmationText() : String? {
        //Only supported on manual pages for now
        return null
    }

    fun seal(folio: FolioRep) {
        this.folioRep = folio
        folioRep.getText(textKey)
    }

    fun validate() {
        folioRep.getText(textKey)
        if (imageRef != null && !folioRep.getFolioContext().checkForFile(imageRef)) {
            throw InvalidFolioException("Could not find referenced entry $imageRef")
        }
    }
}
