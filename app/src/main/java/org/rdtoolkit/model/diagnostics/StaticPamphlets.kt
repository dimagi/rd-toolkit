package org.rdtoolkit.model.diagnostics

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

class StaticPamphlets(val context : Context) : PamphletSource {
    override fun getMatchingPamphlets(category: String, tags: List<String>): List<Pamphlet> {
        val response = ArrayList<Pamphlet>()
        val locale = Locale.getDefault()
        if (category.equals("reference") && tags.contains("carestart_mal_pf_pv")) {
            var folioContext = AssetFolioContext("bootstrapped/carestart_mal_pf_pv", context.assets)
            var mediaContext = ZipStreamFolioContext(folioContext, "media.zip")
            val folio = parseFolio(folioContext.spool("folio.json"), mediaContext)
            folio.setLocale(locale.language)
            response.add(folio.getPamphlet())
        }
        return response
    }
}
