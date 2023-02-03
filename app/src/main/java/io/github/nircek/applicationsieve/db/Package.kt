package io.github.nircek.applicationsieve.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "package_table")
data class Package(
    @PrimaryKey @ColumnInfo(name = "package") val id: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var icon: ByteArray,
    val rating: Float
)
