package com.flygames.flyinthesky.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.get
import androidx.core.graphics.red

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun getPlaneArea(bitmap: Bitmap): List<Offset> {
    val planeArea = mutableListOf<Offset>()
    for (width in 0 until bitmap.width)
        for(height in 0 until bitmap.height) {
            bitmap[width, height].let {
                if(it.red != 0)
                    planeArea.add(Offset(width.toFloat(), height.toFloat()))
            }
        }
    return planeArea
}