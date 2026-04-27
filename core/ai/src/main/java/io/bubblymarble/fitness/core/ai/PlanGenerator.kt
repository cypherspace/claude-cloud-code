package io.bubblymarble.fitness.core.ai

import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.model.Equipment
import io.bubblymarble.fitness.core.data.model.Exercise
import io.bubblymarble.fitness.core.data.model.ExperienceLevel
import io.bubblymarble.fitness.core.data.model.GoalType
import io.bubblymarble.fitness.core.data.model.TemplateItem
import io.bubblymarble.fitness.core.data.model.UserProfile
import io.bubblymarble.fitness.core.data.model.WorkoutTemplate
import io.bubblymarble.fitness.core.data.prefs.SecurePrefs
import io.bubblymarble.fitness.core.data.repository.ExerciseRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class PlanRequest(
    val sessionsPerWeek: Int,
    val minutesPerSession: Int,
    val weeks: Int = 4,
)

@Singleton
class PlanGenerator @Inject constructor(
    private val gemini: GeminiClient,
    private val securePrefs: SecurePrefs,
    private val exercises: ExerciseRepository,
    private val time: TimeSource,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** Generates a plan via Gemini, falling back to a deterministic template if the AI is unavailable. */
    suspend fun generate(profile: UserProfile, request: PlanRequest): List<WorkoutTemplate> {
        val key = securePrefs.geminiKey()
        val owned = Equipment.resolve(profile.equipment, profile.ownedEquipment)
        val catalogue = exercises.observeAllSnapshot().filter { matchesOwned(it, owned) }
        if (key.isNullOrBlank() || catalogue.isEmpty()) {
            return FallbackPlan.build(profile, request, catalogue, time.now().toEpochMilli())
        }
        return runCatching {
            val raw = gemini.generateJson(
                apiKey = key,
                prompt = buildPrompt(profile, request, catalogue, owned),
                responseSchema = planSchema(),
            )
            val parsed = json.decodeFromString(GeneratedPlan.serializer(), raw)
            parsed.toTemplates(catalogue, time.now().toEpochMilli())
        }.getOrElse {
            FallbackPlan.build(profile, request, catalogue, time.now().toEpochMilli())
        }
    }

    private fun buildPrompt(
        profile: UserProfile,
        req: PlanRequest,
        catalogue: List<Exercise>,
        owned: Set<String>,
    ): String {
        val cat = catalogue.joinToString("\n") { "- ${it.id}: ${it.name} (${it.primaryMuscle}, ${it.equipment}, ${it.level})" }
        val ownedDesc = owned.sorted().joinToString(", ")
        return """
            You are a certified strength coach. Generate a ${req.weeks}-week plan with
            ${req.sessionsPerWeek} sessions per week, each ${req.minutesPerSession} minutes.

            Trainee profile:
            - Goal: ${profile.goal}
            - Experience: ${profile.experience}
            - Equipment access tier: ${profile.equipment}
            - Owned equipment: $ownedDesc
            - Weekly sessions target: ${profile.weeklyTargetSessions}
            - Injury notes: ${profile.injuryNotes ?: "none"}

            Use ONLY exercises from this catalogue (referenced by id). The catalogue has
            already been filtered to the trainee's owned equipment, so you do not need
            to filter further:
            $cat

            Return JSON matching the provided schema. Each session should have a focused theme,
            sensible exercise ordering (compounds before isolations), and rest tuned to the goal.
        """.trimIndent()
    }

    private fun matchesOwned(ex: Exercise, owned: Set<String>): Boolean {
        val slug = ex.equipment.lowercase().trim()
        if (slug.isBlank()) return Equipment.BODYWEIGHT in owned
        return slug in owned
    }

    private fun planSchema(): JsonObject = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            put("sessions", buildJsonObject {
                put("type", "array")
                put("items", buildJsonObject {
                    put("type", "object")
                    put("properties", buildJsonObject {
                        put("name", buildJsonObject { put("type", "string") })
                        put("description", buildJsonObject { put("type", "string") })
                        put("exercises", buildJsonObject {
                            put("type", "array")
                            put("items", buildJsonObject {
                                put("type", "object")
                                put("properties", buildJsonObject {
                                    put("exerciseId", buildJsonObject { put("type", "string") })
                                    put("sets", buildJsonObject { put("type", "integer") })
                                    put("reps", buildJsonObject { put("type", "integer") })
                                    put("durationSec", buildJsonObject { put("type", "integer") })
                                    put("restSec", buildJsonObject { put("type", "integer") })
                                    put("notes", buildJsonObject { put("type", "string") })
                                })
                                put("required", buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive("exerciseId")) })
                            })
                        })
                    })
                    put("required", buildJsonArray {
                        add(kotlinx.serialization.json.JsonPrimitive("name"))
                        add(kotlinx.serialization.json.JsonPrimitive("exercises"))
                    })
                })
            })
        })
        put("required", buildJsonArray { add(kotlinx.serialization.json.JsonPrimitive("sessions")) })
    }
}

