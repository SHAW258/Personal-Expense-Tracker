package com.example.financetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financetracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE monthYear = :monthYear ORDER BY dateMillis DESC, id DESC")
    fun getTransactionsByMonth(monthYear: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): Transaction?

    @Query("""
        SELECT * FROM transactions 
        WHERE (:monthYear IS NULL OR monthYear = :monthYear)
          AND (:type IS NULL OR type = :type)
          AND (:category IS NULL OR category = :category)
          AND (title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
        ORDER BY 
          CASE WHEN :sortBy = 'DATE_ASC' THEN dateMillis END ASC,
          CASE WHEN :sortBy = 'DATE_DESC' THEN dateMillis END DESC,
          CASE WHEN :sortBy = 'AMOUNT_ASC' THEN amount END ASC,
          CASE WHEN :sortBy = 'AMOUNT_DESC' THEN amount END DESC,
          id DESC
    """)
    fun searchTransactions(
        query: String,
        monthYear: String?,
        type: String?,
        category: String?,
        sortBy: String = "DATE_DESC"
    ): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT monthYear FROM transactions ORDER BY monthYear DESC")
    fun getAllRecordedMonths(): Flow<List<String>>
}
