package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.layout.ResolvedBlock
import dev.dayboard.ui.theme.BoardDim

@Composable
fun UpNextPane(next: ResolvedBlock?, later: List<ResolvedBlock>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("NEXT", style = MaterialTheme.typography.labelLarge, color = BoardDim)
        Text(
            text = next?.block?.title ?: "—",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (next != null) {
            Text(
                text = "${formatClock(next.start)} · ${next.minutes}m",
                style = MaterialTheme.typography.bodyLarge,
                color = BoardDim
            )
        }
        later.take(2).forEach { block ->
            Text(
                text = "THEN  ${block.block.title} · ${formatClock(block.start)} · ${block.minutes}m",
                style = MaterialTheme.typography.bodyMedium,
                color = BoardDim
            )
        }
    }
}
