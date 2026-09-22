package dev.dayboard.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.dayboard.engine.model.ActiveBlock
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.PlayGlyph
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.Panel
import dev.dayboard.ui.theme.SystemGreen

/** A calm face: how long the break has run, what it interrupted, one way back. */
@Composable
fun BreakFace(active: ActiveBlock, onEndBreak: () -> Unit, modifier: Modifier = Modifier) {
    Panel(modifier, accent = SystemGreen) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ON BREAK", style = MaterialTheme.typography.labelLarge, color = SystemGreen)
            Text(
                text = formatCountdown(active.actuals.breakMs),
                style = MaterialTheme.typography.displayLarge,
                color = LabelPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "Waiting on · ${active.resolved.block.title}",
                style = MaterialTheme.typography.titleMedium,
                color = LabelSecondary
            )
            ActionButton(
                label = "BACK TO WORK",
                modifier = Modifier.fillMaxWidth(0.46f).padding(top = 34.dp),
                tone = Tone.Filled,
                accent = SystemGreen,
                height = 92.dp,
                corner = 26.dp,
                glyph = { PlayGlyph(it, 24.dp) },
                onClick = onEndBreak
            )
        }
    }
}
