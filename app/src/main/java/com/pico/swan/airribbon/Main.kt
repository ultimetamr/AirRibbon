package com.pico.swan.airribbon

import com.pico.swan.airribbon.ui.airribbon.AirRibbonScreen
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultStage
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultStage {
            PicoTheme {
                AirRibbonScreen()
            }
        }
    }
