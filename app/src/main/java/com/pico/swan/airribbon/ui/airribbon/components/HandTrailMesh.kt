package com.pico.swan.airribbon.ui.airribbon.components

import com.pico.spatial.core.ecs.BoundingBox
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.ModelComponent
import com.pico.spatial.core.ecs.resource.BlendingMode
import com.pico.spatial.core.ecs.resource.MaterialCullingMode
import com.pico.spatial.core.ecs.resource.Material
import com.pico.spatial.core.ecs.resource.MeshModel
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.PhysicallyBasedMaterial
import com.pico.spatial.core.ecs.resource.UnlitMaterial
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.Vector3
import com.pico.swan.airribbon.domain.model.*
import kotlin.math.max
import kotlin.math.sqrt

/** Bounded procedural meshes: paper is flat, neon is layered, foam uses round icosahedron bubbles. */
class HandTrailMesh private constructor(
    val entity: Entity,
    private val layers: List<MeshLayer>,
    initialStroke: Stroke,
) {
    private var fingerprint = initialStroke.meshFingerprint()

    fun update(stroke: Stroke) {
        val nextFingerprint = stroke.meshFingerprint()
        if (nextFingerprint == fingerprint) return
        geometries(stroke).zip(layers).forEach { (geometry, layer) ->
            layer.mesh.replaceWithMeshModel(geometry.model, geometry.bounds)
        }
        fingerprint = nextFingerprint
    }

    fun destroy() {
        entity.destroy()
        layers.forEach { it.close() }
    }

    companion object {
        fun create(stroke: Stroke): HandTrailMesh {
            val safeId = stroke.id.toEcsName()
            val root = Entity().apply { setName("Stroke_$safeId") }
            val layers = geometries(stroke).mapIndexed { index, geometry ->
                val mesh = MeshResource.createWithMeshModel(geometry.model, geometry.bounds, "Trail_${safeId}_$index")
                val material = material(stroke, index)
                val child = Entity().apply {
                    setName("Stroke_${safeId}_layer_$index")
                    components.set(ModelComponent(mesh, material))
                }
                root.addChild(child)
                MeshLayer(mesh, material)
            }
            return HandTrailMesh(root, layers, stroke)
        }

        private fun geometries(stroke: Stroke): List<Geometry> = when (stroke.brush.type) {
            BrushType.NEON_RIBBON -> listOf(3.8f, 2.2f, 0.52f).map { scale ->
                ribbonGeometry(stroke.points, stroke.brush.width.metres * scale)
            }
            BrushType.RAINBOW_RIBBON -> rainbowRanges().map { (from, to) ->
                stripeGeometry(stroke.points, stroke.brush.width.metres * 2.4f, from, to)
            }
            BrushType.FOAM -> listOf(bubbleGeometry(stroke.points, stroke.brush.width.metres * 1.9f))
            BrushType.PAPER_TAPE -> listOf(ribbonGeometry(stroke.points, stroke.brush.width.metres))
        }

        private fun material(stroke: Stroke, layer: Int): Material = when (stroke.brush.type) {
            BrushType.NEON_RIBBON -> UnlitMaterial.create(BlendingMode.ADD).apply {
                setBaseColor(stroke.brush.color.vividColor())
                setCullingMode(MaterialCullingMode.NONE)
                setDepthWrite(false)
                setOpacity(floatArrayOf(0.10f, 0.28f, 1f)[layer])
            }
            BrushType.RAINBOW_RIBBON -> UnlitMaterial.create(BlendingMode.OPAQUE).apply {
                setBaseColor(RAINBOW_COLORS[layer])
                setCullingMode(MaterialCullingMode.NONE)
            }
            BrushType.FOAM -> PhysicallyBasedMaterial.create(BlendingMode.OPAQUE).apply {
                setBaseColor(stroke.brush.color.foamColor())
                setRoughness(1f)
                setMetallic(0f)
                setCullingMode(MaterialCullingMode.NONE)
            }
            BrushType.PAPER_TAPE -> PhysicallyBasedMaterial.create(BlendingMode.FADE).apply {
                setBaseColor(stroke.brush.color.paperColor())
                setRoughness(0.88f)
                setMetallic(0f)
                setOpacity(0.82f)
                setDepthWrite(false)
                setCullingMode(MaterialCullingMode.NONE)
            }
        }

        private fun rainbowRanges(): List<Pair<Float, Float>> {
            var cursor = -0.5f
            return RAINBOW_WIDTHS.map { stripeWidth ->
                (cursor to cursor + stripeWidth).also { cursor += stripeWidth }
            }
        }

        private fun stripeGeometry(points: List<Point3>, width: Float, from: Float, to: Float): Geometry {
            val positions = ArrayList<Vector3>(points.size * 2)
            val normals = ArrayList<Vector3>(points.size * 2)
            val indices = ArrayList<Int>(max(0, points.size - 1) * 6)
            points.forEachIndexed { index, point ->
                val side = ribbonSide(points, index)
                positions += (point + side * (width * from)).toVector3()
                positions += (point + side * (width * to)).toVector3()
                normals += Vector3(0f, 1f, 0f)
                normals += Vector3(0f, 1f, 0f)
                if (index < points.lastIndex) {
                    val base = index * 2
                    indices += listOf(base, base + 2, base + 1, base + 1, base + 2, base + 3)
                }
            }
            return Geometry(MeshModel(positions, indices, normals), bounds(points, width))
        }

        private fun bubbleGeometry(points: List<Point3>, diameter: Float): Geometry {
            val centers = evenlyLimited(points, MAX_BUBBLES_PER_STROKE)
            val positions = ArrayList<Vector3>(centers.size * 12)
            val normals = ArrayList<Vector3>(centers.size * 12)
            val indices = ArrayList<Int>(centers.size * ICOSAHEDRON_FACES.size)
            centers.forEachIndexed { index, point ->
                val pulse = BUBBLE_SIZE_PATTERN[index % BUBBLE_SIZE_PATTERN.size]
                val radius = diameter * pulse / 2f
                val base = positions.size
                ICOSAHEDRON_VERTICES.forEach { direction ->
                    positions += (point + direction * radius).toVector3()
                    normals += direction.toVector3()
                }
                ICOSAHEDRON_FACES.forEach { indices += base + it }
            }
            return Geometry(MeshModel(positions, indices, normals), bounds(centers, diameter))
        }

        private fun evenlyLimited(points: List<Point3>, limit: Int): List<Point3> {
            if (points.size <= limit) return points
            return List(limit) { index -> points[index * points.lastIndex / (limit - 1)] }
        }

        private fun ribbonGeometry(points: List<Point3>, width: Float): Geometry {
            val positions = ArrayList<Vector3>(points.size * 2)
            val normals = ArrayList<Vector3>(points.size * 2)
            val indices = ArrayList<Int>(max(0, points.size - 1) * 6)
            points.forEachIndexed { index, point ->
                val offset = ribbonSide(points, index) * (width / 2f)
                positions += (point - offset).toVector3()
                positions += (point + offset).toVector3()
                normals += Vector3(0f, 1f, 0f)
                normals += Vector3(0f, 1f, 0f)
                if (index < points.lastIndex) {
                    val base = index * 2
                    indices += listOf(base, base + 2, base + 1, base + 1, base + 2, base + 3)
                }
            }
            return Geometry(MeshModel(positions, indices, normals), bounds(points, width))
        }

        private fun ribbonSide(points: List<Point3>, index: Int): Point3 {
            val tangent = (points[minOf(points.lastIndex, index + 1)] - points[max(0, index - 1)]).normalized()
            var side = tangent.cross(Point3(0f, 1f, 0f)).normalized()
            if (side.length() < 0.01f) side = tangent.cross(Point3(1f, 0f, 0f)).normalized()
            return side
        }

        private fun bounds(points: List<Point3>, padding: Float): BoundingBox {
            val minX = points.minOf { it.x } - padding; val maxX = points.maxOf { it.x } + padding
            val minY = points.minOf { it.y } - padding; val maxY = points.maxOf { it.y } + padding
            val minZ = points.minOf { it.z } - padding; val maxZ = points.maxOf { it.z } + padding
            return BoundingBox(
                Vector3((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f),
                Vector3((maxX - minX) / 2f, (maxY - minY) / 2f, (maxZ - minZ) / 2f),
            )
        }

        private const val MAX_BUBBLES_PER_STROKE = 128
    }
}

private data class Geometry(val model: MeshModel, val bounds: BoundingBox)
private data class MeshLayer(val mesh: MeshResource, val material: Material) {
    fun close() { mesh.close(); material.close() }
}
private val RAINBOW_WIDTHS = floatArrayOf(0.08f, 0.13f, 0.18f, 0.24f, 0.16f, 0.12f, 0.09f)
private val RAINBOW_COLORS = listOf(
    Color4(1f, 0.05f, 0.12f, 1f), Color4(1f, 0.38f, 0.02f, 1f), Color4(1f, 0.9f, 0.04f, 1f),
    Color4(0.24f, 1f, 0.10f, 1f), Color4(0.02f, 0.88f, 1f, 1f), Color4(0.18f, 0.28f, 1f, 1f),
    Color4(0.72f, 0.08f, 1f, 1f),
)
private val BUBBLE_SIZE_PATTERN = floatArrayOf(0.68f, 1.08f, 0.82f, 1.28f, 0.74f, 0.96f, 1.18f)
private val ICOSAHEDRON_VERTICES: List<Point3> = run {
    val golden = ((1f + sqrt(5f)) / 2f)
    listOf(
        Point3(-1f, golden, 0f), Point3(1f, golden, 0f), Point3(-1f, -golden, 0f), Point3(1f, -golden, 0f),
        Point3(0f, -1f, golden), Point3(0f, 1f, golden), Point3(0f, -1f, -golden), Point3(0f, 1f, -golden),
        Point3(golden, 0f, -1f), Point3(golden, 0f, 1f), Point3(-golden, 0f, -1f), Point3(-golden, 0f, 1f),
    ).map(Point3::normalized)
}
private val ICOSAHEDRON_FACES = intArrayOf(
    0, 11, 5, 0, 5, 1, 0, 1, 7, 0, 7, 10, 0, 10, 11,
    1, 5, 9, 5, 11, 4, 11, 10, 2, 10, 7, 6, 7, 1, 8,
    3, 9, 4, 3, 4, 2, 3, 2, 6, 3, 6, 8, 3, 8, 9,
    4, 9, 5, 2, 4, 11, 6, 2, 10, 8, 6, 7, 9, 8, 1,
)
private fun Point3.toVector3() = Vector3(x, y, z)
private fun Point3.normalized(): Point3 = length().takeIf { it > 0.00001f }?.let { this * (1f / it) } ?: Point3(0f, 0f, 0f)
private fun Point3.cross(other: Point3) = Point3(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x,
)
private fun PaletteColor.vividColor(): Color4 = when (this) {
        PaletteColor.CYAN -> Color4(0.12f, 0.94f, 1f, 1f)
        PaletteColor.CORAL -> Color4(1f, 0.28f, 0.36f, 1f)
        PaletteColor.LIME -> Color4(0.62f, 1f, 0.16f, 1f)
}
private fun PaletteColor.foamColor(): Color4 = when (this) {
            PaletteColor.CYAN -> Color4(0.62f, 0.94f, 1f, 1f)
            PaletteColor.CORAL -> Color4(1f, 0.68f, 0.70f, 1f)
            PaletteColor.LIME -> Color4(0.82f, 1f, 0.62f, 1f)
}
private fun PaletteColor.paperColor(): Color4 = when (this) {
            PaletteColor.CYAN -> Color4(0.36f, 0.82f, 0.88f, 1f)
            PaletteColor.CORAL -> Color4(0.92f, 0.42f, 0.36f, 1f)
            PaletteColor.LIME -> Color4(0.66f, 0.84f, 0.34f, 1f)
}
private fun Stroke.meshFingerprint(): String = "$id:${points.size}:${points.lastOrNull()}:${closedReason}"
private fun String.toEcsName(): String = replace(Regex("[^A-Za-z0-9_]"), "_").take(96).ifBlank { "unnamed" }
