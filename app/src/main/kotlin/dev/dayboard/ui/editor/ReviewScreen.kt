package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.ResolvedPlan
import dev.dayboard.engine.layout.Adjustment
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.ui.board.formatClock
import dev.dayboard.ui.board.formatDuration
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.colorFor

/** The one screen every plan passes through, whatever produced it. */
@Composable
fun ReviewScreen(
    plan: ResolvedPlan,
    onCommit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plannedMs = plan.blocks.sumOf { it.minutes * 60_000L }
    val hasBuffer = plan.blocks.any { it.block.kind == BlockKind.BUFFER }

    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("REVIEW", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onBack, modifier = Modifier.height(72.dp)) {
                Text("BACK", style = MaterialTheme.typography.labelMedium)
            }
            Button(onClick = onCommit, modifier = Modifier.height(72.dp).padding(start = 12.dp)) {
                Text("COMMIT", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            text = "${plan.blocks.size} blocks · ${formatDuration(plannedMs)} planned" +
                if (hasBuffer) "" else " · no buffer in the day",
            style = MaterialTheme.typography.bodyLarge,
            color = if (hasBuffer) BoardDim else BoardAmber
        )

        plan.conflicts.forEach { conflict ->
            Text("⚠ ${conflict.message}", style = MaterialTheme.typography.bodyMedium, color = BoardAmber)
        }

        LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(plan.blocks, key = { it.block.id.takeIf { id -> id != 0L } ?: it.startMinute }) { resolved ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatClock(resolved.start)}–${formatClock(resolved.end)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorFor(resolved.block.colorRole)
                    )
                    Text(
                        text = resolved.block.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(horizontal = 20.dp)
                    )
                    Text(
                        text = "${resolved.minutes}m" +
                            if (resolved.adjustment != Adjustment.NONE) " · ${resolved.adjustment.name.lowercase()}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (resolved.adjustment != Adjustment.NONE) BoardAmber else BoardDim
                    )
                }
            }
        }
    }
}
