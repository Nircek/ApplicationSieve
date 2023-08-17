package io.github.nircek.applicationsieve.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "rating_table", primaryKeys = ["app_id", "category_id"],
    indices = [Index(value = ["category_id"])], // SRC: https://stackoverflow.com/a/58597401/6732111
    foreignKeys = [
        ForeignKey(
            entity = App::class,
            parentColumns = ["app_id"],
            childColumns = ["app_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Rating(
    val app_id: Int,
    val rating_time: Long,
    val version: String,
    val versionCode: Long,
    @ColumnInfo(defaultValue = "᛭")
    val payload: String,
    val description: String,
    val category_id: Int,
    val rating: Float,
)
