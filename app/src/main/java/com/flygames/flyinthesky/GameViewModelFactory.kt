package com.flygames.flyinthesky

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.travelgames.roadrace.data.Storage

class GameViewModelFactory(private val storage: Storage): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GameViewModel(storage = storage) as T
    }
}