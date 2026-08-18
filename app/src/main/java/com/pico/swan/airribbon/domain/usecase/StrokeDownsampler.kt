package com.pico.swan.airribbon.domain.usecase

import com.pico.swan.airribbon.domain.model.DOWNSAMPLE_TARGET_POINTS
import com.pico.swan.airribbon.domain.model.MAX_STROKE_POINTS
import com.pico.swan.airribbon.domain.model.Point3
import kotlin.math.max

object StrokeDownsampler {
    private const val MIN_SAMPLE_DISTANCE_METRES = 0.006f

    fun append(points: List<Point3>, candidate: Point3): List<Point3> {
        if (points.lastOrNull()?.distanceTo(candidate)?.let { it < MIN_SAMPLE_DISTANCE_METRES } == true) return points
        val appended = points + candidate
        return if (appended.size <= MAX_STROKE_POINTS) appended else reduce(appended, DOWNSAMPLE_TARGET_POINTS)
    }

    fun reduce(points: List<Point3>, target: Int = DOWNSAMPLE_TARGET_POINTS): List<Point3> {
        if (points.size <= target) return points
        require(target >= 2)
        var low = 0f
        var high = boundsDiagonal(points).coerceAtLeast(0.001f)
        var best = points
        repeat(24) {
            val simplified = rdp(points, (low + high) / 2f)
            if (simplified.size > target) {
                low = (low + high) / 2f
            } else {
                best = simplified
                high = (low + high) / 2f
            }
        }
        return if (best.size <= target) best else evenDecimate(best, target)
    }

    private fun rdp(points: List<Point3>, epsilon: Float): List<Point3> {
        if (points.size <= 2) return points
        val keep = BooleanArray(points.size)
        keep[0] = true
        keep[points.lastIndex] = true
        val ranges = ArrayDeque<Pair<Int, Int>>()
        ranges.add(0 to points.lastIndex)
        while (ranges.isNotEmpty()) {
            val (start, end) = ranges.removeLast()
            var furthest = -1
            var distance = 0f
            for (index in start + 1 until end) {
                val current = pointSegmentDistance(points[index], points[start], points[end])
                if (current > distance) {
                    distance = current
                    furthest = index
                }
            }
            if (furthest >= 0 && distance > epsilon) {
                keep[furthest] = true
                ranges.add(start to furthest)
                ranges.add(furthest to end)
            }
        }
        return points.filterIndexed { index, _ -> keep[index] }
    }

    private fun pointSegmentDistance(point: Point3, start: Point3, end: Point3): Float {
        val segment = end - start
        val lengthSquared = segment.x * segment.x + segment.y * segment.y + segment.z * segment.z
        if (lengthSquared <= 0.0000001f) return point.distanceTo(start)
        val relative = point - start
        val t = ((relative.x * segment.x + relative.y * segment.y + relative.z * segment.z) / lengthSquared).coerceIn(0f, 1f)
        return point.distanceTo(start + segment * t)
    }

    private fun boundsDiagonal(points: List<Point3>): Float {
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
        points.forEach {
            minX = minOf(minX, it.x); minY = minOf(minY, it.y); minZ = minOf(minZ, it.z)
            maxX = maxOf(maxX, it.x); maxY = maxOf(maxY, it.y); maxZ = maxOf(maxZ, it.z)
        }
        return Point3(maxX - minX, maxY - minY, maxZ - minZ).length()
    }

    private fun evenDecimate(points: List<Point3>, target: Int): List<Point3> = List(target) { index ->
        val source = ((points.lastIndex.toDouble() * index) / max(1, target - 1)).toInt()
        points[source]
    }
}
