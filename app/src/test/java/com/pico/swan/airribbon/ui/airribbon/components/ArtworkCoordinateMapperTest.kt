package com.pico.swan.airribbon.ui.airribbon.components

import com.pico.swan.airribbon.domain.model.ArtworkGroupTransform
import com.pico.swan.airribbon.domain.model.Point3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtworkCoordinateMapperTest {
    @Test fun minimumScaleProducesFiniteLocalPointAroundStableCenter() {
        val center = Point3(2f, 1f, -1f)
        val mapped = ArtworkCoordinateMapper.sceneToArtwork(
            Point3(2.25f, 1f, -1f),
            ArtworkGroupTransform(uniformScale = 0.25f),
            center,
        )
        assertEquals(Point3(3f, 1f, -1f), mapped)
    }

    @Test fun invalidScaleOrPointCannotReachRenderer() {
        assertNull(ArtworkCoordinateMapper.sceneToArtwork(
            Point3(Float.NaN, 0f, 0f), ArtworkGroupTransform(uniformScale = 0.25f), Point3(0f, 0f, 0f),
        ))
        assertNull(ArtworkCoordinateMapper.sceneToArtwork(
            Point3(0f, 0f, 0f), ArtworkGroupTransform(uniformScale = 0f), Point3(0f, 0f, 0f),
        ))
    }
}
