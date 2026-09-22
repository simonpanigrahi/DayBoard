package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.engine.model.ActiveBlock
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.colorFor

@Composable
fun CurrentBlockCard(
    active: ActiveBlock?,
    startable: ResolvedBlock?,
    checklist: List<ChecklistItem>,
    onToggleItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (active == null) {
        IdleCard(startable, modifier)
        return
    }

    val overrun = active.overrunMs > 0
    val accent = if (overrun) BoardAmber else colorFor(active.resolved.block.colorRole)
    val fraction = remember { mutableFloatStateOf(0f) }
    SideEffect { fraction.floatValue = active.progress }

    Panel(modifier, accent = accent) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("NOW", style = MaterialTheme.typography.labelLarge, color = accent)
                Text(
                    text = "  ${formatClock(active.resolved.start)}–${formatClock(active.resolved.end)}" +
                        "  ·  ${active.resolved.minutes}m",
                    style = MaterialTheme.typography.labelMedium,
                    color = BoardDim
                )
            }

            Spacer(Modifier.weight(0.8f))

            Text(
                text = active.resolved.block.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (overrun) "+${formatCountdown(active.overrunMs)}" else formatCountdown(active.remainingMs),
                    style = MaterialTheme.typography.displayMedium,
                    color = if (overrun) BoardAmber else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (overrun) "  over" else "  left",
                    style = MaterialTheme.typography.titleMedium,
                    color = BoardDim,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
                Text(
                    text = if (active.paused) "PAUSED" else "",
                    style = MaterialTheme.typography.labelLarge,
                    color = BoardAmber,
                    modifier = Modifier.weight(1f).padding(bottom = 14.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            ProgressBar(progress = fraction, color = accent)

            ChecklistRow(checklist, active.checkedItemIds, onToggleItem)
        }
    }
}

@Composable
private fun IdleCard(startable: ResolvedBlock?, modifier: Modifier = Modifier) {
    Panel(modifier, accent = startable?.let { colorFor(it.block.colorRole) } ?: BoardDim) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text(
                text = if (startable == null) "DAY DONE" else "READY",
                style = MaterialTheme.typography.labelLarge,
                color = BoardDim
            )
            Text(
                text = startable?.block?.title ?: "Nothing left to run",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (startable != null) {
                Text(
                    text = "${formatClock(startable.start)} · ${startable.minutes}m  ·  press START when you begin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BoardDim
                )
            }
        }
    }
}
