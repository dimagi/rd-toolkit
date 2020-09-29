package org.rdtoolkit;

import org.junit.Test;
import org.rdtoolkit.model.diagnostics.Folio;
import org.rdtoolkit.model.diagnostics.JavaResourceFolioContext;

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
}