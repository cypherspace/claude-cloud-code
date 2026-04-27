package io.bubblymarble.fitness.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.bubblymarble.fitness.core.designsystem.theme.LocalFeatureAccents
import io.bubblymarble.fitness.feature.meals.MealEditorScreen
import io.bubblymarble.fitness.feature.meals.MealsScreen
import io.bubblymarble.fitness.feature.meals.scanner.BarcodeScannerScreen
import io.bubblymarble.fitness.feature.measurements.MeasurementsScreen
import io.bubblymarble.fitness.feature.onboarding.OnboardingScreen
import io.bubblymarble.fitness.feature.plans.PlansScreen
import io.bubblymarble.fitness.feature.settings.SettingsScreen
import io.bubblymarble.fitness.feature.stats.StatsScreen
import io.bubblymarble.fitness.feature.workouts.WorkoutsScreen
import io.bubblymarble.fitness.feature.workouts.runner.WorkoutRunnerScreen

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val PLANS = "plans"
    const val WORKOUTS = "workouts"
    const val MEALS = "meals"
    const val MEASUREMENTS = "measurements"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val RUNNER = "runner/{sessionId}"
    const val MEAL_EDITOR = "meal_editor/{mealId}"
    const val MEAL_SCANNER = "meal_scanner"
    const val SCANNED_BARCODE_KEY = "scanned_barcode"
    fun runner(sessionId: Long) = "runner/$sessionId"
    fun mealEditor(mealId: Long?) = "meal_editor/${mealId ?: 0L}"
}

@Composable
fun RootNavHost(state: RootState) {
    val nav = rememberNavController()

    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val start = if (state.isOnboarded) Routes.HOME else Routes.ONBOARDING

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onDone = {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScaffold(
                onStartSession = { id -> nav.navigate(Routes.runner(id)) },
                onAddMeal = { nav.navigate(Routes.mealEditor(null)) },
                onEditMeal = { id -> nav.navigate(Routes.mealEditor(id)) },
            )
        }
        composable(Routes.RUNNER) { entry ->
            val id = entry.arguments?.getString("sessionId")?.toLongOrNull() ?: 0L
            WorkoutRunnerScreen(sessionId = id, onFinish = { nav.popBackStack() })
        }
        composable(Routes.MEAL_EDITOR) { entry ->
            val barcode by entry.savedStateHandle
                .getStateFlow<String?>(Routes.SCANNED_BARCODE_KEY, null)
                .collectAsStateWithLifecycle()
            MealEditorScreen(
                onSaved = { nav.popBackStack() },
                onScanBarcode = { nav.navigate(Routes.MEAL_SCANNER) },
                scannedBarcode = barcode,
                onBarcodeConsumed = { entry.savedStateHandle[Routes.SCANNED_BARCODE_KEY] = null },
            )
        }
        composable(Routes.MEAL_SCANNER) {
            BarcodeScannerScreen(
                onScanned = { value ->
                    nav.previousBackStackEntry?.savedStateHandle?.set(Routes.SCANNED_BARCODE_KEY, value)
                    nav.popBackStack()
                },
                onCancel = { nav.popBackStack() },
            )
        }
    }
}

private data class Tab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accent: (io.bubblymarble.fitness.core.designsystem.theme.FeatureAccents) -> Color,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(
    onStartSession: (Long) -> Unit,
    onAddMeal: () -> Unit,
    onEditMeal: (Long) -> Unit,
) {
    val nav = rememberNavController()
    val accents = LocalFeatureAccents.current
    val tabs = listOf(
        Tab(Routes.PLANS, "Plans", Icons.Default.ListAlt) { it.plans },
        Tab(Routes.WORKOUTS, "Workouts", Icons.Default.FitnessCenter) { it.workouts },
        Tab(Routes.MEALS, "Meals", Icons.Default.Restaurant) { it.meals },
        Tab(Routes.MEASUREMENTS, "Body", Icons.Default.Straighten) { it.body },
        Tab(Routes.STATS, "Stats", Icons.Default.BarChart) { it.stats },
        Tab(Routes.SETTINGS, "Settings", Icons.Default.Settings) { it.settings },
    )
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val currentTab = tabs.firstOrNull { it.route == currentRoute } ?: tabs[0]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(currentTab.accent(accents)),
                        )
                        Text(
                            text = currentTab.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                tabs.forEach { tab ->
                    val selected = backStack?.destination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = tab.accent(accents),
                            selectedTextColor = tab.accent(accents),
                            indicatorColor = tab.accent(accents).copy(alpha = 0.18f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.PLANS,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Routes.PLANS) { PlansScreen() }
            composable(Routes.WORKOUTS) { WorkoutsScreen(onStartSession = onStartSession) }
            composable(Routes.MEALS) { MealsScreen(onAddMeal = onAddMeal, onEditMeal = onEditMeal) }
            composable(Routes.MEASUREMENTS) { MeasurementsScreen() }
            composable(Routes.STATS) { StatsScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}
