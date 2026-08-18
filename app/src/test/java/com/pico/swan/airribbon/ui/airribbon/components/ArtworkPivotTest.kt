package com.pico.swan.airribbon.ui.airribbon.components

import com.pico.swan.airribbon.domain.model.BrushSpec
import com.pico.swan.airribbon.domain.model.Point3
import com.pico.swan.airribbon.domain.model.Stroke
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkPivotTest {
    @Test
    fun `uses artwork bounds center and offsets content around it`() {
        val stroke = Stroke("one", listOf(Point3(-2f, 1f, 4f), Point3(6f, 5f, -2f)), BrushSpec(), null, 0L)
        val center = ArtworkPivot.centerOf(listOf(stroke))
        val placement = ArtworkPivot.placement(Point3(1f, 2f, 3f), center)

        assertEquals(Point3(2f, 3f, 1f), center)
        assertEquals(Point3(3f, 5f, 4f), placement.pivotPosition)
        assertEquals(Point3(-2f, -3f, -1f), placement.contentOffset)
    }
}