@Serializable
internal data class GeneratedPlan(val sessions: List<GeneratedSession> = emptyList()) {
    fun toTemplates(catalogue: List<Exercise>, nowEpochMs: Long): List<WorkoutTemplate> {
        val ids = catalogue.mapTo(HashSet()) { it.id }
        return sessions.mapIndexedNotNull { idx, session ->
            val items = session.exercises
                .filter { it.exerciseId in ids }
                .mapIndexed { i, e ->
                    TemplateItem(
                        exerciseId = e.exerciseId,
                        orderIndex = i,
                        targetSets = (e.sets ?: 3).coerceAtLeast(1),
                        targetReps = e.reps,
                        targetDurationSec = e.durationSec,
                        targetRestSec = (e.restSec ?: 60).coerceAtLeast(15),
                        notes = e.notes,
                    )
                }
            if (items.isEmpty()) return@mapIndexedNotNull null
            WorkoutTemplate(
                id = 0,
                name = session.name ?: "Session ${idx + 1}",
                description = session.description.orEmpty(),
                goal = GoalType.GENERAL_FITNESS,
                generatedByAi = true,
                createdAt = java.time.Instant.ofEpochMilli(nowEpochMs),
                items = items,
            )
        }
    }
}

@Serializable
internal data class GeneratedSession(
    val name: String? = null,
    val description: String? = null,
    val exercises: List<GeneratedExercise> = emptyList(),
)

@Serializable
internal data class GeneratedExercise(
    val exerciseId: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val durationSec: Int? = null,
    val restSec: Int? = null,
    val notes: String? = null,
)

/** Deterministic fallback when Gemini is unavailable or returns nothing usable. */
internal object FallbackPlan {
    fun build(
        profile: UserProfile,
        req: PlanRequest,
        catalogue: List<Exercise>,
        nowEpochMs: Long,
    ): List<WorkoutTemplate> {
        if (catalogue.isEmpty()) return emptyList()
        val byMuscle = catalogue.groupBy { it.primaryMuscle.lowercase() }
        val days = listOf(
            "Push" to listOf("chest", "shoulders", "triceps"),
            "Pull" to listOf("back", "biceps"),
            "Legs" to listOf("quadriceps", "hamstrings", "glutes", "calves"),
            "Full body" to listOf("chest", "back", "quadriceps", "core"),
        ).take(req.sessionsPerWeek.coerceIn(1, 4))

        val targetSets = if (profile.experience == ExperienceLevel.BEGINNER) 3 else 4
        val targetReps = when (profile.goal) {
            GoalType.STRENGTH -> 5
            GoalType.HYPERTROPHY -> 8
            GoalType.FAT_LOSS, GoalType.ENDURANCE -> 12
            else -> 10
        }

        return days.mapIndexed { idx, (name, muscles) ->
            val items = muscles.flatMap { m -> byMuscle[m].orEmpty() }
                .distinctBy { it.id }
                .take(5)
                .mapIndexed { i, ex ->
                    TemplateItem(
                        exerciseId = ex.id,
                        orderIndex = i,
                        targetSets = targetSets,
                        targetReps = targetReps,
                        targetDurationSec = null,
                        targetRestSec = if (profile.goal == GoalType.STRENGTH) 120 else 60,
                        notes = null,
                    )
                }
            WorkoutTemplate(
                id = 0,
                name = name,
                description = "Auto-generated $name day (offline fallback).",
                goal = profile.goal,
                generatedByAi = false,
                createdAt = java.time.Instant.ofEpochMilli(nowEpochMs + idx),
                items = items,
            )
        }.filter { it.items.isNotEmpty() }
    }
}

private suspend fun ExerciseRepository.observeAllSnapshot(): List<Exercise> = observeAll().first()
