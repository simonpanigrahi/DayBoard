package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.ActiveBlock
import dev.dayboard.ui.theme.BoardBlack
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.RoleRest

/** A calm face: how long the break has run, what it interrupted, one way back. */
@Composable
fun BreakFace(active: ActiveBlock, onEndBreak: () -> Unit, modifier: Modifier = Modifier) {
    Panel(modifier, accent = RoleRest) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ON BREAK", style = MaterialTheme.typography.labelLarge, color = RoleRest)
            Text(
                text = formatCountdown(active.actuals.breakMs),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "waiting on — ${active.resolved.block.title}",
                style = MaterialTheme.typography.titleMedium,
                color = BoardDim
            )
            Button(
                onClick = onEndBreak,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.45f).height(96.dp).padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoleRest, contentColor = BoardBlack)
            ) {
                Text("BACK TO WORK", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
