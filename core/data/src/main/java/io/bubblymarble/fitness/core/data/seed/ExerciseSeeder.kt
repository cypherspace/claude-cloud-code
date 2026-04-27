package io.bubblymarble.fitness.core.data.seed

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.bubblymarble.fitness.core.common.IoDispatcher
import io.bubblymarble.fitness.core.data.model.Exercise
import io.bubblymarble.fitness.core.data.repository.ExerciseRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class ExerciseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ExerciseRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun seedIfEmpty() = withContext(io) {
        if (repository.count() > 0) return@withContext
        val items = loadFromAssets()
        repository.upsert(items)
    }

    private fun loadFromAssets(): List<Exercise> {
        val raw = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        val list = json.decodeFromString<List<SeedExercise>>(raw)
        return list.map {
            Exercise(
                id = it.id,
                name = it.name,
                category = it.category,
                primaryMuscle = it.primaryMuscle,
                secondaryMuscles = it.secondaryMuscles,
                equipment = it.equipment,
                level = it.level,
                instructions = it.instructions,
                mediaUrl = it.mediaUrl,
            )
        }
    }

    companion object {
        const val ASSET_PATH = "exercises/exercises.json"
    }
}
