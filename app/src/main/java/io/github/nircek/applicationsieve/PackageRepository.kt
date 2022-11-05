package io.github.nircek.applicationsieve

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt


class PackageRepository(private val packageDao: PackageDao) {

    companion object {
        fun drawableToStream(icon: Drawable): ByteArray {
            val maxDim = max(icon.intrinsicWidth, icon.intrinsicHeight)
            val expDim = 128
            fun Int.adjust(): Int = (this.toFloat() / maxDim * expDim).roundToInt()
            val w = icon.intrinsicWidth.adjust()
            val h = icon.intrinsicWidth.adjust()
            val bitmap = if (icon is BitmapDrawable) {
                icon.bitmap
            } else {
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                icon.setBounds(0, 0, w, h)
                icon.draw(canvas)
                bmp
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream)
            return stream.toByteArray()
        }
    }

    val allPkgs: Flow<List<Package>> = packageDao.getAll()

    @WorkerThread
    suspend fun insert(pkg: Package) {
        packageDao.insert(pkg)

    }
}

