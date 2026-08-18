package com.pico.swan.airribbon.ui.airribbon.components

import com.pico.swan.airribbon.domain.model.Point3
import com.pico.swan.airribbon.domain.model.Stroke
import com.pico.swan.airribbon.domain.model.ArtworkGroupTransform
import com.pico.swan.airribbon.domain.model.MIN_ARTWORK_SCALE
import kotlin.math.cos
import kotlin.math.sin

internal object ArtworkPivot {
    data class Placement(val pivotPosition: Point3, val contentOffset: Point3)

    fun centerOf(strokes: List<Stroke>): Point3 {
        val points = strokes.flatMap(Stroke::points)
        if (points.isEmpty()) return Point3(0f, 0f, 0f)
        return Point3(
            (points.minOf { it.x } + points.maxOf { it.x }) / 2f,
            (points.minOf { it.y } + points.maxOf { it.y }) / 2f,
            (points.minOf { it.z } + points.maxOf { it.z }) / 2f,
        )
    }

    fun placement(translation: Point3, center: Point3) = Placement(
        pivotPosition = translation + center,
        contentOffset = center * -1f,
    )
}

internal object ArtworkCoordinateMapper {
    fun sceneToArtwork(point: Point3, transform: ArtworkGroupTransform, center: Point3): Point3? {
        if (!point.isFinite() || !center.isFinite() || !transform.position.isFinite()) return null
        val scale = transform.uniformScale
        if (!scale.isFinite() || scale < MIN_ARTWORK_SCALE) return null
        val relative = point - transform.position - center
        val radians = Math.toRadians((-transform.rotationDegrees.y).toDouble()).toFloat()
        val cosine = cos(radians)
        val sine = sin(radians)
        val unrotated = Point3(
            relative.x * cosine + relative.z * sine,
            relative.y,
            -relative.x * sine + relative.z * cosine,
        )
        return (center + unrotated * (1f / scale)).takeIf(Point3::isFinite)
    }
}
