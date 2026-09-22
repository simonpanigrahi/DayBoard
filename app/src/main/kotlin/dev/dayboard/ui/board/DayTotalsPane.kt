package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.DayTotals
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.RoleDeep
import dev.dayboard.ui.theme.RoleNeutral
import dev.dayboard.ui.theme.RoleRest

@Composable
fun DayTotalsPane(totals: DayTotals, modifier: Modifier = Modifier) {
    val fraction = remember { mutableFloatStateOf(0f) }
    SideEffect {
        fraction.floatValue =
            if (totals.plannedMs == 0L) 0f else (totals.elapsedMs.toFloat() / totals.plannedMs).coerceIn(0f, 1f)
    }

    Panel(modifier) {
        Text("TODAY", style = MaterialTheme.typography.labelLarge, color = BoardDim)
        TotalRow("Focus", formatDuration(totals.focusedMs), RoleDeep)
        TotalRow("Break", formatDuration(totals.breakMs), RoleRest)
        TotalRow("Untracked", formatDuration(totals.untrackedMs), RoleNeutral)
        ProgressBar(
            progress = fraction,
            color = RoleDeep,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "${formatDuration(totals.elapsedMs)} of ${formatDuration(totals.plannedMs)} planned" +
                if (totals.confidence == Confidence.PARTIAL) "  ·  partial" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = if (totals.confidence == Confidence.PARTIAL) BoardAmber else BoardDim,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun TotalRow(label: String, value: String, color: Color) {
    Row(
        Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = color)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
