package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.ResolvedPlan
import dev.dayboard.engine.layout.Adjustment
import dev.dayboard.engine.model.BlockKind
import dev.dayboard.ui.board.formatClock
import dev.dayboard.ui.board.formatDuration
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.GhostButton
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardHairline
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.SystemBlue
import dev.dayboard.ui.theme.colorFor

/** The one screen every plan passes through, whatever produced it. */
@Composable
fun ReviewScreen(
    plan: ResolvedPlan,
    warnings: List<String> = emptyList(),
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
            .safeDrawingPadding()
            .padding(horizontal = 34.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Review", style = MaterialTheme.typography.displaySmall, color = LabelPrimary)
                Text(
                    text = "${plan.blocks.size} blocks  ·  ${formatDuration(plannedMs)} planned" +
                        if (hasBuffer) "" else "  ·  no buffer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (hasBuffer) LabelSecondary else BoardAmber,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            GhostButton("Back", accent = SystemBlue, onClick = onBack)
            ActionButton(
                label = "COMMIT",
                modifier = Modifier.padding(start = 10.dp),
                tone = Tone.Filled,
                accent = SystemBlue,
                height = 76.dp,
                corner = 14.dp,
                onClick = onCommit
            )
        }

        val notes = warnings + plan.conflicts.map { it.message }
        if (notes.isNotEmpty()) {
            Panel(corner = 18.dp, padding = 22.dp) {
                notes.forEach {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BoardAmber,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        Panel(Modifier.fillMaxSize(), corner = 18.dp, padding = 10.dp) {
            LazyColumn(Modifier.fillMaxSize()) {
                items(plan.blocks, key = { it.block.id.takeIf { id -> id != 0L } ?: it.startMinute }) { resolved ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            Modifier
                                .padding(end = 20.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(colorFor(resolved.block.colorRole))
                        ) {}
                        Text(
                            text = "${formatClock(resolved.start)} – ${formatClock(resolved.end)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = LabelSecondary
                        )
                        Text(
                            text = resolved.block.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = LabelPrimary,
                            modifier = Modifier.weight(1f).padding(horizontal = 24.dp)
                        )
                        Text(
                            text = "${resolved.minutes} min" +
                                if (resolved.adjustment != Adjustment.NONE) "  ·  ${resolved.adjustment.name.lowercase()}" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (resolved.adjustment != Adjustment.NONE) BoardAmber else LabelTertiary
                        )
                    }
                    Row(Modifier.fillMaxWidth().padding(start = 52.dp).height(Dp.Hairline).background(BoardHairline)) {}
                }
            }
        }
    }
}
