package org.rdtoolkit.util

import android.graphics.BitmapFactory
import android.widget.ImageView
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

fun setImageBitmapFromFile(imageView: ImageView, currentPhotoPath: String?) {
    // Get the dimensions of the View
    var targetW = imageView.width
    if (targetW == 0) {
        targetW = imageView.layoutParams.width
    }
    if (targetW == 0) {
        return
    }

    // Get the dimensions of the bitmap
    val bmOptions = BitmapFactory.Options()
    bmOptions.inJustDecodeBounds = true
    BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    val photoW = bmOptions.outWidth

    // Determine how much to scale down the image
    val scaleFactor = Math.max(1, photoW / targetW)

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false
    bmOptions.inSampleSize = scaleFactor
    bmOptions.inPurgeable = true
    val bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions)
    imageView.setImageBitmap(bitmap)
}


