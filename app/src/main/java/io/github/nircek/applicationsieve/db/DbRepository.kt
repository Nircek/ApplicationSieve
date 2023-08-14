package io.github.nircek.applicationsieve.db

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt


class DbRepository(private val dao: DbDao) {

    data class RatedApp(
        val app_id: Int,
        val package_name: String,
        val app_name: String,
        val icon: ByteArray,
        val rating: Float
    )

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

    val allApps: Flow<List<App>> = dao.getAllApps()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()

    @WorkerThread
    suspend fun dropApps() = dao.deleteAllApps()

    @WorkerThread
    suspend fun insertCategory(category: Category) = dao.insertCategory(category)

    @WorkerThread
    suspend fun dropCategories() = dao.deleteAllCategories()

    @WorkerThread
    suspend fun rate(
        app: App,
        version: String,
        versionCode: Long,
        description: String,
        category: Int,
        rating: Float
    ) {
        val appId = dao.insertApp(app).toInt()
        dao.insertRate(
            Rating(
                app_id = appId, rating_time = System.currentTimeMillis(),
                version = version,
                versionCode = versionCode,
                description = description,
                category_id = category,
                rating = rating
            )
        )
    }

    fun getRatedApps(category: Int = 0): Flow<List<RatedApp>> {
        return (
                if (category != 0) dao.getRatesInCategory(category)
                else dao.getRatesInAllCategories()
                ).map { m ->
                m.map { (k, v) ->
                    RatedApp(v.app_id, v.package_name, v.app_name, v.icon, k.rating)
                }
            }
    }

    @WorkerThread
    suspend fun deleteCategory(c: Category) {
        dao.deleteCategory(c)
    }
}

