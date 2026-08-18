package com.pico.swan.airribbon.data.repository

import android.content.Context
import com.pico.swan.airribbon.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class LocalArtworkRepository(context: Context) : ArtworkRepository {
    private val directory = File(context.filesDir, "airribbon")
    private val target = File(directory, "artwork.json")
    private val temporary = File(directory, "artwork.json.tmp")

    override fun load(): ArtworkDocument? = runCatching {
        if (!target.exists()) return null
        decode(JSONObject(target.readText()))
    }.getOrNull()

    override fun save(document: ArtworkDocument): SaveResult = runCatching {
        directory.mkdirs()
        temporary.writeText(encode(document).toString())
        if (target.exists() && !target.delete()) error("旧文件无法替换")
        if (!temporary.renameTo(target)) error("原子替换失败")
        SaveResult(true, "已保存到本机")
    }.getOrElse { SaveResult(false, "保存失败，已保留上次成功版本") }

    override fun clear(): SaveResult = runCatching {
        if (temporary.exists()) temporary.delete()
        if (target.exists() && !target.delete()) error("无法清空本地文件")
        SaveResult(true, "本地作品已清空")
    }.getOrElse { SaveResult(false, "清空失败，作品仍保留") }

    private fun encode(document: ArtworkDocument) = JSONObject().apply {
        put("schemaVersion", document.schemaVersion)
        put("savedAtMillis", document.savedAtMillis)
        put("groupTransform", transformJson(document.groupTransform))
        put("strokes", JSONArray().apply { document.strokes.forEach { put(strokeJson(it)) } })
    }

    private fun strokeJson(stroke: Stroke) = JSONObject().apply {
        put("id", stroke.id)
        put("createdAtMillis", stroke.createdAtMillis)
        put("closedReason", stroke.closedReason?.name)
        put("brush", JSONObject().apply {
            put("type", stroke.brush.type.name)
            put("color", stroke.brush.color.name)
            put("width", stroke.brush.width.name)
        })
        put("points", JSONArray().apply { stroke.points.forEach { put(pointJson(it)) } })
    }

    private fun transformJson(value: ArtworkGroupTransform) = JSONObject().apply {
        put("position", pointJson(value.position))
        put("rotation", pointJson(value.rotationDegrees))
        put("scale", value.uniformScale.toDouble())
        value.pivot?.let { put("pivot", pointJson(it)) }
    }

    private fun pointJson(value: Point3) = JSONArray(listOf(value.x, value.y, value.z))

    private fun decode(root: JSONObject): ArtworkDocument {
        val strokesJson = root.optJSONArray("strokes") ?: JSONArray()
        return ArtworkDocument(
            schemaVersion = root.optInt("schemaVersion", 1),
            strokes = List(strokesJson.length()) { decodeStroke(strokesJson.getJSONObject(it)) },
            groupTransform = decodeTransform(root.optJSONObject("groupTransform") ?: JSONObject()),
            savedAtMillis = root.optLong("savedAtMillis", 0L),
        )
    }

    private fun decodeStroke(value: JSONObject): Stroke {
        val brush = value.getJSONObject("brush")
        val points = value.getJSONArray("points")
        return Stroke(
            id = value.getString("id"),
            points = List(points.length()) { point(points.getJSONArray(it)) },
            brush = BrushSpec(
                BrushType.valueOf(brush.getString("type")),
                PaletteColor.valueOf(brush.getString("color")),
                StrokeWidth.valueOf(brush.getString("width")),
            ),
            closedReason = value.optString("closedReason").takeIf { it.isNotBlank() }?.let(StrokeClosedReason::valueOf),
            createdAtMillis = value.optLong("createdAtMillis"),
        )
    }

    private fun decodeTransform(value: JSONObject) = ArtworkGroupTransform(
        position = point(value.optJSONArray("position") ?: pointJson(Point3(0f, 0f, 0f))),
        rotationDegrees = point(value.optJSONArray("rotation") ?: pointJson(Point3(0f, 0f, 0f))),
        uniformScale = value.optDouble("scale", 1.0).toFloat(),
        pivot = value.optJSONArray("pivot")?.let(::point),
    )

    private fun point(value: JSONArray) = Point3(
        value.optDouble(0).toFloat(), value.optDouble(1).toFloat(), value.optDouble(2).toFloat(),
    )
}
