package io.github.nircek.applicationsieve.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class Category(
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    var category_id: Int = 0
}
