package com.uet.teamduan.musicplayer.utils

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF




inline fun Bitmap.scaleAndCropCenter(w: Int, h: Int): Bitmap {
    val width = this.width
    val height = this.height
    val b = if (width >= height)
        Bitmap.createBitmap(
                this,
                width / 2 - height / 2,
                0,
                height,
                height
        )
    else
        Bitmap.createBitmap(
                this,
                0,
                height / 2 - width / 2,
                width,
                width
        )
    return Bitmap.createScaledBitmap(b, w, h, true)
}

fun drawableToBitmap (drawable: Drawable) :Bitmap {
    var bitmap:Bitmap?

    if (drawable is BitmapDrawable) {
        val bitmapDrawable:BitmapDrawable = drawable
        if(bitmapDrawable.bitmap != null) {
            return bitmapDrawable.bitmap
        }
    }

    if(drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight<= 0) {
        bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
    } else {
        bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
    }

    val canvas = Canvas(bitmap!!)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
fun getCroppedBitmap(src: Bitmap, path: Path): Bitmap {
    val output = Bitmap.createBitmap(
        src.width,
        src.height, Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(output)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.setColor(-0x1000000)

    canvas.drawPath(path, paint)

    // Keeps the source pixels that cover the destination pixels,
    // discards the remaining source and destination pixels.
    paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))

    canvas.drawBitmap(src, 0.0f, 0.0f, paint)

    return output
}
fun cropCircleBitmap(bitmap: Bitmap):Bitmap{
    val path = Path()
    path.addCircle(bitmap.width.toFloat()/2.0f,bitmap.height.toFloat()/2.0f,bitmap.width.toFloat()/2.0f,Path.Direction.CW)
    return getCroppedBitmap(bitmap, path)
}


fun getCircleBitmap(bitmap: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height,bitmap.config)
    val canvas = Canvas(output)

    val color = Color.RED
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawOval(rectF, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
}