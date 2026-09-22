package dev.dayboard.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    val dayStart: Flow<LocalTime> = dataStore.data.map { preferences ->
        preferences[DAY_START]?.let(LocalTime::parse) ?: DEFAULT_DAY_START
    }

    suspend fun setDayStart(time: LocalTime) {
        dataStore.edit { it[DAY_START] = time.toString() }
    }

    companion object {
        val DEFAULT_DAY_START: LocalTime = LocalTime.of(8, 0)
        private val DAY_START = stringPreferencesKey("day_start")
    }
}
