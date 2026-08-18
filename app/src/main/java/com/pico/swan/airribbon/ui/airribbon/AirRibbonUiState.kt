package com.pico.swan.airribbon.ui.airribbon

import com.pico.swan.airribbon.domain.model.*

data class AirRibbonUiState(
    val mode: AppMode = AppMode.DRAW,
    val brush: BrushSpec = BrushSpec(),
    val strokes: List<Stroke> = emptyList(),
    val currentStroke: Stroke? = null,
    val groupTransform: ArtworkGroupTransform = ArtworkGroupTransform(),
    val inputSource: InputSource = InputSource.HAND,
    val undoDepth: Int = 0,
    val statusMessage: String = "正在初始化…",
    val saveSucceeded: Boolean = true,
    val exportRequestId: Long = 0L,
    val exportInProgress: Boolean = false,
    val lastExportUri: String? = null,
) {
    val isDrawing: Boolean get() = currentStroke != null
}

sealed interface AirRibbonEvent {
    data class SelectMode(val mode: AppMode) : AirRibbonEvent
    data class SelectBrush(val type: BrushType) : AirRibbonEvent
    data class SelectColor(val color: PaletteColor) : AirRibbonEvent
    data class SelectWidth(val width: StrokeWidth) : AirRibbonEvent
    data class StartStroke(val point: Point3) : AirRibbonEvent
    data class AppendPoint(val point: Point3) : AirRibbonEvent
    data class FinishStroke(val reason: StrokeClosedReason = StrokeClosedReason.RELEASED) : AirRibbonEvent
    data object TrackingLost : AirRibbonEvent
    data class SetInputSource(val source: InputSource) : AirRibbonEvent
    data object Undo : AirRibbonEvent
    data object RequestClear : AirRibbonEvent
    data object ConfirmClear : AirRibbonEvent
    data object CancelClear : AirRibbonEvent
    data class TranslateGroup(val delta: Point3) : AirRibbonEvent
    data class ScaleGroup(val factor: Float) : AirRibbonEvent
    data class RotateGroup(val yDegrees: Float) : AirRibbonEvent
    data object ResetGroup : AirRibbonEvent
    data object RequestExport : AirRibbonEvent
    data class ExportFinished(val result: ExportResult) : AirRibbonEvent
}
