package io.github.nircek.applicationsieve.db

import android.content.res.Resources
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.nircek.applicationsieve.R

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
    }
}
