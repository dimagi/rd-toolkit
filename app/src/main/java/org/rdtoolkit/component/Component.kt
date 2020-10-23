package org.rdtoolkit.component

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import org.rdtoolkit.R
import org.rdtoolkit.model.diagnostics.Pamphlet
import java.io.File

val TAG_READINESS_PRODUCTION = "production"
val TAG_READINESS_AVAILABLE = "available"

val VALUE_DEFAULT = 1

val VALUE_PREFERRED = 3

/**
 * Components are the digital processing units for test capture and analysis.
 * The component base class provides the appropriate hooks for managing UI lifecycles
 * related to each type of activity
 */
abstract class Component {
    protected var activity : Activity? = null
    private var listener: ComponentEventListener? = null
    protected var scope: CoroutineScope? = null
    var componentInterfaceId : Int? = null

    fun register(activity: Activity, listener : ComponentEventListener,
                 scope : CoroutineScope, componentId: Int) {
        this.activity = activity
        this.listener = listener
        this.scope = scope
        this.componentInterfaceId = componentId

        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(
                    activity!!, getRequiredPermissions(), componentId)

        }
    }
    open fun unregister() {
        this.activity = null
        this.listener = null
        this.componentInterfaceId = null
    }

    protected open fun getRequiredPermissions() : Array<String> {
        return arrayOf()
    }

    protected fun getListener() : ComponentEventListener {
        if (listener == null) {
            throw Exception("Request for listener on deregistered component")
        }
        return listener!!
    }

    protected fun hasAllPermissions() : Boolean {
        with(getRequiredPermissions()) {
            return if (this.isEmpty()) {
                true
            } else {
                this.all {
                    ContextCompat.checkSelfPermission(
                            activity!!, it) == PackageManager.PERMISSION_GRANTED

                }
            }
        }
    }
}

class ComponentManager(private val activity : AppCompatActivity,
                       private val listener : ComponentEventListener)  : LifecycleObserver {
    var managedComponents = ArrayList<Component>()
    val activityCodeBaseId : Int = 100

    fun deregisterComponents() {
        for (component in managedComponents) {
            component.unregister()
        }
        this.managedComponents.clear()
    }

    fun registerComponents(vararg components: Component) {
        deregisterComponents()
        for (component in components) {
            component.register(activity, listener, activity.lifecycleScope, activityCodeBaseId + managedComponents.size)
            managedComponents.add(component)
        }
    }

    fun notifyIntentCallback(requestCode : Int, resultCode : Int, data : Intent?) {
        for (component in managedComponents) {
            if (component is ActivityLifecycleComponent) {
                component.processIntentCallback(requestCode, resultCode, data)
            }
        }
    }

    fun getCaptureComponent() : TestImageCaptureComponent {
        for (component in managedComponents) {
            if (component is TestImageCaptureComponent) {
                return component
            }
        }
        throw Exception("No Capture Component available")
    }

    fun getImageClassifierComponent() : ImageClassifierComponent? {
        for (component in managedComponents) {
            if (component is ImageClassifierComponent) {
                return component
            }
        }
        return null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        deregisterComponents()
    }

    init {
        activity.lifecycle.addObserver(this)
    }
}

class Sandbox(val context : Context, val sessionId: String) {
    /**
     * Get the file system location for any files that need to be stored by the component
     */
    fun getFileRoot() : File {
        val externalDir = File(context.getExternalFilesDir(DIRECTORY_SESSIONS),sessionId)
        externalDir.mkdirs()
        return externalDir
    }

    companion object {
        const val DIRECTORY_SESSIONS = "session_media"
    }
}

interface Config {

}

class NoConfig : Config {

}

val CAPTURE_TYPE_PLAIN = "plain"
val CAPTURE_TYPE_RETICLE = "reticle"

open class ImageCaptureResult(private val imagePath : String) {
    open fun getCaptureType() : String {
        return CAPTURE_TYPE_PLAIN
    }

    open fun getImages() : Pair<String, Map<String, String>> {
        return Pair(imagePath, mapOf("raw" to imagePath))
    }
}

class ReticleCaptureResult(val rawImagePath : String,
                           val croppedImagePath : String,
                           val reticleOffset : Rect) : ImageCaptureResult(rawImagePath) {
    override fun getCaptureType() : String {
        return CAPTURE_TYPE_RETICLE
    }
    override fun getImages() : Pair<String, Map<String, String>> {
        return Pair(croppedImagePath, mapOf("raw" to rawImagePath,
                "cropped" to croppedImagePath))
    }
}

interface ComponentEventListener {
    fun testImageCaptured(imagePath : ImageCaptureResult)

    fun onClassifierError(error: String, details: Pamphlet?)
    fun onClassifierComplete(results: Map<String, String>)
}

interface ToolkitComponentManifest<C : Component, G> {
    fun getTagsForDiagnostic(diagnosticId: String) : Set<String>

    fun getConfigForDiagnostic(diagnosticId: String) : G {
        return NoConfig() as G
    }

    fun getComponent(config: G, sandbox : Sandbox) : C

    fun getDownstreamTags() : Set<String> {
        return setOf()
    }

    fun getValue() : Int {
        return VALUE_DEFAULT
    }

    fun getCompatibleOutputs(diagnosticId: String) : Set<String> {
        return setOf()
    }
}

interface ActivityLifecycleComponent {
    fun processIntentCallback(requestCode : Int, resultCode : Int, data : Intent?)
}

abstract class TestImageCaptureComponent : Component() {
    abstract fun getResultImage() : ImageCaptureResult
    abstract fun captureImage()
}

abstract class ImageClassifierComponent : Component() {
    private var currentJob : Job? = null

    fun doImageProcessing(inputResult : ImageCaptureResult) {
        scope!!.launch {
            currentJob?.let {
                it.cancelAndJoin()
            }
            currentJob = launch(Dispatchers.Default) {
                try{
                    processImage(inputResult)
                } catch(e: Exception){
                    e.printStackTrace()
                    getListener().onClassifierError(activity?.getString(R.string.component_classifier_unknown_error)?.format(e.message)!!, null);
                }
            }
        }
    }

    protected abstract suspend fun processImage(inputResult : ImageCaptureResult)

    open fun compatibleCaptureModes() : List<String> {
        return listOf(CAPTURE_TYPE_PLAIN)
    }


    override fun unregister() {
        super.unregister()
        currentJob?.let {
            it.cancel()
        }
    }

}