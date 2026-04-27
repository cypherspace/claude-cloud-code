package io.bubblymarble.fitness.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.bubblymarble.fitness.feature.meals.MealEditorScreen
import io.bubblymarble.fitness.feature.meals.MealsScreen
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
        composable(Routes.MEAL_EDITOR) {
            MealEditorScreen(onSaved = { nav.popBackStack() })
        }
    }
}

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun HomeScaffold(
    onStartSession: (Long) -> Unit,
    onAddMeal: () -> Unit,
    onEditMeal: (Long) -> Unit,
) {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab(Routes.PLANS, "Plans", Icons.Default.ListAlt),
        Tab(Routes.WORKOUTS, "Workouts", Icons.Default.FitnessCenter),
        Tab(Routes.MEALS, "Meals", Icons.Default.Restaurant),
        Tab(Routes.MEASUREMENTS, "Body", Icons.Default.Straighten),
        Tab(Routes.STATS, "Stats", Icons.Default.BarChart),
        Tab(Routes.SETTINGS, "Settings", Icons.Default.Settings),
    )
    Scaffold(
        bottomBar = {
            val backStack by nav.currentBackStackEntryAsState()
            val current = backStack?.destination
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = current?.hierarchy?.any { it.route == tab.route } == true
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
                        label = { Text(tab.label) },
                    )
                }
            }
        }
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
