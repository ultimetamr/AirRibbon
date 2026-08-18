package com.pico.swan.airribbon.ui.airribbon.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.gesture.detectSpatialDragGesture
import com.pico.swan.airribbon.domain.model.*
import com.pico.swan.airribbon.ui.airribbon.AirRibbonEvent
import com.pico.swan.airribbon.ui.airribbon.AirRibbonUiState

@Composable
fun MaterialDock(
    state: AirRibbonUiState,
    onEvent: (AirRibbonEvent) -> Unit,
    onDrag: (Point3) -> Unit,
    onRotate: (Float) -> Unit,
    onHide: () -> Unit,
    onTutorial: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier.width(560.dp).clip(RoundedCornerShape(24.dp))
            .background(PicoTheme.colorScheme.fillPrimary).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "AirRibbon · ${if (state.mode == AppMode.DRAW) "绘制模式" else "作品编辑模式"}",
                modifier = Modifier.weight(1f).pointerInput(Unit) {
                    detectSpatialDragGesture(context) { drag ->
                        onDrag(Point3(drag.dragAmount.x, -drag.dragAmount.y, drag.dragAmount.z))
                    }
                },
                style = PicoTheme.typography.titleLarge,
                color = PicoTheme.colorScheme.labelPrimaryLight,
            )
            Button(onClick = onHide) { Text("隐藏") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onRotate(5f) }) { Text("面板左旋") }
            Button(onClick = { onRotate(-5f) }) { Text("面板右旋") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(AirRibbonEvent.SelectMode(AppMode.DRAW)) }) { Text(if (state.mode == AppMode.DRAW) "● 绘制" else "绘制") }
            Button(onClick = { onEvent(AirRibbonEvent.SelectMode(AppMode.EDIT)) }, enabled = state.strokes.isNotEmpty()) { Text(if (state.mode == AppMode.EDIT) "■ 编辑" else "编辑作品") }
            Button(onClick = { onEvent(AirRibbonEvent.SelectMode(AppMode.PHOTO)) }, enabled = state.strokes.isNotEmpty()) { Text("摄影") }
        }
        if (state.mode == AppMode.DRAW) {
            PanelLabel("笔刷")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BrushType.entries.forEach { brush ->
                    Button(onClick = { onEvent(AirRibbonEvent.SelectBrush(brush)) }) { Text(brush.label(state.brush.type == brush)) }
                }
            }
            PanelLabel("三色板")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaletteColor.entries.forEach { color ->
                    Button(onClick = { onEvent(AirRibbonEvent.SelectColor(color)) }) { Text(color.label(state.brush.color == color)) }
                }
            }
            PanelLabel("粗细")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StrokeWidth.entries.forEach { width ->
                    Button(onClick = { onEvent(AirRibbonEvent.SelectWidth(width)) }) { Text(width.label(state.brush.width == width)) }
                }
            }
        } else {
            Text(
                "扳机抓住作品整体移动；摇杆旋转或缩放。旋转和缩放始终以作品中心为轴。",
                style = PicoTheme.typography.bodyLarge,
                color = PicoTheme.colorScheme.labelPrimaryLight,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onEvent(AirRibbonEvent.ScaleGroup(0.9f)) }) { Text("缩小") }
                Button(onClick = { onEvent(AirRibbonEvent.ScaleGroup(1.1f)) }) { Text("放大") }
                Button(onClick = { onEvent(AirRibbonEvent.RotateGroup(-15f)) }) { Text("左转") }
                Button(onClick = { onEvent(AirRibbonEvent.RotateGroup(15f)) }) { Text("右转") }
                Button(onClick = { onEvent(AirRibbonEvent.ResetGroup) }) { Text("复位") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(AirRibbonEvent.Undo) }, enabled = state.undoDepth > 0) { Text("撤销 ${state.undoDepth}/10") }
            Button(onClick = { onEvent(AirRibbonEvent.RequestClear) }, enabled = state.strokes.isNotEmpty()) { Text("清空…") }
            Button(onClick = onTutorial) { Text("教程") }
        }
        Text(
            "${state.strokes.size} 笔 · ${if (state.inputSource == InputSource.HAND) "手势" else "手柄"}",
            style = PicoTheme.typography.bodyLarge,
            color = PicoTheme.colorScheme.labelPrimaryLight,
        )
    }
}

