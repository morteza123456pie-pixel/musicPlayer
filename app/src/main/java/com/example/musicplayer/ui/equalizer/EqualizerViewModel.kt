package com.example.musicplayer.ui.equalizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.audio.AudioPreset
import com.example.musicplayer.audio.EqualizerRepository
import com.example.musicplayer.audio.EqualizerUiSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val repository: EqualizerRepository
) : ViewModel() {

    val state = repository.state

    init {
        viewModelScope.launch { repository.refresh() }
    }

    fun setEnabled(enabled: Boolean) = repository.setEnabled(enabled)

    fun setBandLevel(bandIndex: Int, levelMillibel: Int) = repository.setBandLevel(bandIndex, levelMillibel)

    fun applyPreset(preset: AudioPreset) = repository.applyPreset(preset)

    fun setBassBoostEnabled(enabled: Boolean) = repository.setBassBoostEnabled(enabled)

    fun setBassBoostStrength(strength: Int) = repository.setBassBoostStrength(strength)

    // Deliberately does NOT call repository.release() here:
    // EqualizerRepository is an app-scoped Hilt singleton (it keeps
    // its own MediaController connection alive so state stays fresh
    // even if the Equalizer screen is reopened quickly), not something
    // owned by this screen's lifecycle. Its connection is torn down
    // once, at process end, alongside the rest of the app's singletons.
}
