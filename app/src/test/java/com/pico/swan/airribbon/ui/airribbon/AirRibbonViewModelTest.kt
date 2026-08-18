package com.pico.swan.airribbon.ui.airribbon

import com.pico.swan.airribbon.data.repository.InMemoryArtworkRepository
import com.pico.swan.airribbon.domain.model.*
import com.pico.swan.airribbon.domain.usecase.AirRibbonUseCase
import org.junit.Assert.*
import org.junit.Test

class AirRibbonViewModelTest {
    private var id = 0
    private fun subject(repository: InMemoryArtworkRepository = InMemoryArtworkRepository()) =
        AirRibbonViewModel(AirRibbonUseCase(repository, { 10L }, { "vm${id++}" }))

    @Test fun initRestoresSavedStrokes() {
        val saved = Stroke("saved", listOf(Point3(0f, 0f, 0f), Point3(0.1f, 0f, 0f)), BrushSpec(), StrokeClosedReason.RELEASED, 1L)
        val vm = subject(InMemoryArtworkRepository(ArtworkDocument(strokes = listOf(saved))))
        assertEquals(1, vm.state.value.strokes.size)
        assertEquals(1, vm.state.value.undoDepth)
    }

    @Test fun happyPathDrawsOneStroke() {
        val vm = subject()
        vm.onEvent(AirRibbonEvent.StartStroke(Point3(0f, 0f, 0f)))
        vm.onEvent(AirRibbonEvent.AppendPoint(Point3(0.02f, 0f, 0f)))
        vm.onEvent(AirRibbonEvent.FinishStroke())
        assertEquals(1, vm.state.value.strokes.size)
        assertFalse(vm.state.value.isDrawing)
    }

    @Test fun trackingLossNeverLeavesDrawingActive() {
        val vm = subject()
        vm.onEvent(AirRibbonEvent.StartStroke(Point3(0f, 0f, 0f)))
        vm.onEvent(AirRibbonEvent.AppendPoint(Point3(0.02f, 0f, 0f)))
        vm.onEvent(AirRibbonEvent.TrackingLost)
        assertFalse(vm.state.value.isDrawing)
        assertEquals(StrokeClosedReason.TRACKING_LOST, vm.state.value.strokes.single().closedReason)
    }

    @Test fun clearRequiresExplicitConfirmation() {
        val vm = subject()
        vm.onEvent(AirRibbonEvent.StartStroke(Point3(0f, 0f, 0f)))
        vm.onEvent(AirRibbonEvent.AppendPoint(Point3(0.02f, 0f, 0f)))
        vm.onEvent(AirRibbonEvent.FinishStroke())
        vm.onEvent(AirRibbonEvent.RequestClear)
        assertEquals(AppMode.CLEAR_CONFIRM, vm.state.value.mode)
        assertEquals(1, vm.state.value.strokes.size)
        vm.onEvent(AirRibbonEvent.ConfirmClear)
        assertTrue(vm.state.value.strokes.isEmpty())
    }
}
