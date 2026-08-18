package com.pico.swan.airribbon.ui.airribbon

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.pico.swan.airribbon.domain.model.Point3

class SpatialInputPolicyTest {
    @Test
    fun leftControllerCanOwnTheDrawingRayWhenItIsTheAvailableController() {
        assertEquals(
            ControllerHand.LEFT,
            SpatialInputPolicy.selectDrawingHand(
                leftTriggerPressed = true,
                rightTriggerPressed = false,
                leftPoseAvailable = true,
                rightPoseAvailable = false,
                previous = null,
            ),
        )
    }

    @Test
    fun mainPanelIsCenteredAndPlacedInFrontOfTheHeadset() {
        val offset = SpatialInputPolicy.MAIN_PANEL_OFFSET
        assertEquals(0f, offset.x, 0.001f)
        assertTrue(offset.z < -0.75f)
        assertTrue(offset.z > -1.20f)
    }

    @Test
    fun panelDragIgnoresDepthAndClampsAbnormalDeltasInsideReachableArea() {
        var position = Point3(0f, 0f, 0f)
        repeat(100) {
            position = SpatialInputPolicy.constrainPanelDrag(position, Point3(10_000f, -10_000f, 10_000f))
        }
        assertEquals(0.65f, position.x, 0.001f)
        assertEquals(-0.45f, position.y, 0.001f)
        assertEquals(0f, position.z, 0.001f)
    }

    @Test fun activeUiDragBlocksDrawingUntilCooldownExpires() {
        assertTrue(SpatialInputPolicy.uiBlocksDrawing(1_200L, 1_500L))
        assertEquals(false, SpatialInputPolicy.uiBlocksDrawing(1_501L, 1_500L))
    }

    @Test fun stationaryPinchIsAUiClickWhileMovedPinchArmsDrawing() {
        val start = Point3(0f, 0f, 0f)
        assertEquals(false, SpatialInputPolicy.movedFarEnoughToDraw(start, Point3(0.029f, 0f, 0f)))
        assertTrue(SpatialInputPolicy.movedFarEnoughToDraw(start, Point3(0.030f, 0f, 0f)))
    }

}
