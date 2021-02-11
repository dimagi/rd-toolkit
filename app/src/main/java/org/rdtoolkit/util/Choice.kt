package org.rdtoolkit.util

import android.content.Context

class Choice(val context: Context, val id: String, val stringResourceId: Int) {
    override fun toString() : String {
        return context.getString(stringResourceId)
    }
}

