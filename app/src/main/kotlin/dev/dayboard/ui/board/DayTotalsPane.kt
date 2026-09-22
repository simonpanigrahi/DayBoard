package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.Confidence
import dev.dayboard.engine.model.DayTotals
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.SystemBlue
import dev.dayboard.ui.theme.SystemGray
import dev.dayboard.ui.theme.SystemGreen

/**
 * Three figures side by side rather than stacked: the whole point is the shape of the
 * day at a glance, and stacked rows pushed the honest one off the bottom of the panel.
 */
@Composable
fun DayTotalsPane(totals: DayTotals, modifier: Modifier = Modifier) {
    Panel(modifier, padding = 22.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TODAY",
                style = MaterialTheme.typography.labelLarge,
                color = LabelTertiary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatDuration(totals.elapsedMs)} of ${formatDuration(totals.plannedMs)}" +
                    if (totals.confidence == Confidence.PARTIAL) "   partial" else "",
                style = MaterialTheme.typography.labelSmall,
                color = if (totals.confidence == Confidence.PARTIAL) BoardAmber else LabelTertiary
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Stat("Focus", formatDuration(totals.focusedMs), SystemBlue, Modifier.weight(1f))
            Stat("Break", formatDuration(totals.breakMs), SystemGreen, Modifier.weight(1f))
            Stat("Untracked", formatDuration(totals.untrackedMs), SystemGray, Modifier.weight(1f))
        }

    }
}

@Composable
private fun Stat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Dot(color, 8.dp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = LabelSecondary)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = LabelPrimary,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}
