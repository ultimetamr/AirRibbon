package com.pico.swan.airribbon.domain.usecase

import com.pico.swan.airribbon.domain.model.MAX_STROKE_POINTS
import com.pico.swan.airribbon.domain.model.Point3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class StrokeDownsamplerTest {
    @Test fun overflowReducesBelowHardLimitAndKeepsEndpoints() {
        val points = List(700) { Point3(it * 0.01f, sin(it * 0.08f), 0f) }
        val reduced = StrokeDownsampler.reduce(points)
        assertTrue(reduced.size <= MAX_STROKE_POINTS)
        assertEquals(points.first(), reduced.first())
        assertEquals(points.last(), reduced.last())
    }

    @Test fun appendFiltersTinyJitter() {
        val start = listOf(Point3(0f, 0f, 0f))
        assertEquals(start, StrokeDownsampler.append(start, Point3(0.001f, 0f, 0f)))
        assertEquals(2, StrokeDownsampler.append(start, Point3(0.01f, 0f, 0f)).size)
    }
}
