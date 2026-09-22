package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.ActiveBlock
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.colorFor

@Composable
fun CurrentBlockCard(
    active: ActiveBlock?,
    next: ResolvedBlock?,
    checklist: List<ChecklistItem>,
    onToggleItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (active == null) {
        IdleCard(next, modifier)
        return
    }

    val overrun = active.overrunMs > 0
    val fraction = remember { mutableFloatStateOf(0f) }
    SideEffect { fraction.floatValue = active.progress }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NOW", style = MaterialTheme.typography.labelLarge, color = BoardDim)
                Text(
                    text = active.resolved.block.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (overrun) "+${formatCountdown(active.overrunMs)}" else formatCountdown(active.remainingMs),
                    style = MaterialTheme.typography.displayMedium,
                    color = if (overrun) BoardAmber else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (overrun) "over" else "left",
                    style = MaterialTheme.typography.labelLarge,
                    color = BoardDim,
                    textAlign = TextAlign.End
                )
            }
        }

        ProgressBar(
            progress = fraction,
            color = if (overrun) BoardAmber else colorFor(active.resolved.block.colorRole)
        )

        ChecklistRow(checklist, active.checkedItemIds, onToggleItem)
    }
}

@Composable
private fun IdleCard(next: ResolvedBlock?, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("NOTHING RUNNING", style = MaterialTheme.typography.labelLarge, color = BoardDim)
        Text(
            text = next?.let { "${it.block.title} at ${formatClock(it.start)}" } ?: "Day done",
            style = MaterialTheme.typography.headlineLarge,
            color = BoardDim
        )
    }
}
