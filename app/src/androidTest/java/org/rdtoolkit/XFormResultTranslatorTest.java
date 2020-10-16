package org.rdtoolkit;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rdtoolkit.interop.InterfacesKt;
import org.rdtoolkit.interop.translator.InteropRepository;

import java.util.Map;

import static org.rdtoolkit.support.interop.IntentObjectMappingsKt.INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE;
import static org.rdtoolkit.support.interop.IntentObjectMappingsKt.INTENT_EXTRA_RDT_RESULT_MAP;
import static org.rdtoolkit.interop.translator.TranslatorsKt.TRANSLATOR_XFORM_RESULT;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class XFormResultTranslatorTest {

    @Test
    public void testStructure() {
        Intent testIntent = new Intent();
        testIntent.putExtra("base_level", "one");

        Bundle b = new Bundle();
        b.putLong("second_level", 2L);
        testIntent.putExtra("test_nested", b);

        Bundle btwo = new Bundle();
        btwo.putLong("second_level", 3L);
        testIntent.putExtra(INTENT_EXTRA_RDT_RESULT_MAP, btwo);

        Intent output = new InteropRepository().getTranslator(TRANSLATOR_XFORM_RESULT).map(testIntent);

        Assert.assertTrue(output.hasExtra("odk_intent_bundle"));
        Assert.assertFalse(output.hasExtra("base_level"));
        Assert.assertFalse(output.hasExtra("test_nested"));

        Bundle xformBundle = output.getBundleExtra("odk_intent_bundle");
        Assert.assertEquals("one", xformBundle.getString("base_level"));
        Assert.assertEquals("2", xformBundle.getString("second_level"));
        Assert.assertEquals("3", xformBundle.getString("result_second_level"));
    }

    @Test
    public void testWithSession() {
        Intent sessionIntent = InterfacesKt.captureReturnIntent(Constants.SessionCompleted);
        Intent output = new InteropRepository().getTranslator(TRANSLATOR_XFORM_RESULT).map(sessionIntent);
        Bundle xformBundle = output.getBundleExtra("odk_intent_bundle");

        Assert.assertEquals(xformBundle.getString(INTENT_EXTRA_RDT_CONFIG_FLAVOR_TEXT_ONE),
                Constants.SessionCompleted.getConfiguration().getFlavorText());

        Map<String, String> results = Constants.SessionCompleted.getResult().getResults();

        for (String key : results.keySet()) {
            Assert.assertEquals(results.get(key), xformBundle.getString(String.format("result_%s",key)));
        }
    }

}