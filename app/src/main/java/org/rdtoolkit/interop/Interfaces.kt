package org.rdtoolkit.interop

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import org.rdtoolkit.interop.translator.InteropRepository
import org.rdtoolkit.support.interop.FilePathMapper
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_INPUT_TRANSLATOR
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_CONFIG_BUNDLE
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_SESSION_BUNDLE
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_SESSION_ID
import org.rdtoolkit.support.interop.SessionToBundle
import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.ui.provision.ProvisionActivity
import java.io.File


fun provisionIntent(context : Context, incoming: Intent) : Intent {
    var toBootstrap = incoming
    incoming.getStringExtra(INTENT_EXTRA_INPUT_TRANSLATOR)?.let {
        toBootstrap = InteropRepository().getTranslator(it).map(incoming)
        toBootstrap.putExtra(INTENT_EXTRA_RDT_SESSION_ID,
                incoming.getStringExtra(INTENT_EXTRA_RDT_SESSION_ID))
    }
    var toReturn = Intent(context, ProvisionActivity::class.java)
    bootstrap(toReturn, toBootstrap)

    return toReturn
}

fun bootstrap(newIntent: Intent, oldIntent: Intent) {
    newIntent.putExtra(INTENT_EXTRA_RDT_SESSION_ID,
            oldIntent.getStringExtra(INTENT_EXTRA_RDT_SESSION_ID))

    newIntent.putExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE,
            oldIntent.getBundleExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE))

}

fun provisionReturnIntent(session : TestSession) : Intent {
    val intent = Intent()
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_BUNDLE, SessionToBundle().map(session))
    return intent
}

fun captureReturnIntent(session : TestSession, filePathWrapper : FilePathMapper) : Intent {
    val intent = Intent()
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_ID, session.sessionId)
    intent.putExtra(INTENT_EXTRA_RDT_SESSION_BUNDLE, SessionToBundle(filePathWrapper).map(session))

    filePathWrapper.prepare(intent)

    return intent
}

fun getFileEncodingMapper(context : Context, useUriByDefault : Boolean = false) : FilePathMapper {
    val useUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || useUriByDefault
    if (useUri) {
        return FileUriResponseMapper(context)
    } else {
        return FilePathMapper()
    }
}

class FileUriResponseMapper(val context : Context) : FilePathMapper() {
    override fun map(input: String?): String? {
        val fileUri: Uri? = input?.let {
            FileProvider.getUriForFile(
                    context,
                    "org.rdtoolkit.fileprovider",
                    File(input))
        }

        return fileUri?.toString().also { it?.let { this.outputs.add(it) } }
    }

    override fun prepare(intent: Intent) {
        if (outputs.isNotEmpty()) {
            val i = outputs.iterator()
            val clipData = ClipData.newRawUri(null, Uri.parse(i.next()))
            while (i.hasNext()) {
                clipData.addItem(ClipData.Item(Uri.parse(i.next())))
            }

            intent.clipData = clipData
        }
    }
}