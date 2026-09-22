package dev.dayboard.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.colorFor

@Composable
fun UpNextPane(next: ResolvedBlock?, later: List<ResolvedBlock>, modifier: Modifier = Modifier) {
    Panel(modifier) {
        Text("UP NEXT", style = MaterialTheme.typography.labelLarge, color = BoardDim)

        if (next == null) {
            Text(
                text = "Nothing after this",
                style = MaterialTheme.typography.bodyLarge,
                color = BoardDim,
                modifier = Modifier.padding(top = 14.dp)
            )
            return@Panel
        }

        UpNextRow(next, emphasised = true)
        later.take(2).forEach { UpNextRow(it, emphasised = false) }
    }
}

@Composable
private fun UpNextRow(block: ResolvedBlock, emphasised: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            Modifier
                .size(if (emphasised) 18.dp else 12.dp)
                .clip(CircleShape)
                .background(colorFor(block.block.colorRole))
        ) {}
        Text(
            text = block.block.title,
            style = if (emphasised) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = if (emphasised) MaterialTheme.colorScheme.onBackground else BoardDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${formatClock(block.start)} · ${block.minutes}m",
            style = MaterialTheme.typography.bodyMedium,
            color = BoardDim
        )
    }
}
