package com.joshafeinberg.babyblocker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BabyBlockerStatus {

    private val _babyBlockerStatus = MutableStateFlow(false)
    val babyBlockerStatus: StateFlow<Boolean> = _babyBlockerStatus

    suspend fun updateStatus(isActive: Boolean) {
        _babyBlockerStatus.emit(isActive)
    }

}