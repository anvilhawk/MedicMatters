package com.example.data

import android.content.Context
import com.example.domain.DripPreset
import com.example.domain.MedicMath
import com.example.domain.SyringePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProtocolRepository(
    private val context: Context,
    private val dao: ProtocolDao
) {
    private val seedMutex = Mutex()

    val dripProtocols: Flow<List<DripPreset>> =
        dao.observeDripProtocols().map { list -> list.map { it.toPreset() } }

    val syringeProtocols: Flow<List<SyringePreset>> =
        dao.observeSyringeProtocols().map { list -> list.map { it.toPreset() } }

    suspend fun ensureSeeded() {
        seedMutex.withLock {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val alreadySeeded = prefs.getBoolean(KEY_SEEDED, false)
            if (!alreadySeeded) {
                val dripCount = dao.countDripProtocols()
                val syringeCount = dao.countSyringeProtocols()
                if (dripCount == 0 && syringeCount == 0) {
                    dao.replaceAllWithDefaults(
                        drips = defaultDripEntities(),
                        syringes = defaultSyringeEntities()
                    )
                }
                prefs.edit()
                    .putBoolean(KEY_SEEDED, true)
                    .putBoolean(KEY_GENERIC_ADDED_V2, true)
                    .apply()
            } else if (!prefs.getBoolean(KEY_GENERIC_ADDED_V2, false)) {
                if (dao.countDripByName("Generic") == 0) {
                    val minOrder = dao.minDripSortOrder()
                    MedicMath.findDripPreset("Generic")?.let { genericDrip ->
                        dao.insertDripProtocol(
                            genericDrip.copy(id = 0L, sortOrder = minOrder - 1)
                                .toEntity(sortOrderOverride = minOrder - 1)
                        )
                    }
                }
                if (dao.countSyringeByName("Generic") == 0) {
                    val minOrder = dao.minSyringeSortOrder()
                    MedicMath.findSyringePreset("Generic")?.let { genericSyringe ->
                        dao.insertSyringeProtocol(
                            genericSyringe.copy(id = 0L, sortOrder = minOrder - 1)
                                .toEntity(sortOrderOverride = minOrder - 1)
                        )
                    }
                }
                prefs.edit().putBoolean(KEY_GENERIC_ADDED_V2, true).apply()
            }
        }
    }

    suspend fun saveDripProtocol(preset: DripPreset): Long {
        return if (preset.id > 0L) {
            dao.updateDripProtocol(preset.toEntity())
            preset.id
        } else {
            val nextOrder = dao.maxDripSortOrder() + 1
            dao.insertDripProtocol(preset.toEntity(sortOrderOverride = nextOrder).copy(id = 0L))
        }
    }

    suspend fun deleteDripProtocol(id: Long) {
        dao.deleteDripProtocolById(id)
    }

    suspend fun saveSyringeProtocol(preset: SyringePreset): Long {
        return if (preset.id > 0L) {
            dao.updateSyringeProtocol(preset.toEntity())
            preset.id
        } else {
            val nextOrder = dao.maxSyringeSortOrder() + 1
            dao.insertSyringeProtocol(preset.toEntity(sortOrderOverride = nextOrder).copy(id = 0L))
        }
    }

    suspend fun deleteSyringeProtocol(id: Long) {
        dao.deleteSyringeProtocolById(id)
    }

    suspend fun resetToDefaults() {
        seedMutex.withLock {
            dao.replaceAllWithDefaults(
                drips = defaultDripEntities(),
                syringes = defaultSyringeEntities()
            )
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SEEDED, true)
                .putBoolean(KEY_GENERIC_ADDED_V2, true)
                .apply()
        }
    }

    private fun defaultDripEntities(): List<DripProtocolEntity> =
        MedicMath.DRIP_PRESETS.mapIndexed { index, preset ->
            preset.copy(id = 0L, sortOrder = index).toEntity(sortOrderOverride = index)
        }

    private fun defaultSyringeEntities(): List<SyringeProtocolEntity> =
        MedicMath.SYRINGE_PRESETS.mapIndexed { index, preset ->
            preset.copy(id = 0L, sortOrder = index).toEntity(sortOrderOverride = index)
        }

    companion object {
        private const val PREFS_NAME = "medic_calc_protocol_prefs"
        private const val KEY_SEEDED = "protocols_seeded_v1"
        private const val KEY_GENERIC_ADDED_V2 = "generic_preset_added_v2"
    }
}
