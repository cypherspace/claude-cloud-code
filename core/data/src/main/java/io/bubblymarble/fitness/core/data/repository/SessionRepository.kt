package io.bubblymarble.fitness.core.data.repository

import io.bubblymarble.fitness.core.common.TimeSource
import io.bubblymarble.fitness.core.data.db.dao.SessionDao
import io.bubblymarble.fitness.core.data.db.entities.SessionSetEntity
import io.bubblymarble.fitness.core.data.db.entities.WorkoutSessionEntity
import io.bubblymarble.fitness.core.data.model.WorkoutSession
import io.bubblymarble.fitness.core.data.model.toDomain
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class SessionRepository @Inject constructor(
    private val dao: SessionDao,
    private val time: TimeSource,
) {
    suspend fun startSession(templateId: Long?): Long {
        return dao.insertSession(
            WorkoutSessionEntity(
                templateId = templateId,
                startedAtEpochMs = time.now().toEpochMilli(),
                completedAtEpochMs = null,
            )
        )
    }

    suspend fun complete(sessionId: Long, rpe: Int? = null, notes: String? = null) {
        val current = dao.sessionById(sessionId) ?: return
        dao.updateSession(
            current.copy(
                completedAtEpochMs = time.now().toEpochMilli(),
                rpe = rpe,
                notes = notes,
            )
        )
    }

    suspend fun cancel(sessionId: Long) {
        val current = dao.sessionById(sessionId) ?: return
        // leave completedAt null; treated as abandoned
        dao.updateSession(current.copy(notes = (current.notes ?: "") + " [cancelled]"))
    }

    suspend fun recordSet(set: SessionSetEntity): Long = dao.insertSet(set)

    suspend fun updateSet(set: SessionSetEntity) = dao.updateSet(set)

    suspend fun getSession(sessionId: Long): WorkoutSession? {
        val session = dao.sessionById(sessionId) ?: return null
        val sets = dao.setsForSession(sessionId)
        return session.toDomain(sets)
    }

    fun observeCompleted(): Flow<List<WorkoutSession>> =
        dao.observeCompleted().map { sessions ->
            sessions.map { it.toDomain(dao.setsForSession(it.id)) }
        }

    suspend fun completedSince(epochMs: Long): List<WorkoutSession> {
        val sessions = dao.completedSince(epochMs)
        return sessions.map { it.toDomain(dao.setsForSession(it.id)) }
    }

    fun observeSets(sessionId: Long) = dao.observeSetsForSession(sessionId)

    suspend fun latestCompleted(): WorkoutSession? =
        observeCompleted().first().firstOrNull()
}
