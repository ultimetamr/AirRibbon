package com.pico.swan.airribbon.ui.airribbon

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pico.swan.airribbon.data.repository.LocalArtworkRepository
import com.pico.swan.airribbon.domain.model.*
import com.pico.swan.airribbon.domain.usecase.AirRibbonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AirRibbonViewModel(private val useCase: AirRibbonUseCase) : ViewModel() {
    private val _state = MutableStateFlow(useCase.snapshot().toUiState())
    val state: StateFlow<AirRibbonUiState> = _state.asStateFlow()

    fun onEvent(event: AirRibbonEvent) {
        when (event) {
            is AirRibbonEvent.SelectMode -> useCase.setMode(event.mode)
            is AirRibbonEvent.SelectBrush -> useCase.setBrush(state.value.brush.copy(type = event.type))
            is AirRibbonEvent.SelectColor -> useCase.setBrush(state.value.brush.copy(color = event.color))
            is AirRibbonEvent.SelectWidth -> useCase.setBrush(state.value.brush.copy(width = event.width))
            is AirRibbonEvent.StartStroke -> useCase.startStroke(event.point)
            is AirRibbonEvent.AppendPoint -> useCase.appendPoint(event.point)
            is AirRibbonEvent.FinishStroke -> useCase.finishStroke(event.reason)
            AirRibbonEvent.TrackingLost -> useCase.finishStroke(StrokeClosedReason.TRACKING_LOST)
            is AirRibbonEvent.SetInputSource -> useCase.setInputSource(event.source)
            AirRibbonEvent.Undo -> useCase.undo()
            AirRibbonEvent.RequestClear -> useCase.setMode(AppMode.CLEAR_CONFIRM)
            AirRibbonEvent.ConfirmClear -> useCase.confirmClear()
            AirRibbonEvent.CancelClear -> useCase.cancelClear()
            is AirRibbonEvent.TranslateGroup -> useCase.translateGroup(event.delta)
            is AirRibbonEvent.ScaleGroup -> useCase.scaleGroup(event.factor)
            is AirRibbonEvent.RotateGroup -> useCase.rotateGroup(event.yDegrees)
            AirRibbonEvent.ResetGroup -> useCase.resetGroup()
            AirRibbonEvent.RequestExport -> _state.value = _state.value.copy(
                exportRequestId = _state.value.exportRequestId + 1,
                exportInProgress = true,
                lastExportUri = null,
            )
            is AirRibbonEvent.ExportFinished -> {
                useCase.setStatus(event.result.message, event.result.success)
                _state.value = useCase.snapshot().toUiState().copy(
                    exportRequestId = _state.value.exportRequestId,
                    exportInProgress = false,
                    lastExportUri = event.result.uri,
                )
                return
            }
        }
        val exportFields = _state.value
        _state.value = useCase.snapshot().toUiState().copy(
            exportRequestId = exportFields.exportRequestId,
            exportInProgress = exportFields.exportInProgress,
            lastExportUri = exportFields.lastExportUri,
        )
    }

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AirRibbonViewModel(AirRibbonUseCase(LocalArtworkRepository(appContext))) as T
    }
}

private fun com.pico.swan.airribbon.domain.usecase.SessionSnapshot.toUiState() = AirRibbonUiState(
    mode = mode,
    brush = brush,
    strokes = strokes,
    currentStroke = currentStroke,
    groupTransform = groupTransform,
    inputSource = inputSource,
    undoDepth = undoDepth,
    statusMessage = statusMessage,
    saveSucceeded = saveSucceeded,
)
