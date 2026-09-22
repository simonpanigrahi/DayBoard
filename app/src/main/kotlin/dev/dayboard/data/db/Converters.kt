package dev.dayboard.data.db

import androidx.room.TypeConverter
import dev.dayboard.engine.model.Anchor
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.engine.model.ColorRole
import dev.dayboard.engine.model.EventType
import dev.dayboard.engine.model.OverflowPolicy
import dev.dayboard.engine.model.PlanSource
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter fun dateToText(value: LocalDate?): String? = value?.toString()
    @TypeConverter fun textToDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter fun timeToText(value: LocalTime?): String? = value?.toString()
    @TypeConverter fun textToTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    // Epoch millis rather than text: wallAt is indexed and range-queried per day.
    @TypeConverter fun instantToMillis(value: Instant?): Long? = value?.toEpochMilli()
    @TypeConverter fun millisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter fun kindToText(value: BlockKind): String = value.name
    @TypeConverter fun textToKind(value: String): BlockKind = BlockKind.valueOf(value)

    @TypeConverter fun anchorToText(value: Anchor): String = value.name
    @TypeConverter fun textToAnchor(value: String): Anchor = Anchor.valueOf(value)

    @TypeConverter fun overflowToText(value: OverflowPolicy): String = value.name
    @TypeConverter fun textToOverflow(value: String): OverflowPolicy = OverflowPolicy.valueOf(value)

    @TypeConverter fun colorToText(value: ColorRole): String = value.name
    @TypeConverter fun textToColor(value: String): ColorRole = ColorRole.valueOf(value)

    @TypeConverter fun sourceToText(value: PlanSource): String = value.name
    @TypeConverter fun textToSource(value: String): PlanSource = PlanSource.valueOf(value)

    @TypeConverter fun eventTypeToText(value: EventType): String = value.name
    @TypeConverter fun textToEventType(value: String): EventType = EventType.valueOf(value)
}
