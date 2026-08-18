package com.pico.swan.airribbon.ui.airribbon.components

import com.pico.spatial.core.container.SpatialViewContent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import com.pico.swan.airribbon.domain.model.ArtworkGroupTransform
import com.pico.swan.airribbon.domain.model.Point3
import com.pico.swan.airribbon.domain.model.Stroke

class TrailSceneRenderer {
    private val sceneRoot = Entity().apply { setName("AirRibbonSceneRoot") }
    private val artworkPivot = Entity().apply { setName("AirRibbonArtworkPivot") }
    val artworkGroup = Entity().apply { setName("AirRibbonArtworkGroup") }
    private val trails = linkedMapOf<String, HandTrailMesh>()
    private var attached = false
    private var currentTransform = ArtworkGroupTransform()
    private var currentCenter = Point3(0f, 0f, 0f)

    fun sync(content: SpatialViewContent, strokes: List<Stroke>, transform: ArtworkGroupTransform) {
        if (!attached) {
            sceneRoot.addChild(artworkPivot)
            artworkPivot.addChild(artworkGroup)
            content.addEntity(sceneRoot)
            attached = true
        }
        val renderable = strokes.filter { it.points.size >= 2 }
        val liveIds = renderable.mapTo(mutableSetOf()) { it.id }
        trails.keys.filterNot(liveIds::contains).toList().forEach { id -> trails.remove(id)?.destroy() }
        renderable.forEach { stroke ->
            val trail = trails[stroke.id] ?: HandTrailMesh.create(stroke).also {
                trails[stroke.id] = it
                content.addEntity(it.entity)
                artworkGroup.addChild(it.entity)
            }
            trail.update(stroke)
        }
        val center = transform.pivot ?: ArtworkPivot.centerOf(strokes)
        currentTransform = transform
        currentCenter = center
        val placement = ArtworkPivot.placement(transform.position, center)
        artworkPivot.components[TransformComponent::class.java]?.apply {
            setPosition(placement.pivotPosition.toVector3())
            setEulerAngles(EulerAngles(transform.rotationDegrees.x, transform.rotationDegrees.y, transform.rotationDegrees.z))
            setScaleVector(Vector3(transform.uniformScale, transform.uniformScale, transform.uniformScale))
        }
        artworkGroup.components[TransformComponent::class.java]?.apply {
            setPosition(placement.contentOffset.toVector3())
            setEulerAngles(EulerAngles(0f, 0f, 0f))
            setScaleVector(Vector3(1f, 1f, 1f))
        }
    }

    /** Tracking providers publish Stage-global points; trail meshes store artwork-group-local points. */
    fun worldToArtworkPoint(position: Vector3): Point3? {
        if (!attached) return null
        val scenePoint = runCatching { sceneRoot.convertPositionFrom(position, null).toPoint3() }.getOrNull()
            ?: return null
        return ArtworkCoordinateMapper.sceneToArtwork(scenePoint, currentTransform, currentCenter)
    }

    /** Group translation is expressed in the stable SpatialView root coordinate system. */
    fun worldToScenePoint(position: Vector3): Point3? =
        if (attached) runCatching { sceneRoot.convertPositionFrom(position, null).toPoint3() }.getOrNull() else null

    fun destroy() {
        trails.values.forEach(HandTrailMesh::destroy)
        trails.clear()
        sceneRoot.destroy()
    }
}

private fun Vector3.toPoint3() = Point3(x, y, z)
private fun Point3.toVector3() = Vector3(x, y, z)
