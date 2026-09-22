package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.common.GhostButton
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.SystemBlue
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val WEEKDAY = DateTimeFormatter.ofPattern("EEEE")
private val DATE = DateTimeFormatter.ofPattern("d MMMM")

@Composable
fun ClockHeader(
    clock: ZonedDateTime,
    onEdit: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = formatClock(clock.toLocalTime()),
            style = MaterialTheme.typography.displayLarge,
            color = LabelPrimary
        )
        Column(Modifier.weight(1f).padding(start = 30.dp, top = 12.dp)) {
            Text(
                text = clock.format(WEEKDAY),
                style = MaterialTheme.typography.titleLarge,
                color = LabelPrimary
            )
            Text(
                text = clock.format(DATE),
                style = MaterialTheme.typography.titleMedium,
                color = LabelSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            GhostButton("Fill the day", accent = SystemBlue, onClick = onImport)
            GhostButton("Edit", accent = SystemBlue, onClick = onEdit)
        }
    }
}
