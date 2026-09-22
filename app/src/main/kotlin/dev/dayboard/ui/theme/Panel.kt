package dev.dayboard.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * One panel shape for the whole board. The optional accent is the block's colour,
 * which is what ties a card to its segment on the ribbon.
 */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    padding: Int = 26,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(BoardSurface)
    ) {
        if (accent != null) {
            Row(Modifier.width(8.dp).fillMaxHeight().background(accent)) {}
        }
        Column(Modifier.fillMaxWidth().padding(padding.dp), content = content)
    }
}
