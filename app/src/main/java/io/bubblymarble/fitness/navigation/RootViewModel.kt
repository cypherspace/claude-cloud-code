package io.bubblymarble.fitness.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.bubblymarble.fitness.core.data.repository.UserProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RootState(val isOnboarded: Boolean = false, val loading: Boolean = true)

@HiltViewModel
class RootViewModel @Inject constructor(
    profiles: UserProfileRepository,
) : ViewModel() {

    val state: StateFlow<RootState> = profiles.observeProfile()
        .map { profile -> RootState(isOnboarded = profile != null, loading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RootState())
}
