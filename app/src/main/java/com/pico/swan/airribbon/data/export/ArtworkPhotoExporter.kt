package com.pico.swan.airribbon.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.provider.MediaStore
import com.pico.swan.airribbon.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class ArtworkPhotoExporter(private val context: Context) {
    fun export(strokes: List<Stroke>, transform: ArtworkGroupTransform): ExportResult = runCatching {
        if (strokes.isEmpty()) return ExportResult(false, message = "没有可导出的作品")
        val bitmap = Bitmap.createBitmap(2048, 1536, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.rgb(7, 12, 20))
        val bounds = bounds(strokes.flatMap(Stroke::points))
        strokes.forEach { drawStroke(canvas, it, bounds, transform) }
        val filename = "AirRibbon_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AirRibbon")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("无法创建图片文件")
        context.contentResolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            ?: error("无法写入图片")
        values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, values, null, null)
        bitmap.recycle()
        ExportResult(true, uri.toString(), "图片已导出到 Pictures/AirRibbon")
    }.getOrElse { ExportResult(false, message = "导出失败，可重试") }

    private fun drawStroke(canvas: Canvas, stroke: Stroke, bounds: Bounds, transform: ArtworkGroupTransform) {
        val path = Path()
        stroke.points.forEachIndexed { index, point ->
            val projected = project(point, bounds, transform)
            if (index == 0) path.moveTo(projected.first, projected.second) else path.lineTo(projected.first, projected.second)
        }
        val base = stroke.brush.color.androidColor()
        val width = stroke.brush.width.metres * 900f * transform.uniformScale
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = base
            strokeWidth = width
        }
        when (stroke.brush.type) {
            BrushType.NEON_RIBBON -> {
                canvas.drawPath(path, Paint(paint).apply { alpha = 25; strokeWidth = width * 3.8f })
                canvas.drawPath(path, Paint(paint).apply { alpha = 72; strokeWidth = width * 2.2f })
                canvas.drawPath(path, paint)
                canvas.drawPath(path, Paint(paint).apply { color = Color.WHITE; strokeWidth = max(2f, width * 0.16f) })
            }
            BrushType.RAINBOW_RIBBON -> {
                val colors = intArrayOf(Color.RED, 0xffff6d00.toInt(), Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, 0xff9c27b0.toInt())
                val widths = floatArrayOf(0.08f, 0.13f, 0.18f, 0.24f, 0.16f, 0.12f, 0.09f)
                var remaining = widths.sum()
                widths.indices.forEach { index ->
                    canvas.drawPath(path, Paint(paint).apply { color = colors[index]; strokeWidth = width * 2.4f * remaining })
                    remaining -= widths[index]
                }
            }
            BrushType.FOAM -> {
                canvas.drawPath(path, Paint(paint).apply { color = Color.argb(100, 255, 255, 255); strokeWidth = width * 1.35f })
                canvas.drawPath(path, paint)
            }
            BrushType.PAPER_TAPE -> canvas.drawPath(path, Paint(paint).apply { strokeCap = Paint.Cap.SQUARE })
        }
    }

    private fun project(point: Point3, bounds: Bounds, transform: ArtworkGroupTransform): Pair<Float, Float> {
        val spanX = max(0.1f, bounds.maxX - bounds.minX)
        val spanY = max(0.1f, bounds.maxY - bounds.minY)
        val scale = minOf(1640f / spanX, 1120f / spanY) * transform.uniformScale
        val depthOffset = (point.z - bounds.minZ) * 90f
        return (1024f + (point.x - (bounds.minX + bounds.maxX) / 2f) * scale + depthOffset) to
            (768f - (point.y - (bounds.minY + bounds.maxY) / 2f) * scale - depthOffset * 0.35f)
    }

    private fun bounds(points: List<Point3>) = Bounds(
        points.minOf { it.x }, points.maxOf { it.x }, points.minOf { it.y }, points.maxOf { it.y }, points.minOf { it.z },
    )

    private data class Bounds(val minX: Float, val maxX: Float, val minY: Float, val maxY: Float, val minZ: Float)
}

private fun PaletteColor.androidColor(): Int = when (this) {
    PaletteColor.CYAN -> Color.rgb(37, 235, 255)
    PaletteColor.CORAL -> Color.rgb(255, 72, 91)
    PaletteColor.LIME -> Color.rgb(157, 255, 52)
}
