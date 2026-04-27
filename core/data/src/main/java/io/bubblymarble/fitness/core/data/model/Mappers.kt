package io.bubblymarble.fitness.core.data.model

import io.bubblymarble.fitness.core.data.db.entities.ExerciseEntity
import io.bubblymarble.fitness.core.data.db.entities.SessionSetEntity
import io.bubblymarble.fitness.core.data.db.entities.StreakStateEntity
import io.bubblymarble.fitness.core.data.db.entities.UserProfileEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutSessionEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutTemplateEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutTemplateExerciseEntity
import java.time.Instant
import java.time.LocalDate

private const val LIST_SEP = "|"
private const val MULTILINE_SEP = "\n"

fun ExerciseEntity.toDomain() = Exercise(
    id = id,
    name = name,
    category = category,
    primaryMuscle = primaryMuscle,
    secondaryMuscles = secondaryMuscles.split(LIST_SEP).filter { it.isNotBlank() },
    equipment = equipment,
    level = level,
    instructions = instructions.split(MULTILINE_SEP).filter { it.isNotBlank() },
    mediaUrl = mediaUrl,
)

fun Exercise.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    category = category,
    primaryMuscle = primaryMuscle,
    secondaryMuscles = secondaryMuscles.joinToString(LIST_SEP),
    equipment = equipment,
    level = level,
    instructions = instructions.joinToString(MULTILINE_SEP),
    mediaUrl = mediaUrl,
)

fun WorkoutTemplateEntity.toDomain(items: List<WorkoutTemplateExerciseEntity>) = WorkoutTemplate(
    id = id,
    name = name,
    description = description,
    goal = runCatching { GoalType.valueOf(goal) }.getOrDefault(GoalType.GENERAL_FITNESS),
    generatedByAi = generatedByAi,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
    items = items.map {
        TemplateItem(
            exerciseId = it.exerciseId,
            orderIndex = it.orderIndex,
            targetSets = it.targetSets,
            targetReps = it.targetReps,
            targetDurationSec = it.targetDurationSec,
            targetRestSec = it.targetRestSec,
            notes = it.notes,
        )
    },
)

fun WorkoutTemplate.toEntity() = WorkoutTemplateEntity(
    id = id,
    name = name,
    description = description,
    goal = goal.name,
    generatedByAi = generatedByAi,
    createdAtEpochMs = createdAt.toEpochMilli(),
)

fun TemplateItem.toEntity(templateId: Long) = WorkoutTemplateExerciseEntity(
    templateId = templateId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    targetSets = targetSets,
    targetReps = targetReps,
    targetDurationSec = targetDurationSec,
    targetRestSec = targetRestSec,
    notes = notes,
)

fun WorkoutSessionEntity.toDomain(sets: List<SessionSetEntity>) = WorkoutSession(
    id = id,
    templateId = templateId,
    startedAt = Instant.ofEpochMilli(startedAtEpochMs),
    completedAt = completedAtEpochMs?.let(Instant::ofEpochMilli),
    sets = sets.map {
        SessionSet(
            id = it.id,
            exerciseId = it.exerciseId,
            setNumber = it.setNumber,
            repsActual = it.repsActual,
            weightKg = it.weightKg,
            durationSec = it.durationSec,
            distanceM = it.distanceM,
            hrAvgBpm = it.hrAvgBpm,
            hrMaxBpm = it.hrMaxBpm,
            spo2MinPercent = it.spo2MinPercent,
            completedAt = it.completedAtEpochMs?.let(Instant::ofEpochMilli),
        )
    },
    rpe = rpe,
    notes = notes,
)

fun UserProfileEntity.toDomain() = UserProfile(
    displayName = displayName,
    dob = dobEpochDay?.let(LocalDate::ofEpochDay),
    sexAtBirth = sexAtBirth,
    heightCm = heightCm,
    goal = runCatching { GoalType.valueOf(goalType) }.getOrDefault(GoalType.GENERAL_FITNESS),
    weeklyTargetSessions = weeklyTargetSessions,
    equipment = runCatching { EquipmentAccess.valueOf(equipmentAccess) }.getOrDefault(EquipmentAccess.BODYWEIGHT_ONLY),
    experience = runCatching { ExperienceLevel.valueOf(experienceLevel) }.getOrDefault(ExperienceLevel.BEGINNER),
    injuryNotes = injuryNotes,
)

fun UserProfile.toEntity(createdAtEpochMs: Long) = UserProfileEntity(
    displayName = displayName,
    dobEpochDay = dob?.toEpochDay(),
    sexAtBirth = sexAtBirth,
    heightCm = heightCm,
    goalType = goal.name,
    weeklyTargetSessions = weeklyTargetSessions,
    equipmentAccess = equipment.name,
    experienceLevel = experience.name,
    injuryNotes = injuryNotes,
    createdAtEpochMs = createdAtEpochMs,
)

fun StreakStateEntity.toDomain() = StreakState(
    lastCompleted = lastCompletedEpochDay?.let(LocalDate::ofEpochDay),
    currentLength = currentLength,
    longestLength = longestLength,
    rule = runCatching { StreakRule.valueOf(ruleType) }.getOrDefault(StreakRule.DAILY),
    weeklyTarget = ruleTargetPerWeek,
)

fun StreakState.toEntity() = StreakStateEntity(
    lastCompletedEpochDay = lastCompleted?.toEpochDay(),
    currentLength = currentLength,
    longestLength = longestLength,
    ruleType = rule.name,
    ruleTargetPerWeek = weeklyTarget,
)
