package dev.dayboard

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dev.dayboard.clock.BootId
import dev.dayboard.data.db.DayBoardDatabase
import dev.dayboard.data.repo.EventRepository
import dev.dayboard.data.repo.PlanRepository
import dev.dayboard.data.repo.SettingsRepository

/** Manual DI: about a dozen objects, so a graph framework would cost more than it saves. */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: DayBoardDatabase by lazy { DayBoardDatabase.build(appContext) }

    private val preferences: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create { appContext.preferencesDataStoreFile("dayboard") }
    }

    val settings: SettingsRepository by lazy { SettingsRepository(preferences) }

    private val bootId: BootId by lazy { BootId(preferences) }

    val plans: PlanRepository by lazy { PlanRepository(database.planDao()) }

    val events: EventRepository by lazy { EventRepository(database.eventDao(), bootId::current) }
}
