package org.rdtoolkit.util

import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import org.rdtoolkit.model.diagnostics.Page
import java.io.IOException
import java.io.InputStream

fun configureImageView(iv: ImageView, stream: InputStream?) {
    if (stream == null) {
        iv.setImageBitmap(null)
        return
    }
    stream.use {
        iv.setImageBitmap(BitmapFactory.decodeStream(it))
    }
}
