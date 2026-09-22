package dev.dayboard.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PlanDayEntity::class,
        BlockEntity::class,
        ChecklistItemEntity::class,
        GoalEntity::class,
        SessionEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DayBoardDatabase : RoomDatabase() {

    abstract fun planDao(): PlanDao
    abstract fun eventDao(): EventDao
    abstract fun goalDao(): GoalDao

    companion object {
        fun build(context: Context): DayBoardDatabase =
            Room.databaseBuilder(context, DayBoardDatabase::class.java, "dayboard.db").build()
    }
}
