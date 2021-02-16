package org.rdtoolkit

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.rdtoolkit.support.interop.BundleToConfiguration
import org.rdtoolkit.support.interop.RdtIntentBuilder
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.INTENT_EXTRA_RDT_CONFIG_BUNDLE
import org.rdtoolkit.support.interop.RdtIntentBuilder.Companion.forProvisioning
import org.rdtoolkit.support.interop.RdtProvisioningIntentBuilder
import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.support.model.session.isCloudworksActive
import org.rdtoolkit.support.model.session.isComprehensiveImageSubmissionEnabled
import org.rdtoolkit.support.model.session.isTraceEnabled
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BuilderConfigTests {

    @Test
    fun testBuilderOutputs() {
        val cloudworksDefaults = cloudworks().config()
        Assert.assertTrue(cloudworksDefaults.isCloudworksActive())
        Assert.assertTrue(cloudworksDefaults.isTraceEnabled())
        Assert.assertFalse(cloudworksDefaults.isComprehensiveImageSubmissionEnabled())

        Assert.assertFalse(cloudworks().setCloudworksTraceEnabled(false).config().isTraceEnabled())
        Assert.assertTrue(cloudworks().setCloudworksTraceEnabled(true).config().isTraceEnabled())

        Assert.assertFalse(cloudworks().setSubmitAllImagesToCloudworks(false).config().isComprehensiveImageSubmissionEnabled())
        Assert.assertTrue(cloudworks().setSubmitAllImagesToCloudworks(true).config().isComprehensiveImageSubmissionEnabled())
    }

    fun cloudworks(): RdtProvisioningIntentBuilder {
        return RdtIntentBuilder.forProvisioning().requestTestProfile("test").setCloudworksBackend("test")
    }

    fun RdtProvisioningIntentBuilder.config() : TestSession.Configuration {
        return BundleToConfiguration().map(
                this.build().getBundleExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE)!!)
    }
}