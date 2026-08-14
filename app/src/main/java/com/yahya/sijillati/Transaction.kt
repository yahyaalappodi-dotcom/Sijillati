package com.yahya.sijillati

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val type: String,
    val paymentMethod: String,
    val currency: String = "د.ع",
    val date: String,
    val isArchived: Boolean = false
)
