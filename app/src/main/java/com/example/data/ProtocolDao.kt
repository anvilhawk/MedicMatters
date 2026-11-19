package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtocolDao {

    @Query("SELECT * FROM drip_protocols ORDER BY sortOrder ASC, id ASC")
    fun observeDripProtocols(): Flow<List<DripProtocolEntity>>

    @Query("SELECT * FROM syringe_protocols ORDER BY sortOrder ASC, id ASC")
    fun observeSyringeProtocols(): Flow<List<SyringeProtocolEntity>>

    @Query("SELECT COUNT(*) FROM drip_protocols")
    suspend fun countDripProtocols(): Int

    @Query("SELECT COUNT(*) FROM syringe_protocols")
    suspend fun countSyringeProtocols(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM drip_protocols")
    suspend fun maxDripSortOrder(): Int

    @Query("SELECT COALESCE(MIN(sortOrder), 0) FROM drip_protocols")
    suspend fun minDripSortOrder(): Int

    @Query("SELECT COUNT(*) FROM drip_protocols WHERE LOWER(name) = LOWER(:name)")
    suspend fun countDripByName(name: String): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM syringe_protocols")
    suspend fun maxSyringeSortOrder(): Int

    @Query("SELECT COALESCE(MIN(sortOrder), 0) FROM syringe_protocols")
    suspend fun minSyringeSortOrder(): Int

    @Query("SELECT COUNT(*) FROM syringe_protocols WHERE LOWER(name) = LOWER(:name)")
    suspend fun countSyringeByName(name: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDripProtocol(entity: DripProtocolEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDripProtocols(entities: List<DripProtocolEntity>)

    @Update
    suspend fun updateDripProtocol(entity: DripProtocolEntity)

    @Query("DELETE FROM drip_protocols WHERE id = :id")
    suspend fun deleteDripProtocolById(id: Long)

    @Query("DELETE FROM drip_protocols")
    suspend fun deleteAllDripProtocols()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyringeProtocol(entity: SyringeProtocolEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyringeProtocols(entities: List<SyringeProtocolEntity>)

    @Update
    suspend fun updateSyringeProtocol(entity: SyringeProtocolEntity)

    @Query("DELETE FROM syringe_protocols WHERE id = :id")
    suspend fun deleteSyringeProtocolById(id: Long)

    @Query("DELETE FROM syringe_protocols")
    suspend fun deleteAllSyringeProtocols()

    @Transaction
    suspend fun replaceAllWithDefaults(
        drips: List<DripProtocolEntity>,
        syringes: List<SyringeProtocolEntity>
    ) {
        deleteAllDripProtocols()
        deleteAllSyringeProtocols()
        insertDripProtocols(drips)
        insertSyringeProtocols(syringes)
    }
}
