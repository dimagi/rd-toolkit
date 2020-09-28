package org.rdtoolkit.model.diagnostics

interface Folio {
    fun getDictionary() : Map<String, String>
    fun getPamphlets() : Pamphlet
}

interface Pamphlet {
    fun getTitle() : String?
    fun getPages() : List<Page>
}

interface Page {
    fun getText() : String
    fun getImage() : String
}