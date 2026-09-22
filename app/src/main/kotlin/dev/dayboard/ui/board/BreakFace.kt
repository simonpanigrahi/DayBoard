package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.ActiveBlock
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.RoleRest

/** A calm face for the break: how long it has run, what it interrupted, one way back. */
@Composable
fun BreakFace(active: ActiveBlock, onEndBreak: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("ON BREAK", style = MaterialTheme.typography.labelLarge, color = BoardDim)
        Text(
            text = formatCountdown(active.actuals.breakMs),
            style = MaterialTheme.typography.displayLarge,
            color = RoleRest
        )
        Text(
            text = "paused: ${active.resolved.block.title}",
            style = MaterialTheme.typography.titleMedium,
            color = BoardDim
        )
        Button(
            onClick = onEndBreak,
            modifier = Modifier.fillMaxWidth(0.5f).height(96.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RoleRest,
                contentColor = MaterialTheme.colorScheme.background
            )
        ) {
            Text("RESUME", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
