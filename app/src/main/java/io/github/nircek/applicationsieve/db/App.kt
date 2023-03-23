package io.github.nircek.applicationsieve.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_table",
    indices = [Index(value = ["package_name"], unique = true)]
) // FIXME: test what if we add the same app twice
data class App(
    val package_name: String,
    val app_name: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var icon: ByteArray,
    val source: String,
    val install_time: Long,
    val update_time: Long,
    val min_sdk: Int,
    val sdk: Int,
) {
    @PrimaryKey(autoGenerate = true)
    var app_id: Int = 0
}
