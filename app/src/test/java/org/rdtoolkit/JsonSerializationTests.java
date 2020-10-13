package org.rdtoolkit;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.rdtoolkit.interop.SessionToJson;
import org.rdtoolkit.model.diagnostics.Folio;
import org.rdtoolkit.model.diagnostics.JavaResourceFolioContext;
import org.rdtoolkit.model.diagnostics.ZipStreamFolioContext;

import static org.junit.Assert.assertEquals;
import static org.rdtoolkit.TestObjects.SessionCompleted;
import static org.rdtoolkit.TestObjects.SessionProvisioned;
import static org.rdtoolkit.model.diagnostics.FolioJsonKt.parseFolio;

public class JsonSerializationTests {
    @Test
    public void testBasicParsing() throws JSONException {
        JSONObject roundTripped = new JSONObject(new SessionToJson().map(SessionCompleted).toString());
        System.out.println(roundTripped.toString(4));
        assertEquals(roundTripped.getString("id"), "unique_id");
    }
}