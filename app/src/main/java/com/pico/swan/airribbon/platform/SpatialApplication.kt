package com.pico.swan.airribbon.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.pico.swan.airribbon.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
