package com.example.musicplayer.ui.queue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.model.Track
import com.example.musicplayer.player.MusicPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Thin read-through + forwarding layer over [MusicPlayerController]'s
 * queue — this screen has no independent queue state of its own.
 * Every mutation (reorder/remove/clear/jump) is delegated straight to
 * the controller, which is also the only thing that touches Media3's
 * real playlist, so the UI list and the actual playback queue can
 * never drift apart.
 */
@HiltViewModel
class QueueViewModel @Inject constructor(
    private val playerController: MusicPlayerController
) : ViewModel() {

    val uiState: StateFlow<QueueUiState> = playerController.state
        .map { state ->
            QueueUiState(
                queue = state.queue,
                currentIndex = state.queueIndex,
                isPlaying = state.isPlaying
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = QueueUiState()
        )

    fun onTrackTapped(index: Int) = playerController.playQueueItemAt(index)
    fun onRemove(index: Int) = playerController.removeFromQueue(index)
    fun onMove(fromIndex: Int, toIndex: Int) = playerController.moveQueueItem(fromIndex, toIndex)
    fun onClearQueue() = playerController.clearQueue()
}
