package com.pico.swan.airribbon.domain.usecase

import com.pico.swan.airribbon.data.repository.ArtworkRepository
import com.pico.swan.airribbon.domain.model.*
import java.util.UUID

data class SessionSnapshot(
    val mode: AppMode,
    val returnMode: AppMode,
    val brush: BrushSpec,
    val strokes: List<Stroke>,
    val currentStroke: Stroke?,
    val groupTransform: ArtworkGroupTransform,
    val inputSource: InputSource,
    val undoDepth: Int,
    val statusMessage: String,
    val saveSucceeded: Boolean,
)

class AirRibbonUseCase(
    private val repository: ArtworkRepository,
    private val clock: () -> Long = System::currentTimeMillis,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    private val strokes = mutableListOf<Stroke>()
    private val undoIds = ArrayDeque<String>()
    private var currentStroke: Stroke? = null
    private var transform = ArtworkGroupTransform()
    private var mode = AppMode.DRAW
    private var returnMode = AppMode.DRAW
    private var brush = BrushSpec()
    private var inputSource = InputSource.HAND
    private var status = "捏合开始绘制，松开结束一笔"
    private var saveSucceeded = true

    init {
        repository.load()?.let { document ->
            strokes += document.strokes.filter { it.isFinished }.map { it.copy(points = StrokeDownsampler.reduce(it.points, MAX_STROKE_POINTS)) }
            transform = document.groupTransform.copy(
                uniformScale = document.groupTransform.uniformScale.takeIf(Float::isFinite)
                    ?.coerceIn(MIN_ARTWORK_SCALE, MAX_ARTWORK_SCALE) ?: 1f,
            )
            strokes.takeLast(UNDO_LIMIT).forEach { undoIds.addLast(it.id) }
            status = if (strokes.isEmpty()) "开始画第一笔" else "已恢复 ${strokes.size} 笔相对布局"
        }
    }

    fun snapshot() = SessionSnapshot(
        mode, returnMode, brush, strokes.toList(), currentStroke, transform,
        inputSource, undoIds.size, status, saveSucceeded,
    )

    fun setBrush(value: BrushSpec) { brush = value }

    fun setMode(value: AppMode) {
        if (value == mode) return
        finishStroke(StrokeClosedReason.MODE_SWITCH)
        if (value == AppMode.CLEAR_CONFIRM) returnMode = mode else if (mode != AppMode.CLEAR_CONFIRM) returnMode = value
        if (value == AppMode.EDIT && transform.pivot == null) {
            transform = transform.copy(pivot = artworkCenter(strokes))
            persist()
        }
        mode = value
        status = when (value) {
            AppMode.DRAW -> "绘制模式：捏合开始，松开结束"
            AppMode.EDIT -> "作品编辑模式：抓住作品整体移动"
            AppMode.PHOTO -> "摄影模式：工具已隐藏，可导出图片"
            AppMode.CLEAR_CONFIRM -> "确认清空全部 ${strokes.size} 笔？"
        }
    }

    fun setInputSource(value: InputSource) {
        if (inputSource == value) return
        if (value == InputSource.CONTROLLER) finishStroke(StrokeClosedReason.TRACKING_LOST)
        inputSource = value
        status = if (value == InputSource.HAND) "手势已就绪" else "手柄模式：射线与扳机可绘制"
    }

    fun startStroke(point: Point3) {
        if (mode != AppMode.DRAW || currentStroke != null || !point.isFinite()) return
        currentStroke = Stroke(idFactory(), listOf(point), brush, createdAtMillis = clock())
        status = "绘制中"
    }

    fun appendPoint(point: Point3) {
        val active = currentStroke ?: return
        if (!point.isFinite() || active.points.last().distanceTo(point) > MAX_SAMPLE_JUMP_METRES) {
            finishStroke(StrokeClosedReason.TRACKING_LOST)
            return
        }
        currentStroke = active.copy(points = StrokeDownsampler.append(active.points, point))
    }

    fun finishStroke(reason: StrokeClosedReason) {
        val active = currentStroke ?: return
        currentStroke = null
        if (active.points.size < 2) {
            status = "这一笔太短，未保留"
            return
        }
        strokes += active.copy(points = StrokeDownsampler.reduce(active.points, MAX_STROKE_POINTS), closedReason = reason)
        undoIds.addLast(active.id)
        while (undoIds.size > UNDO_LIMIT) undoIds.removeFirst()
        status = if (reason == StrokeClosedReason.TRACKING_LOST) "手势丢失，已在最后有效点安全收笔" else "完成第 ${strokes.size} 笔"
        persist()
    }

    fun undo() {
        finishStroke(StrokeClosedReason.MODE_SWITCH)
        val id = undoIds.removeLastOrNull() ?: run { status = "没有可撤销的笔画"; return }
        strokes.removeAll { it.id == id }
        status = "已撤销最近一笔"
        persist()
    }

    fun confirmClear() {
        currentStroke = null
        strokes.clear()
        undoIds.clear()
        mode = returnMode.takeUnless { it == AppMode.CLEAR_CONFIRM } ?: AppMode.DRAW
        val result = repository.clear()
        saveSucceeded = result.success
        status = if (result.success) "作品已清空" else result.message
    }

    fun cancelClear() {
        mode = returnMode.takeUnless { it == AppMode.CLEAR_CONFIRM } ?: AppMode.DRAW
        status = "已取消清空"
    }

    fun translateGroup(delta: Point3) = updateTransform(transform.copy(position = transform.position + delta))

    fun scaleGroup(factor: Float) = updateTransform(
        transform.copy(
            uniformScale = (transform.uniformScale * factor).takeIf(Float::isFinite)
                ?.coerceIn(MIN_ARTWORK_SCALE, MAX_ARTWORK_SCALE)
                ?: transform.uniformScale.coerceIn(MIN_ARTWORK_SCALE, MAX_ARTWORK_SCALE),
        ),
    )

    fun rotateGroup(yDegrees: Float) = updateTransform(
        transform.copy(rotationDegrees = transform.rotationDegrees.copy(y = transform.rotationDegrees.y + yDegrees)),
    )

    fun resetGroup() = updateTransform(ArtworkGroupTransform())

    fun setStatus(message: String, success: Boolean = true) { status = message; saveSucceeded = success }

    private fun updateTransform(value: ArtworkGroupTransform) {
        if (mode != AppMode.EDIT) return
        transform = value
        status = "作品组：${(transform.uniformScale * 100).toInt()}%"
        persist()
    }

    private fun persist() {
        val result = repository.save(ArtworkDocument(strokes = strokes.toList(), groupTransform = transform, savedAtMillis = clock()))
        saveSucceeded = result.success
        if (!result.success) status = result.message
    }

    private fun artworkCenter(values: List<Stroke>): Point3 {
        val points = values.flatMap(Stroke::points)
        if (points.isEmpty()) return Point3(0f, 0f, 0f)
        return Point3(
            (points.minOf { it.x } + points.maxOf { it.x }) / 2f,
            (points.minOf { it.y } + points.maxOf { it.y }) / 2f,
            (points.minOf { it.z } + points.maxOf { it.z }) / 2f,
        )
    }

    private companion object {
        const val MAX_SAMPLE_JUMP_METRES = 10f
    }
}
