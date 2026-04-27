package io.bubblymarble.fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import io.bubblymarble.fitness.core.designsystem.theme.BubblymarbleTheme
import io.bubblymarble.fitness.navigation.RootNavHost
import io.bubblymarble.fitness.navigation.RootViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BubblymarbleTheme {
                val vm: RootViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val state by vm.state.collectAsState()
                RootNavHost(state = state)
            }
        }
    }
}
