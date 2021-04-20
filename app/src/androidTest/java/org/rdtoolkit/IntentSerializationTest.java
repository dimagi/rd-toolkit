package org.rdtoolkit;

import android.content.Intent;

import androidx.core.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rdtoolkit.support.interop.BundleToConfiguration;
import org.rdtoolkit.support.interop.BundleToResult;
import org.rdtoolkit.support.interop.BundleToSession;
import org.rdtoolkit.support.interop.ConfigurationToBundle;
import org.rdtoolkit.support.interop.ResultToBundle;
import org.rdtoolkit.support.interop.SessionToBundle;
import org.rdtoolkit.support.model.Mapper;
import org.rdtoolkit.support.model.session.TestSession;

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

    @Test
    public void testFilePathTransforms() {
        TestSession.TestResult roundTripResult = new BundleToResult().map(new ResultToBundle(new ReversingStringMapper()).map(Constants.TestResultsSampleValues));
        Assert.assertEquals("htaptset", roundTripResult.getMainImage());
        Assert.assertEquals("htaptset", roundTripResult.getImages().get("raw"));

        TestSession roundTripSession = new BundleToSession().map(new SessionToBundle(new ReversingStringMapper()).map(Constants.SessionCompleted));
        Assert.assertEquals("htaptset", roundTripSession.getResult().getMainImage());
        Assert.assertEquals("htaptset", roundTripSession.getResult().getImages().get("raw"));
    }


    private <T> void testRoundTrip(T t) {
        Pair<Mapper<Object, Intent>, Mapper<Intent,Object>> pair = serializers.get(t.getClass());
        Assert.assertEquals(t, pair.second.map(pair.first.map((Object)t)));
    }
}
