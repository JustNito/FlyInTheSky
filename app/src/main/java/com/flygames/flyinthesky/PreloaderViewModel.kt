package com.flygames.flyinthesky

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PreloaderViewModel: ViewModel() {

    private var _status by mutableStateOf(PreloaderStatus.Loading)
    val status
        get() = _status

    fun wait() = viewModelScope.launch {
        delay(5000)
        _status = PreloaderStatus.OK
    }
}

enum class PreloaderStatus{
    Loading, OK
}