package dev.dayboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.dayboard.ui.DayBoardRoot
import dev.dayboard.ui.theme.DayBoardTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as DayBoardApp).container
        setContent {
            DayBoardTheme {
                DayBoardRoot(container)
            }
        }
    }
}
