package io.bubblymarble.fitness.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.bubblymarble.fitness.core.data.db.dao.ExerciseDao
import io.bubblymarble.fitness.core.data.db.dao.HealthSampleDao
import io.bubblymarble.fitness.core.data.db.dao.IngredientDao
import io.bubblymarble.fitness.core.data.db.dao.MealDao
import io.bubblymarble.fitness.core.data.db.dao.SessionDao
import io.bubblymarble.fitness.core.data.db.dao.StreakDao
import io.bubblymarble.fitness.core.data.db.dao.UserProfileDao
import io.bubblymarble.fitness.core.data.db.dao.WorkoutTemplateDao
import io.bubblymarble.fitness.core.data.db.entities.BodyMeasurementEntity
import io.bubblymarble.fitness.core.data.db.entities.ExerciseEntity
import io.bubblymarble.fitness.core.data.db.entities.HealthSampleEntity
import io.bubblymarble.fitness.core.data.db.entities.IngredientEntity
import io.bubblymarble.fitness.core.data.db.entities.MealEntity
import io.bubblymarble.fitness.core.data.db.entities.MealItemEntity
import io.bubblymarble.fitness.core.data.db.entities.SessionSetEntity
import io.bubblymarble.fitness.core.data.db.entities.StreakStateEntity
import io.bubblymarble.fitness.core.data.db.entities.UserProfileEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutSessionEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutTemplateEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutTemplateExerciseEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutTemplateEntity::class,
        WorkoutTemplateExerciseEntity::class,
        WorkoutSessionEntity::class,
        SessionSetEntity::class,
        HealthSampleEntity::class,
        UserProfileEntity::class,
        StreakStateEntity::class,
        IngredientEntity::class,
        MealEntity::class,
        MealItemEntity::class,
        BodyMeasurementEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun sessionDao(): SessionDao
    abstract fun healthSampleDao(): HealthSampleDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun streakDao(): StreakDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealDao(): MealDao

    companion object {
        const val NAME = "bubblymarble.db"
    }
}
