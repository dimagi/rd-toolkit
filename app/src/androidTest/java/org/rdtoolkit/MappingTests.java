package org.rdtoolkit;

import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.rdtoolkit.support.interop.BundleToStringMap;
import org.rdtoolkit.support.interop.FixedSetKeyQualifier;
import org.rdtoolkit.support.interop.PrefixKeyQualifier;
import org.rdtoolkit.support.interop.StringIdentityMapper;
import org.rdtoolkit.support.interop.StringMapToBundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class MappingTests {
    HashMap<String, String> map = new HashMap();
    Bundle bundle = new Bundle();



    @Before
    public void setup() {
        map = new HashMap();
        bundle = new Bundle();
        map.put("test", "value");
        map.put("test2", "value2");

        bundle.putString("a_key_one", "ako");
        bundle.putString("a_key_two", "akt");
        bundle.putString("b_key_one", "bko");
    }

    @Test
    public void testStringMapToBundle() {
        Bundle b = new StringMapToBundle().map(map);
        for (String key : map.keySet()) {
            Assert.assertEquals(map.get(key), b.getString(key));
        }
    }

    @Test
    public void testIdentityMapper() {
        StringIdentityMapper m = new StringIdentityMapper();
        Assert.assertEquals("value",m.map("value"));
    }

    @Test
    public void testBundleToStringMapUnqualified() {
        Map<String, String> map = new BundleToStringMap().map(bundle);

        for (String key : bundle.keySet()) {
            Assert.assertEquals(bundle.getString(key), map.get(key));
        }
    }

    @Test
    public void testBundleToStringMapListQualifier() {
        Set<String> keySet = new HashSet();
        keySet.add("a_key_one");
        keySet.add("b_key_one");

        Map<String, String> map =
                new BundleToStringMap(new FixedSetKeyQualifier(keySet)).map(bundle);
        Assert.assertEquals(keySet.size(), map.size());

        for (String key : keySet) {
            Assert.assertEquals(bundle.getString(key), map.get(key));
        }
    }

    @Test
    public void testBundleToStringMapPrefixQualifier() {
        Map<String, String> map = new BundleToStringMap(new PrefixKeyQualifier("a_")).map(bundle);

        Set<String> matchingKeySet = new HashSet();
        matchingKeySet.add("a_key_one");
        matchingKeySet.add("a_key_two");

        Assert.assertEquals(2, map.size());

        for (String key : matchingKeySet) {
            Assert.assertEquals(bundle.getString(key), map.get(key));
        }
    }
}