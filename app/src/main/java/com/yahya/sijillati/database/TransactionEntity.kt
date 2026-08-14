package com.yahya.sijillati.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String,
    val paymentMethod: String,
    val currency: String,
    val date: Long,
    val timestamp: Long = System.currentTimeMillis()
)
