package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.db.dao.UserProfileDao
import io.bubblymarble.fitness.core.data.model.UserProfile
import io.bubblymarble.fitness.core.data.model.toDomain
import io.bubblymarble.fitness.core.data.model.toEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class UserProfileRepository @Inject constructor(
    private val dao: UserProfileDao,
    private val time: TimeSource,
) {
    fun observeProfile(): Flow<UserProfile?> =
        dao.observe().map { it?.toDomain() }

    suspend fun get(): UserProfile? = dao.get()?.toDomain()

    suspend fun save(profile: UserProfile) {
        dao.upsert(profile.toEntity(time.now().toEpochMilli()))
    }
}
