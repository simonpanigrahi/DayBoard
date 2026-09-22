package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardDim
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val DATE = DateTimeFormatter.ofPattern("EEE d MMM")

@Composable
fun ClockHeader(clock: ZonedDateTime, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatClock(clock.toLocalTime()),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = clock.format(DATE),
                style = MaterialTheme.typography.headlineMedium,
                color = BoardDim
            )
            TextButton(onClick = onEdit, modifier = Modifier.heightIn(min = 72.dp)) {
                Text("EDIT", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
