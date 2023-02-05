package io.github.nircek.applicationsieve.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "rating_table", primaryKeys = ["app_id", "category_id"],
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
    val category_id: Int,
    val rating: Float,
)
