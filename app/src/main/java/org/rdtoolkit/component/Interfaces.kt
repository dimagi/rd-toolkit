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

val TAG_READINESS_PRODUCTION = "production"
val TAG_READINESS_AVAILABLE = "available"

val VALUE_DEFAULT = 1

val VALUE_PREFERRED = 3

abstract class Component {
    protected var activity : Activity? = null
    protected var listener: ComponentEventListener? = null
    var componentInterfaceId : Int? = null

    fun register(activity: Activity, listener : ComponentEventListener, componentId: Int) {
        this.activity = activity
        this.listener = listener
        this.componentInterfaceId = componentId

        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(
                    activity!!, getRequiredPermissions(), componentId)

        }
    }
    fun unregister() {
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
            component.register(activity, listener, activityCodeBaseId + managedComponents.size)
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
}

interface ToolkitComponentManifest<C : Component, G> {
    fun getTagsForDiagnostic(diagnosticId: String) : Set<String>

    fun getConfigForDiagnostic(diagnosticId: String) : G {
        return NoConfig() as G
    }
    fun getComponent(config: G) : C

    fun getValue() : Int {
        return VALUE_DEFAULT
    }
}

interface ActivityLifecycleComponent {
    fun triggerCallout(activity: Activity)

    fun processIntentCallback(requestCode : Int, resultCode : Int, data : Intent?)
}

abstract class TestImageCaptureComponent : Component() {
    abstract fun getResultImage() : String
    abstract fun captureImage()
}

abstract class ImageClassifierComponent : Component() {

}