package dev.dayboard.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.ui.theme.BoardHairline
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.colorFor

@Composable
fun UpNextPane(next: ResolvedBlock?, later: List<ResolvedBlock>, modifier: Modifier = Modifier) {
    Panel(modifier, padding = 24.dp) {
        Text("UP NEXT", style = MaterialTheme.typography.labelLarge, color = LabelTertiary)

        if (next == null) {
            Text(
                text = "Nothing after this",
                style = MaterialTheme.typography.bodyLarge,
                color = LabelSecondary,
                modifier = Modifier.padding(top = 18.dp)
            )
            return@Panel
        }

        UpNextRow(next, emphasised = true)
        later.take(1).forEach { block ->
            Hairline()
            UpNextRow(block, emphasised = false)
        }
    }
}

@Composable
private fun UpNextRow(block: ResolvedBlock, emphasised: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = if (emphasised) 13.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Dot(colorFor(block.block.colorRole), if (emphasised) 14.dp else 10.dp)
        Text(
            text = block.block.title,
            style = if (emphasised) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = if (emphasised) LabelPrimary else LabelSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${formatClock(block.start)}   ${block.minutes}m",
            style = MaterialTheme.typography.labelSmall,
            color = LabelTertiary
        )
    }
}

@Composable
fun Dot(color: androidx.compose.ui.graphics.Color, size: Dp) {
    Row(Modifier.size(size).clip(CircleShape).background(color)) {}
}

@Composable
private fun Hairline() {
    Row(Modifier.fillMaxWidth().height(Dp.Hairline).background(BoardHairline)) {}
}
