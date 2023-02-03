package io.github.nircek.applicationsieve.db

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
            val h = icon.intrinsicHeight.adjust()

            val bitmap = if (icon is BitmapDrawable) {
                Bitmap.createScaledBitmap(icon.bitmap, w, h, false)
            } else {
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                val save = icon.copyBounds()
                icon.setBounds(0, 0, w, h)
                icon.draw(canvas)
                icon.bounds = save
                bmp
            }
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream)
            bitmap.recycle()
            val ret = stream.toByteArray()
            stream.close()
            return ret
        }
    }

    val allPkgs: Flow<List<Package>> = packageDao.getAll()

    @WorkerThread
    suspend fun insert(pkg: Package) {
        packageDao.insert(pkg)
    }

    @WorkerThread
    suspend fun deleteAll() {
        packageDao.deleteAll()
    }
}

