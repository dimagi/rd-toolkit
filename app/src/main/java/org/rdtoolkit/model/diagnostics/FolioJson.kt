package org.rdtoolkit.model.diagnostics

import org.json.JSONObject

fun parseFolio(body : String, folioContext: FolioContext) : Folio {
    var folio = JSONObject(body)
    val dict = getDictionaryRep(folio)
    val pamphlet = getPamphletRep(folio)
    return FolioRep(dict, pamphlet, folioContext)
}

fun getPamphletRep(folio: JSONObject): PamphletRep {
    if (!folio.has("pamphlet")) {
        throw Exception("Invalid Folio, no pamphlet present")
    }

    val pamphlet = folio.getJSONObject("pamphlet")
    val pages = pamphlet.getJSONArray("pages")

    val pagesRep = ArrayList<PageRep>()

    for (index in 0 until pages.length()) {
        val text = pages.getJSONObject(index).getString("textKey")
        val image = if (pages.getJSONObject(index).has("image")) pages.getJSONObject(index).getString("image") else null
        var page = PageRep(text, image)

        pagesRep.add(page)
    }
    return PamphletRep(pagesRep)
}

fun getDictionaryRep(folio : JSONObject) : HashMap<String, Map<String,String>>  {
    var retVal = LinkedHashMap<String, Map<String,String>>()
    if (!folio.has("dictionary")) {
        return retVal
    }
    val langs = folio.getJSONArray("dictionary")
    for (index in 0 until langs.length()) {
        val langMap = HashMap<String, String>()
        val langCode: String = langs.getJSONObject(index).getString("lang")
        val dict = langs.getJSONObject(index).getJSONObject("strings")
        for (key : String in dict.keys()) {
            langMap[key] = dict.getString(key)
        }
        retVal.put(langCode, langMap)
    }
    return retVal
}