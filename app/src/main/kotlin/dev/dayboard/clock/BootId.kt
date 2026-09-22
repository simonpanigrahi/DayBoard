package dev.dayboard.clock

import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.util.UUID

/**
 * Groups the elapsedRealtime readings that belong to one boot.
 *
 * elapsedRealtime() restarts at zero on reboot, so a duration subtracted across that
 * boundary is garbage. A reading lower than the last one we stored can only mean the
 * device restarted, so the id is rotated and the fold refuses to subtract across it.
 */
class BootId(
    private val dataStore: DataStore<Preferences>,
    private val elapsedRealtime: () -> Long = SystemClock::elapsedRealtime
) {

    suspend fun current(): String {
        val elapsed = elapsedRealtime()
        val updated = dataStore.edit { preferences ->
            val lastSeen = preferences[LAST_ELAPSED]
            if (preferences[BOOT_ID] == null || lastSeen == null || elapsed < lastSeen) {
                preferences[BOOT_ID] = UUID.randomUUID().toString()
            }
            preferences[LAST_ELAPSED] = elapsed
        }
        return checkNotNull(updated[BOOT_ID])
    }

    private companion object {
        val BOOT_ID = stringPreferencesKey("boot_id")
        val LAST_ELAPSED = longPreferencesKey("boot_last_elapsed")
    }
}
