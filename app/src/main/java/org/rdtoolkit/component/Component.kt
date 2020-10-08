package org.rdtoolkit.component

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import org.rdtoolkit.R
import org.rdtoolkit.model.diagnostics.Pamphlet

val TAG_READINESS_PRODUCTION = "production"
val TAG_READINESS_AVAILABLE = "available"

val VALUE_DEFAULT = 1

val VALUE_PREFERRED = 3

abstract class Component {
    protected var activity : Activity? = null
    protected var listener: ComponentEventListener? = null
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
            managedComponents.add(component)
            component.register(activity, listener, activity.lifecycleScope, activityCodeBaseId + managedComponents.size)
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

interface Config {

}

class NoConfig : Config {

}

interface ComponentEventListener {
    fun testImageCaptured(imagePath : String)

    fun onClassifierError(error: String, details: Pamphlet?)
    fun onClassifierComplete(results: MutableMap<String, String>)
}

interface ToolkitComponentManifest<C : Component, G> {
    fun getTagsForDiagnostic(diagnosticId: String) : Set<String>

    fun getConfigForDiagnostic(diagnosticId: String) : G {
        return NoConfig() as G
    }

    fun getComponent(config: G) : C

    fun getDownstreamTags() : Set<String> {
        return setOf()
    }

    fun getValue() : Int {
        return VALUE_DEFAULT
    }
}

interface ActivityLifecycleComponent {
    fun processIntentCallback(requestCode : Int, resultCode : Int, data : Intent?)
}

abstract class TestImageCaptureComponent : Component() {
    abstract fun getResultImage() : String
    abstract fun captureImage()
}

abstract class ImageClassifierComponent : Component() {
    private var currentJob : Job? = null

    fun doImageProcessing(inputFilePath : String) {
        scope!!.launch {
            currentJob?.let {
                it.cancelAndJoin()
            }
            currentJob = launch {
                try{
                    processImage(inputFilePath)
                } catch(e: Exception){
                    e.printStackTrace()
                    listener?.let {
                        it.onClassifierError(activity?.getString(R.string.component_classifier_unknown_error)?.format(e.message)!!, null);
                    }
                }
            }
        }
    }

    protected abstract suspend fun processImage(inputFilePath : String)

    override fun unregister() {
        super.unregister()
        currentJob?.let {
            it.cancel()
        }
    }

}