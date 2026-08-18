package com.pico.swan.airribbon.platform

import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.pico.spatial.ui.platform.stub.SpatialLaunchActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LaunchActivity : SpatialLaunchActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            CapturePreviewController.accept(intent.getStringExtra(CAPTURE_STATE_EXTRA))
        }
        super.onCreate(savedInstanceState)
    }

    companion object {
        const val CAPTURE_STATE_EXTRA = "airribbon_capture_state"
    }
}

/** Debug-only intent bridge used to capture reproducible emulator acceptance screenshots. */
object CapturePreviewController {
    private val _state = MutableStateFlow<String?>(null)
    val state = _state.asStateFlow()

    fun accept(value: String?) {
        _state.value = value?.uppercase()?.takeIf { it == "DRAW" || it == "PHOTO" }
    }
}
