package com.pico.swan.airribbon

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pico.swan.airribbon.data.export.ArtworkPhotoExporter
import com.pico.swan.airribbon.domain.model.ArtworkGroupTransform
import com.pico.swan.airribbon.domain.model.BrushSpec
import com.pico.swan.airribbon.domain.model.Point3
import com.pico.swan.airribbon.domain.model.Stroke
import com.pico.swan.airribbon.domain.model.StrokeClosedReason
import com.pico.swan.airribbon.platform.LaunchActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun launchActivityStaysAlive() {
        ActivityScenario.launch(LaunchActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> assertNotNull(activity) }
        }
    }

    @Test
    fun artworkPngExportsToMediaStore() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val stroke = Stroke(
            id = "export-check",
            points = listOf(Point3(-0.2f, 1f, -1f), Point3(0.2f, 1.3f, -1.1f)),
            brush = BrushSpec(),
            closedReason = StrokeClosedReason.RELEASED,
            createdAtMillis = 1L,
        )
        val result = ArtworkPhotoExporter(context).export(listOf(stroke), ArtworkGroupTransform())
        assertTrue(result.message, result.success)
        assertNotNull(result.uri)
    }
}
