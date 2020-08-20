package org.rdtoolkit;

import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rdtoolkit.interop.BundleToConfiguration;
import org.rdtoolkit.interop.translator.InteropRepository;
import org.rdtoolkit.model.session.ProvisionMode;
import org.rdtoolkit.model.session.SessionMode;
import org.rdtoolkit.model.session.TestSession;

import static org.rdtoolkit.interop.InterfacesKt.INTENT_EXTRA_RDT_CONFIG_BUNDLE;
import static org.rdtoolkit.interop.IntentObjectMappingsKt.INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE;
import static org.rdtoolkit.interop.IntentObjectMappingsKt.INTENT_EXTRA_RDT_PROVISION_MODE;
import static org.rdtoolkit.interop.IntentObjectMappingsKt.INTENT_EXTRA_RDT_PROVISION_MODE_DATA;
import static org.rdtoolkit.interop.translator.TranslatorsKt.TRANSLATOR_PROVISION_FLAT;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FlatInputTranslatorTest {
    @Test
    public void testStructure() {
        Intent testIntent = new Intent();
        testIntent.putExtra(INTENT_EXTRA_RDT_PROVISION_MODE, ProvisionMode.TEST_PROFILE.toString());
        testIntent.putExtra(INTENT_EXTRA_RDT_PROVISION_MODE_DATA, "TestType");
        testIntent.putExtra(INTENT_EXTRA_RDT_CONFIG_SESSION_TYPE, SessionMode.ONE_PHASE.toString());

        Intent output = new InteropRepository().getTranslator(TRANSLATOR_PROVISION_FLAT).map(testIntent);

        Assert.assertTrue(output.hasExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE));

        TestSession.Configuration config =
                new BundleToConfiguration().map(output.getBundleExtra(INTENT_EXTRA_RDT_CONFIG_BUNDLE));

        Assert.assertEquals(SessionMode.ONE_PHASE, config.getSessionType());
    }

}