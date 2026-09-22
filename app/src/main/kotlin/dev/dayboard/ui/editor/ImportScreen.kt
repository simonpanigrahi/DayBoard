package dev.dayboard.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.dayboard.ui.common.ActionButton
import dev.dayboard.ui.common.GhostButton
import dev.dayboard.ui.common.Tone
import dev.dayboard.ui.common.boardFieldColors
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.LabelPrimary
import dev.dayboard.ui.theme.LabelSecondary
import dev.dayboard.ui.theme.LabelTertiary
import dev.dayboard.ui.theme.SystemBlue

/**
 * The whole day in one box. Type the line grammar, or copy the prompt into any AI and
 * paste its JSON straight back here: the same field takes both.
 */
@Composable
fun ImportScreen(
    errors: List<String>,
    onParse: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

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
                Text("Fill the day", style = MaterialTheme.typography.displaySmall, color = LabelPrimary)
                Text(
                    text = "Type it, or paste what an AI gave you. The box takes both.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = LabelSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            GhostButton("Cancel", accent = SystemBlue, onClick = onBack)
            ActionButton(
                label = if (copied) "PROMPT COPIED" else "COPY AI PROMPT",
                modifier = Modifier.padding(start = 10.dp),
                accent = SystemBlue,
                height = 76.dp,
                corner = 14.dp,
                onClick = {
                    clipboard.setText(AnnotatedString(PLAN_PROMPT))
                    copied = true
                }
            )
            ActionButton(
                label = "BUILD IT",
                modifier = Modifier.padding(start = 10.dp),
                tone = Tone.Filled,
                accent = SystemBlue,
                enabled = text.isNotBlank(),
                height = 76.dp,
                corner = 14.dp,
                onClick = { onParse(text) }
            )
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = DSL_HINT,
                    color = LabelTertiary,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
            shape = RoundedCornerShape(14.dp),
            colors = boardFieldColors(),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        if (errors.isEmpty()) {
            Text(
                text = "HH:MM–HH:MM or HH:MM 45m pins to the clock   ·   45m flows after the block above" +
                    "   ·   =  buffer   ·   ~  break   ·   !  appointment   ·   #tag colours it" +
                    "   ·   two spaces and  -  adds a checklist item",
                style = MaterialTheme.typography.labelSmall,
                color = LabelTertiary
            )
        } else {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                errors.forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = BoardAmber)
                }
            }
        }
    }
}
