package com.pico.swan.airribbon.domain.usecase

import com.pico.swan.airribbon.data.repository.InMemoryArtworkRepository
import com.pico.swan.airribbon.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class AirRibbonUseCaseTest {
    private var id = 0
    private fun subject() = AirRibbonUseCase(InMemoryArtworkRepository(), { 1000L }, { "s${id++}" })

    @Test fun trackingLossClosesAtLastValidPointWithoutNewSegment() {
        val useCase = subject()
        useCase.startStroke(Point3(0f, 0f, 0f))
        useCase.appendPoint(Point3(0.02f, 0f, 0f))
        useCase.finishStroke(StrokeClosedReason.TRACKING_LOST)
        val stroke = useCase.snapshot().strokes.single()
        assertEquals(StrokeClosedReason.TRACKING_LOST, stroke.closedReason)
        assertEquals(Point3(0.02f, 0f, 0f), stroke.points.last())
        assertNull(useCase.snapshot().currentStroke)
    }

    @Test fun undoHistoryIsCappedAtTenAndRemovesNewest() {
        val useCase = subject()
        repeat(12) { index ->
            useCase.startStroke(Point3(index.toFloat(), 0f, 0f))
            useCase.appendPoint(Point3(index + 0.02f, 0f, 0f))
            useCase.finishStroke(StrokeClosedReason.RELEASED)
        }
        assertEquals(10, useCase.snapshot().undoDepth)
        useCase.undo()
        assertEquals(11, useCase.snapshot().strokes.size)
        assertEquals("s10", useCase.snapshot().strokes.last().id)
    }

    @Test fun changingModeSafelyEndsActiveStroke() {
        val useCase = subject()
        useCase.startStroke(Point3(0f, 0f, 0f))
        useCase.appendPoint(Point3(0.02f, 0f, 0f))
        useCase.setMode(AppMode.EDIT)
        assertNull(useCase.snapshot().currentStroke)
        assertEquals(StrokeClosedReason.MODE_SWITCH, useCase.snapshot().strokes.single().closedReason)
        assertEquals(AppMode.EDIT, useCase.snapshot().mode)
    }

    @Test fun groupTransformOnlyChangesInEditMode() {
        val useCase = subject()
        useCase.translateGroup(Point3(1f, 0f, 0f))
        assertEquals(Point3(0f, 0f, 0f), useCase.snapshot().groupTransform.position)
        useCase.setMode(AppMode.EDIT)
        useCase.translateGroup(Point3(1f, 0f, 0f))
        useCase.scaleGroup(2f)
        useCase.rotateGroup(30f)
        assertEquals(Point3(1f, 0f, 0f), useCase.snapshot().groupTransform.position)
        assertEquals(2f, useCase.snapshot().groupTransform.uniformScale)
        assertEquals(30f, useCase.snapshot().groupTransform.rotationDegrees.y)
    }

    @Test fun artworkPivotIsCapturedOnceSoRepeatedScalingCannotMoveItsCenter() {
        val useCase = subject()
        useCase.startStroke(Point3(2f, 0f, 0f))
        useCase.appendPoint(Point3(6f, 4f, 2f))
        useCase.finishStroke(StrokeClosedReason.RELEASED)
        useCase.setMode(AppMode.EDIT)
        val fixedPivot = Point3(4f, 2f, 1f)
        assertEquals(fixedPivot, useCase.snapshot().groupTransform.pivot)

        useCase.scaleGroup(0.5f)
        useCase.scaleGroup(1.5f)
        assertEquals(fixedPivot, useCase.snapshot().groupTransform.pivot)
        assertEquals(Point3(0f, 0f, 0f), useCase.snapshot().groupTransform.position)
    }

    @Test fun scaleCannotBecomeZeroNaNOrInfinite() {
        val useCase = subject()
        useCase.setMode(AppMode.EDIT)
        repeat(100) { useCase.scaleGroup(0.1f) }
        assertEquals(MIN_ARTWORK_SCALE, useCase.snapshot().groupTransform.uniformScale)
        useCase.scaleGroup(Float.NaN)
        useCase.scaleGroup(Float.POSITIVE_INFINITY)
        assertEquals(MIN_ARTWORK_SCALE, useCase.snapshot().groupTransform.uniformScale)
    }

    @Test fun invalidOrFlyingSampleSafelyEndsStroke() {
        val useCase = subject()
        useCase.startStroke(Point3(0f, 0f, 0f))
        useCase.appendPoint(Point3(0.02f, 0f, 0f))
        useCase.appendPoint(Point3(Float.NaN, 0f, 0f))
        assertNull(useCase.snapshot().currentStroke)
        assertEquals(StrokeClosedReason.TRACKING_LOST, useCase.snapshot().strokes.single().closedReason)
    }

    @Test fun tenDenseStrokesStayInsidePointAndMeshBudgets() {
        val useCase = subject()
        repeat(10) { strokeIndex ->
            useCase.startStroke(Point3(0f, strokeIndex * 0.02f, 0f))
            repeat(700) { pointIndex ->
                useCase.appendPoint(Point3(pointIndex * 0.007f, strokeIndex * 0.02f, (pointIndex % 17) * 0.002f))
            }
            useCase.finishStroke(StrokeClosedReason.RELEASED)
        }
        val strokes = useCase.snapshot().strokes
        assertEquals(10, strokes.size)
        assertTrue(strokes.all { it.points.size <= MAX_STROKE_POINTS })
        val worstCaseFoamVertices = strokes.sumOf { it.points.size * 6 }
        assertTrue(worstCaseFoamVertices <= 10 * MAX_STROKE_POINTS * 6)
    }
}
