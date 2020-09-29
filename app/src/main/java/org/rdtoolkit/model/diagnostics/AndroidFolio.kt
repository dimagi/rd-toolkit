package org.rdtoolkit.model.diagnostics

import android.content.res.AssetManager
import java.io.File
import java.io.InputStream

class AssetFolioContext(private val path: String,
                        private val manager : AssetManager) : FolioContext {

    override fun checkForFile(ref: String) : Boolean {
        return manager.list(path)!!.contains(ref)
    }

    override fun getStream(ref: String): InputStream {
        return manager.open(path + File.separator + ref)
    }

    init {
        if(manager.list(path) == null) {
            throw InvalidFolioException("Invalid folio context, no path $path in assets")
        }
    }
}
