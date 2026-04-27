package io.bubblymarble.fitness.core.data.model

import java.time.Instant
import java.time.LocalDate

enum class GoalType { GENERAL_FITNESS, STRENGTH, HYPERTROPHY, FAT_LOSS, ENDURANCE, MOBILITY }

enum class ExperienceLevel { BEGINNER, INTERMEDIATE, ADVANCED }

enum class EquipmentAccess { BODYWEIGHT_ONLY, MINIMAL_HOME, FULL_GYM }

enum class StreakRule { DAILY, WEEKLY_TARGET }

data class UserProfile(
    val displayName: String,
    val dob: LocalDate?,
    val sexAtBirth: String?,
    val heightCm: Double?,
    val goal: GoalType,
    val weeklyTargetSessions: Int,
    val equipment: EquipmentAccess,
    val experience: ExperienceLevel,
    val injuryNotes: String?,
)

data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    val primaryMuscle: String,
    val secondaryMuscles: List<String>,
    val equipment: String,
    val level: String,
    val instructions: List<String>,
    val mediaUrl: String?,
)

data class WorkoutTemplate(
    val id: Long,
    val name: String,
    val description: String,
    val goal: GoalType,
    val generatedByAi: Boolean,
    val createdAt: Instant,
    val items: List<TemplateItem>,
)

data class TemplateItem(
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetDurationSec: Int?,
    val targetRestSec: Int,
    val notes: String?,
)

data class WorkoutSession(
    val id: Long,
    val templateId: Long?,
    val startedAt: Instant,
    val completedAt: Instant?,
    val sets: List<SessionSet>,
    val rpe: Int?,
    val notes: String?,
)

data class SessionSet(
    val id: Long,
    val exerciseId: String,
    val setNumber: Int,
    val repsActual: Int?,
    val weightKg: Double?,
    val durationSec: Int?,
    val distanceM: Double?,
    val hrAvgBpm: Int?,
    val hrMaxBpm: Int?,
    val spo2MinPercent: Int?,
    val completedAt: Instant?,
)

data class StreakState(
    val lastCompleted: LocalDate?,
    val currentLength: Int,
    val longestLength: Int,
    val rule: StreakRule,
    val weeklyTarget: Int,
)
