package io.bubblymarble.fitness.core.common

sealed interface Outcome<out T> {
    data class Ok<T>(val value: T) : Outcome<T>
    data class Err(val message: String, val cause: Throwable? = null) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(f: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Ok -> Outcome.Ok(f(value))
    is Outcome.Err -> this
}

inline fun <T> outcome(block: () -> T): Outcome<T> = try {
    Outcome.Ok(block())
} catch (t: Throwable) {
    Outcome.Err(t.message ?: t.javaClass.simpleName, t)
}
