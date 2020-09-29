package org.rdtoolkit;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.rdtoolkit.model.diagnostics.AssetFolioContext;
import org.rdtoolkit.model.diagnostics.Folio;
import org.rdtoolkit.model.diagnostics.JavaResourceFolioContext;
import org.rdtoolkit.model.diagnostics.ZipStreamFolioContext;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.rdtoolkit.model.diagnostics.FolioJsonKt.parseFolio;

@RunWith(AndroidJUnit4.class)
public class AndroidFolioTests {

    @Test
    public void testBasicAssetAccess() throws IOException {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        appContext.getAssets().list("");

        AssetFolioContext context = new AssetFolioContext("basic", appContext.getAssets());
        Folio f = parseFolio(context.spool("folio.json"), context);

        assertEquals(2, f.getPamphlet().getPages().size());
        assertEquals("value1_en", f.getPamphlet().getPages().get(0).getText());
        assertEquals("value2_en", f.getPamphlet().getPages().get(1).getText());
    }

    @Test
    public void testWrappedZipAssetAccess() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        AssetFolioContext context = new AssetFolioContext("media", appContext.getAssets());

        ZipStreamFolioContext wrappedZipContext = new ZipStreamFolioContext(context, "all_in_one_folio.zip");
        Folio wrapped = parseFolio(wrappedZipContext.spool("media_folio.json"), wrappedZipContext);
        wrapped.validate();
    }
}