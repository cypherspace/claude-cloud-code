package io.bubblymarble.fitness.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.bubblymarble.fitness.core.common.DefaultDispatcher
import io.bubblymarble.fitness.core.common.IoDispatcher
import io.bubblymarble.fitness.core.common.MainDispatcher
import io.bubblymarble.fitness.core.common.SystemTimeSource
import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.db.FitnessDatabase
import io.bubblymarble.fitness.core.data.db.dao.BodyMeasurementDao
import io.bubblymarble.fitness.core.data.db.dao.ExerciseDao
import io.bubblymarble.fitness.core.data.db.dao.HealthSampleDao
import io.bubblymarble.fitness.core.data.db.dao.IngredientDao
import io.bubblymarble.fitness.core.data.db.dao.MealDao
import io.bubblymarble.fitness.core.data.db.dao.SessionDao
import io.bubblymarble.fitness.core.data.db.dao.StreakDao
import io.bubblymarble.fitness.core.data.db.dao.UserProfileDao
import io.bubblymarble.fitness.core.data.db.dao.WorkoutTemplateDao
import io.bubblymarble.fitness.core.data.prefs.SecurePrefs
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): FitnessDatabase =
        Room.databaseBuilder(ctx, FitnessDatabase::class.java, FitnessDatabase.NAME)
            // Pre-1.0 personal-use app: schema changes wipe and re-seed rather than
            // ship hand-written migrations. Onboarding will repopulate exercises +
            // user profile on next launch.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun exerciseDao(db: FitnessDatabase): ExerciseDao = db.exerciseDao()
    @Provides fun templateDao(db: FitnessDatabase): WorkoutTemplateDao = db.workoutTemplateDao()
    @Provides fun sessionDao(db: FitnessDatabase): SessionDao = db.sessionDao()
    @Provides fun healthSampleDao(db: FitnessDatabase): HealthSampleDao = db.healthSampleDao()
    @Provides fun userProfileDao(db: FitnessDatabase): UserProfileDao = db.userProfileDao()
    @Provides fun streakDao(db: FitnessDatabase): StreakDao = db.streakDao()
    @Provides fun ingredientDao(db: FitnessDatabase): IngredientDao = db.ingredientDao()
    @Provides fun mealDao(db: FitnessDatabase): MealDao = db.mealDao()
    @Provides fun bodyMeasurementDao(db: FitnessDatabase): BodyMeasurementDao = db.bodyMeasurementDao()

    @Provides @Singleton
    fun provideTimeSource(): TimeSource = SystemTimeSource()

    @Provides @Singleton
    fun provideSecurePrefs(@ApplicationContext ctx: Context): SecurePrefs {
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            ctx,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        return SecurePrefs(prefs)
    }

    @Provides @IoDispatcher fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @DefaultDispatcher fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    @Provides @MainDispatcher fun mainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
