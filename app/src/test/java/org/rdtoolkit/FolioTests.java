package org.rdtoolkit;

import org.junit.Test;
import org.rdtoolkit.model.diagnostics.Folio;
import org.rdtoolkit.model.diagnostics.JavaResourceFolioContext;
import org.rdtoolkit.model.diagnostics.ZipFileFolioContext;
import org.rdtoolkit.model.diagnostics.ZipStreamFolioContext;

import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.rdtoolkit.model.diagnostics.FolioJsonKt.parseFolio;

public class FolioTests {
    @Test
    public void testBasicParsing() {
        JavaResourceFolioContext context = new JavaResourceFolioContext("models");
        Folio f = parseFolio(context.spool("folio.json"), context);

        assertEquals(2, f.getPamphlet().getPages().size());
        assertEquals("value1_en", f.getPamphlet().getPages().get(0).getText());
        assertEquals("value2_en", f.getPamphlet().getPages().get(1).getText());
    }

    @Test
    public void testTranslations() {
        JavaResourceFolioContext context = new JavaResourceFolioContext("models");
        Folio f = parseFolio(context.spool("folio.json"), context);

        assertEquals(2, f.getPamphlet().getPages().size());
        assertEquals("value1_en", f.getPamphlet().getPages().get(0).getText());
        assertEquals("value2_en", f.getPamphlet().getPages().get(1).getText());

        f.setLocale("spa");

        assertEquals(2, f.getPamphlet().getPages().size());
        assertEquals("value1_spa", f.getPamphlet().getPages().get(0).getText());
        assertEquals("value2_spa", f.getPamphlet().getPages().get(1).getText());
    }

    @Test
    public void testBasicMedia() {
        JavaResourceFolioContext context = new JavaResourceFolioContext("models/media");
        Folio f = parseFolio(context.spool("media_folio.json"), context);
        f.validate();
    }

    @Test
    public void testZippedMedia() {
        JavaResourceFolioContext folioContext = new JavaResourceFolioContext("models/ziptests");
        ZipStreamFolioContext zipContext = new ZipStreamFolioContext(folioContext, "media_folio.zip");
        Folio f = parseFolio(folioContext.spool("media_folio.json"), zipContext);
        f.validate();
    }

    @Test
    public void testWrappedFolio() {
        JavaResourceFolioContext folioContext = new JavaResourceFolioContext("models/ziptests");

        ZipStreamFolioContext wrappedZipContext = new ZipStreamFolioContext(folioContext, "all_in_one_folio.zip");
        Folio wrapped = parseFolio(wrappedZipContext.spool("media_folio.json"), wrappedZipContext);
        wrapped.validate();
    }

}