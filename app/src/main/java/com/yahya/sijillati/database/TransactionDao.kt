package com.yahya.sijillati.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, timestamp DESC")
    fun getAll(): LiveData<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getByType(type: String): LiveData<List<TransactionEntity>>

    @Insert
    suspend fun insert(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'INCOME' AND currency = :currency")
    fun getTotalIncome(currency: String): LiveData<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE' AND currency = :currency")
    fun getTotalExpense(currency: String): LiveData<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'LEND' AND currency = :currency")
    fun getTotalLent(currency: String): LiveData<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'BORROW' AND currency = :currency")
    fun getTotalBorrowed(currency: String): LiveData<Double>

    @Query("SELECT * FROM transactions WHERE title LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): LiveData<List<TransactionEntity>>
}
