package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.DayTotals
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.RoleDeep

@Composable
fun DayTotalsPane(totals: DayTotals, modifier: Modifier = Modifier) {
    val fraction = remember { mutableFloatStateOf(0f) }
    SideEffect {
        fraction.floatValue =
            if (totals.plannedMs == 0L) 0f else (totals.elapsedMs.toFloat() / totals.plannedMs).coerceIn(0f, 1f)
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("TODAY", style = MaterialTheme.typography.labelLarge, color = BoardDim)
        TotalRow("Focus", formatDuration(totals.focusedMs))
        TotalRow("Break", formatDuration(totals.breakMs))
        TotalRow("Untracked", formatDuration(totals.untrackedMs))
        ProgressBar(progress = fraction, color = RoleDeep)
        Text(
            text = "${formatDuration(totals.elapsedMs)} / ${formatDuration(totals.plannedMs)}" +
                if (totals.confidence == Confidence.PARTIAL) "  · partial" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = if (totals.confidence == Confidence.PARTIAL) BoardAmber else BoardDim
        )
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = BoardDim)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}
