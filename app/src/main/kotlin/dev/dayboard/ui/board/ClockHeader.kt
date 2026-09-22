package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.theme.BoardDim
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val DATE = DateTimeFormatter.ofPattern("EEEE d MMMM")

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
            color = MaterialTheme.colorScheme.onBackground
        )
        Column(Modifier.weight(1f).padding(start = 28.dp, bottom = 10.dp)) {
            Text(
                text = clock.format(DATE),
                style = MaterialTheme.typography.titleLarge,
                color = BoardDim
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            HeaderButton("FILL THE DAY", onImport)
            HeaderButton("EDIT", onEdit)
        }
    }
}

@Composable
private fun HeaderButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.height(72.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
