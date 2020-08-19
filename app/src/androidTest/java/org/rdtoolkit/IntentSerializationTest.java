package org.rdtoolkit;

import android.content.Context;
import android.content.Intent;

import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rdtoolkit.interop.BundleToConfiguration;
import org.rdtoolkit.interop.BundleToResult;
import org.rdtoolkit.interop.BundleToSession;
import org.rdtoolkit.interop.ConfigurationToBundle;
import org.rdtoolkit.interop.ResultToBundle;
import org.rdtoolkit.interop.SessionToBundle;
import org.rdtoolkit.model.Mapper;
import org.rdtoolkit.model.session.TestSession;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class IntentSerializationTest {

    Map<Class, Pair<Mapper<Object, Intent>, Mapper<Intent,Object>>> serializers = new HashMap();
    {
        serializers.put(TestSession.TestResult.class, new Pair(new ResultToBundle(), new BundleToResult()));
        serializers.put(TestSession.Configuration.class, new Pair(new ConfigurationToBundle(), new BundleToConfiguration()));
        serializers.put(TestSession.class, new Pair(new SessionToBundle(), new BundleToSession()));
    }
    @Test
    public void testResultSerialization() {
        testRoundTrip(Constants.TestResultNoValues);
        testRoundTrip(Constants.TestResultsSampleValues);
    }

    @Test
    public void testConfigSerialization() {
        testRoundTrip(Constants.ConfigMinimal);
        testRoundTrip(Constants.ConfigNormal);
    }

    @Test
    public void testSessionSerialization() {
        testRoundTrip(Constants.SessionProvisioned);
        testRoundTrip(Constants.SessionCompleted);
    }

    private <T> void testRoundTrip(T t) {
        Pair<Mapper<Object, Intent>, Mapper<Intent,Object>> pair = serializers.get(t.getClass());
        Assert.assertEquals(t, pair.second.map(pair.first.map((Object)t)));
    }
}