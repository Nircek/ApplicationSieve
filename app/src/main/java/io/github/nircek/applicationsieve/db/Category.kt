package io.github.nircek.applicationsieve.db

import android.content.Context
import android.content.res.Resources
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.nircek.applicationsieve.R
import io.github.nircek.applicationsieve.ui.PackageViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Entity(tableName = "category_table")
data class Category(
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    var category_id: Int = 0

    constructor(id: Int, name: String) : this(name) {
        category_id = id
    }

    override fun toString(): String {
        return name
    }

    companion object {
        fun all(res: Resources) = Category(0, res.getString(R.string.all_categories))
        fun new(res: Resources) = Category(-1, res.getString(R.string.new_category))

        @OptIn(ExperimentalCoroutinesApi::class)
        fun dialogNew(ctx: Context, vm: PackageViewModel) {
            AlertDialog.Builder(ctx).apply {
                setTitle(R.string.add_category)
                val et = EditText(ctx)
                setView(et)
                setPositiveButton(R.string.add_category) { _, _ -> vm.addCategory(et.text.toString()) }
            }.show()
        }
    }
}