@Composable
fun TutorialPanel(page: Int, onPrevious: () -> Unit, onNext: () -> Unit, onSkip: () -> Unit) {
    val pages = listOf(
        "手势捏合绘制" to "先张开手。拇指与食指捏合后移动约 3 cm 开始绘制；保持捏合移动，松开结束一笔。",
        "手柄扳机绘制" to "用左或右手柄射线确定位置。按住扳机并移动约 3 cm 开始绘制；松开扳机结束。",
        "绘制与编辑" to "绘制模式用于创作。切换到作品编辑模式后，可移动、缩放和围绕作品中心旋转，不会误画。",
        "撤销与导出" to "可撤销最近十笔。清空需要再次确认；进入摄影模式后可导出 PNG 图片。",
    )
    val current = pages[page.coerceIn(pages.indices)]
    Column(
        Modifier.width(560.dp).clip(RoundedCornerShape(24.dp))
            .background(PicoTheme.colorScheme.fillPrimary).padding(26.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text("新手教程 · ${page + 1}/${pages.size}", style = PicoTheme.typography.titleMedium, color = PicoTheme.colorScheme.interaction)
        Text(current.first, style = PicoTheme.typography.titleLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
        Text(current.second, style = PicoTheme.typography.bodyLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
        Text("教程期间绘制与作品操作已暂停。", style = PicoTheme.typography.bodyLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onPrevious, enabled = page > 0) { Text("上一步") }
            Button(onClick = onNext) { Text(if (page == pages.lastIndex) "开始体验" else "下一步") }
            Button(onClick = onSkip) { Text("跳过") }
        }
    }
}

@Composable
fun OpenToolbarPanel(onOpen: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(18.dp))
            .background(PicoTheme.colorScheme.fillPrimary)
            .padding(10.dp),
    ) {
        Button(onClick = onOpen) { Text("打开工具栏") }
    }
}

@Composable
fun SafetyStatus(state: AirRibbonUiState) {
    Row(
        Modifier.width(520.dp).clip(RoundedCornerShape(20.dp))
            .background(PicoTheme.colorScheme.fillPrimary).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(if (state.saveSucceeded) "●" else "▲", color = if (state.saveSucceeded) PicoTheme.colorScheme.interaction else PicoTheme.colorScheme.error)
        Text(state.statusMessage, style = PicoTheme.typography.bodyLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
    }
}

@Composable
fun PhotoExportPanel(state: AirRibbonUiState, onEvent: (AirRibbonEvent) -> Unit) {
    Column(
        Modifier.width(480.dp).clip(RoundedCornerShape(24.dp))
            .background(PicoTheme.colorScheme.fillPrimary).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("作品摄影态", style = PicoTheme.typography.titleLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
        Text("工具已隐藏。当前导出会把丝带投影为高分辨率 PNG，并保留深度层次。", style = PicoTheme.typography.bodyLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(AirRibbonEvent.SelectMode(AppMode.DRAW)) }) { Text("返回绘制") }
            Button(onClick = { onEvent(AirRibbonEvent.SelectMode(AppMode.EDIT)) }) { Text("返回编辑") }
            Button(onClick = { onEvent(AirRibbonEvent.RequestExport) }, enabled = !state.exportInProgress) {
                Text(if (state.exportInProgress) "正在导出…" else "导出图片")
            }
        }
        state.lastExportUri?.let { Text("已导出：$it", style = PicoTheme.typography.bodyLarge, color = PicoTheme.colorScheme.labelPrimaryLight) }
    }
}

@Composable
fun ClearConfirmPanel(state: AirRibbonUiState, onEvent: (AirRibbonEvent) -> Unit) {
    Column(
        Modifier.width(440.dp).clip(RoundedCornerShape(24.dp))
            .background(PicoTheme.colorScheme.fillPrimary).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("确认清空？", style = PicoTheme.typography.titleLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
        Text("将删除当前 ${state.strokes.size} 笔，且不能通过撤销恢复。", style = PicoTheme.typography.bodyLarge, color = PicoTheme.colorScheme.labelPrimaryLight)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onEvent(AirRibbonEvent.CancelClear) }) { Text("取消") }
            Button(onClick = { onEvent(AirRibbonEvent.ConfirmClear) }) { Text("确认清空") }
        }
    }
}

@Composable
private fun PanelLabel(label: String) {
    Text(label, style = PicoTheme.typography.titleMedium, color = PicoTheme.colorScheme.labelPrimaryLight)
}

private fun BrushType.label(selected: Boolean) = (if (selected) "● " else "") + when (this) {
    BrushType.NEON_RIBBON -> "霓虹"
    BrushType.RAINBOW_RIBBON -> "彩虹"
    BrushType.FOAM -> "泡沫"
    BrushType.PAPER_TAPE -> "纸带"
}
private fun PaletteColor.label(selected: Boolean) = (if (selected) "● " else "") + when (this) {
    PaletteColor.CYAN -> "青"
    PaletteColor.CORAL -> "珊瑚"
    PaletteColor.LIME -> "青柠"
}
private fun StrokeWidth.label(selected: Boolean) = (if (selected) "● " else "") + when (this) {
    StrokeWidth.THIN -> "细"
    StrokeWidth.MEDIUM -> "中"
    StrokeWidth.THICK -> "粗"
}
