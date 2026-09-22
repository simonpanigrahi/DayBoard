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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import dev.dayboard.ui.theme.BoardAmber
import dev.dayboard.ui.theme.BoardDim
import dev.dayboard.ui.theme.RoleRest

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
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("FILL THE DAY", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onBack, modifier = Modifier.height(72.dp)) {
                Text("CANCEL", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
                onClick = {
                    clipboard.setText(AnnotatedString(PLAN_PROMPT))
                    copied = true
                },
                modifier = Modifier.height(72.dp).padding(start = 12.dp)
            ) {
                Text(if (copied) "PROMPT COPIED" else "COPY AI PROMPT", style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = { onParse(text) },
                enabled = text.isNotBlank(),
                modifier = Modifier.height(72.dp).padding(start = 12.dp)
            ) {
                Text("BUILD IT", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text(
            text = "Type lines, or paste the JSON an AI gave you. Both work here.",
            style = MaterialTheme.typography.bodyMedium,
            color = BoardDim
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(DSL_HINT, color = BoardDim.copy(alpha = 0.6f), fontFamily = FontFamily.Monospace) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.fillMaxWidth().weight(1f)
        )

        if (errors.isEmpty()) {
            Text(
                text = "HH:MM-HH:MM or HH:MM 45m pins to the clock  ·  45m flows after the block above" +
                    "  ·  '=' buffer  ·  '~' break  ·  '!' appointment  ·  '#tag' colours it" +
                    "  ·  two spaces and '-' adds a checklist item",
                style = MaterialTheme.typography.bodyMedium,
                color = RoleRest.copy(alpha = 0.75f)
            )
        } else {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                errors.forEach { Text("⚠ $it", style = MaterialTheme.typography.bodyMedium, color = BoardAmber) }
            }
        }
    }
}
