package com.pico.swan.airribbon.domain.model

import kotlin.math.sqrt

const val MAX_STROKE_POINTS = 512
const val DOWNSAMPLE_TARGET_POINTS = 384
const val UNDO_LIMIT = 10

data class Point3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Point3) = Point3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Point3) = Point3(x - other.x, y - other.y, z - other.z)
    operator fun times(scale: Float) = Point3(x * scale, y * scale, z * scale)
    fun length(): Float = sqrt(x * x + y * y + z * z)
    fun distanceTo(other: Point3): Float = (this - other).length()
    fun isFinite(): Boolean = x.isFinite() && y.isFinite() && z.isFinite()
}

enum class BrushType { NEON_RIBBON, RAINBOW_RIBBON, FOAM, PAPER_TAPE }
enum class PaletteColor { CYAN, CORAL, LIME }
enum class StrokeWidth(val metres: Float) { THIN(0.012f), MEDIUM(0.024f), THICK(0.042f) }
enum class StrokeClosedReason { RELEASED, TRACKING_LOST, MODE_SWITCH, SYSTEM_INTERRUPTION }
enum class AppMode { DRAW, EDIT, PHOTO, CLEAR_CONFIRM }
enum class InputSource { HAND, CONTROLLER }

data class BrushSpec(
    val type: BrushType = BrushType.NEON_RIBBON,
    val color: PaletteColor = PaletteColor.CYAN,
    val width: StrokeWidth = StrokeWidth.MEDIUM,
)

data class Stroke(
    val id: String,
    val points: List<Point3>,
    val brush: BrushSpec,
    val closedReason: StrokeClosedReason? = null,
    val createdAtMillis: Long,
) {
    val isFinished: Boolean get() = closedReason != null
}

data class ArtworkGroupTransform(
    val position: Point3 = Point3(0f, 0f, 0f),
    val rotationDegrees: Point3 = Point3(0f, 0f, 0f),
    val uniformScale: Float = 1f,
    /** Stable local-space pivot captured when artwork editing begins. */
    val pivot: Point3? = null,
)

const val MIN_ARTWORK_SCALE = 0.25f
const val MAX_ARTWORK_SCALE = 3f

data class ArtworkDocument(
    val schemaVersion: Int = 1,
    val strokes: List<Stroke> = emptyList(),
    val groupTransform: ArtworkGroupTransform = ArtworkGroupTransform(),
    val savedAtMillis: Long = 0L,
)

data class SaveResult(val success: Boolean, val message: String)
data class ExportResult(val success: Boolean, val uri: String? = null, val message: String)
