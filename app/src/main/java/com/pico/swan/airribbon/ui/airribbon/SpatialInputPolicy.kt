package com.pico.swan.airribbon.ui.airribbon

import com.pico.swan.airribbon.domain.model.Point3

enum class ControllerHand { LEFT, RIGHT }

/** Pure input/layout choices kept outside the SDK boundary so simulator regressions stay testable. */
object SpatialInputPolicy {
    val MAIN_PANEL_OFFSET = Point3(0f, -0.08f, -0.90f)

    /** Keeps spatial drag incremental, planar and within a comfortable reachable window. */
    fun constrainPanelDrag(current: Point3, rawDelta: Point3): Point3 {
        val safeX = rawDelta.x.coerceIn(-MAX_DRAG_UNITS_PER_EVENT, MAX_DRAG_UNITS_PER_EVENT)
        val safeY = rawDelta.y.coerceIn(-MAX_DRAG_UNITS_PER_EVENT, MAX_DRAG_UNITS_PER_EVENT)
        return Point3(
            x = (current.x + safeX * PANEL_DRAG_METRES_PER_UNIT).coerceIn(-0.65f, 0.65f),
            y = (current.y + safeY * PANEL_DRAG_METRES_PER_UNIT).coerceIn(-0.45f, 0.45f),
            z = 0f,
        )
    }

    fun uiBlocksDrawing(nowMillis: Long, blockedUntilMillis: Long): Boolean = nowMillis <= blockedUntilMillis

    fun movedFarEnoughToDraw(start: Point3, current: Point3): Boolean =
        start.distanceTo(current) >= DRAW_ARM_DISTANCE_METRES

    fun selectDrawingHand(
        leftTriggerPressed: Boolean,
        rightTriggerPressed: Boolean,
        leftPoseAvailable: Boolean,
        rightPoseAvailable: Boolean,
        previous: ControllerHand?,
    ): ControllerHand? {
        if (previous == ControllerHand.LEFT && leftTriggerPressed && leftPoseAvailable) return previous
        if (previous == ControllerHand.RIGHT && rightTriggerPressed && rightPoseAvailable) return previous
        if (leftTriggerPressed && leftPoseAvailable) return ControllerHand.LEFT
        if (rightTriggerPressed && rightPoseAvailable) return ControllerHand.RIGHT
        return null
    }

    private const val PANEL_DRAG_METRES_PER_UNIT = 0.00035f
    private const val MAX_DRAG_UNITS_PER_EVENT = 24f
    private const val DRAW_ARM_DISTANCE_METRES = 0.030f
}
