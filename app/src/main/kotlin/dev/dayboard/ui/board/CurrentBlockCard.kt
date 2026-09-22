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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.engine.model.ActiveBlock
import dev.dayboard.engine.model.ChecklistItem
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
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
    // A block the schedule has reached but nobody started is not running, and the card
    // says so rather than counting down time that is not being worked.
    if (active?.startedAt == null) {
        ReadyCard(active?.resolved ?: startable, checklist, active?.checkedItemIds.orEmpty(), onToggleItem, modifier)
        return
    }

    val overrun = active.overrunMs > 0
    val accent = if (overrun) BoardAmber else colorFor(active.resolved.block.colorRole)
    val fraction = remember { mutableFloatStateOf(0f) }
    SideEffect { fraction.floatValue = active.progress }

    Panel(modifier, accent = accent) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EyebrowRow(
                label = if (active.paused) "PAUSED" else "NOW",
                labelColor = if (active.paused) BoardAmber else accent,
                detail = window(active.resolved) + doneCount(checklist, active.checkedItemIds)
            )

            Spacer(Modifier.weight(0.7f))

            Text(
                text = active.resolved.block.title,
                style = MaterialTheme.typography.headlineLarge,
                color = LabelPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (overrun) "+${formatCountdown(active.overrunMs)}" else formatCountdown(active.remainingMs),
                    style = MaterialTheme.typography.displayMedium,
                    color = if (overrun) BoardAmber else LabelPrimary
                )
                Text(
                    text = if (overrun) "over" else "left",
                    style = MaterialTheme.typography.titleMedium,
                    color = LabelSecondary,
                    modifier = Modifier.padding(start = 14.dp, bottom = 12.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            ProgressBar(progress = fraction, color = accent)

            ChecklistRow(checklist, active.checkedItemIds, accent, onToggleItem, Modifier.padding(top = 14.dp))
        }
    }
}

@Composable
private fun ReadyCard(
    block: ResolvedBlock?,
    checklist: List<ChecklistItem>,
    checkedIds: Set<Long>,
    onToggleItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = block?.let { colorFor(it.block.colorRole) } ?: LabelTertiary
    Panel(modifier, accent = block?.let { accent }) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EyebrowRow(
                label = if (block == null) "DAY DONE" else "READY",
                labelColor = accent,
                detail = block?.let { window(it) + doneCount(checklist, checkedIds) }.orEmpty()
            )

            Spacer(Modifier.weight(0.7f))

            Text(
                text = block?.block?.title ?: "Nothing left to run",
                style = MaterialTheme.typography.headlineLarge,
                color = if (block == null) LabelSecondary else LabelPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (block != null) {
                Text(
                    text = "Press start when you begin",
                    style = MaterialTheme.typography.titleMedium,
                    color = LabelSecondary
                )
            }

            Spacer(Modifier.weight(1f))

            ChecklistRow(checklist, checkedIds, accent, onToggleItem)
        }
    }
}

private fun window(block: ResolvedBlock) =
    "${formatClock(block.start)} – ${formatClock(block.end)}   ${block.minutes} min"

private fun doneCount(checklist: List<ChecklistItem>, checkedIds: Set<Long>) =
    if (checklist.isEmpty()) "" else "   ${checklist.count { it.id in checkedIds }} of ${checklist.size} done"

@Composable
private fun EyebrowRow(label: String, labelColor: Color, detail: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = labelColor)
        Text(
            text = detail,
            style = MaterialTheme.typography.labelMedium,
            color = LabelTertiary,
            modifier = Modifier.padding(start = 18.dp)
        )
    }
}
