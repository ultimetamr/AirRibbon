package com.pico.swan.airribbon.ui.airribbon

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pico.spatial.core.ecs.AnchorComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.anchor.AnchorTarget
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.tracking.controller.ControllerAction
import com.pico.spatial.tracking.controller.ControllerPose
import com.pico.spatial.tracking.controller.ControllerTrackingData
import com.pico.spatial.tracking.controller.ControllerTrackingProvider
import com.pico.spatial.tracking.hand.HandJoint.Index
import com.pico.spatial.tracking.hand.HandTrackingData
import com.pico.spatial.tracking.hand.HandTrackingProvider
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.swan.airribbon.data.export.ArtworkPhotoExporter
import com.pico.swan.airribbon.domain.model.*
import com.pico.swan.airribbon.platform.CapturePreviewController
import com.pico.swan.airribbon.ui.airribbon.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AirRibbonScreen() {
    val context = LocalContext.current
    val viewModel: AirRibbonViewModel = viewModel(factory = AirRibbonViewModel.Factory(context))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val capturePreviewState by CapturePreviewController.state.collectAsStateWithLifecycle()
    val handProvider = remember { HandTrackingProvider() }
    val controllerProvider = remember { ControllerTrackingProvider() }
    val handData by handProvider.dataFlow.collectAsState(HandTrackingData(null, null, 0L))
    val controllerData by controllerProvider.dataFlow.collectAsState(ControllerTrackingData(null, null, 0L))
    val renderer = remember { TrailSceneRenderer() }
    val panelAnchor = remember { Entity().apply { setName("AirRibbonPanelAnchor") } }
    val exporter = remember(context) { ArtworkPhotoExporter(context) }
    val scope = rememberCoroutineScope()
    val tutorialPreferences = remember(context) { context.getSharedPreferences("airribbon_tutorial", 0) }
    var tutorialPage by remember { mutableIntStateOf(0) }
    var tutorialShowing by remember { mutableStateOf(!tutorialPreferences.getBoolean("completed", false)) }
    var handPinched by remember { mutableStateOf(false) }
    var editLastPoint by remember { mutableStateOf<Point3?>(null) }
    var controllerTrigger by remember { mutableStateOf(false) }
    var activeControllerHand by remember { mutableStateOf<ControllerHand?>(null) }
    var controllerEditLastPoint by remember { mutableStateOf<Point3?>(null) }
    var controllerPendingDrawStart by remember { mutableStateOf<Point3?>(null) }
    var toolbarHidden by remember { mutableStateOf(false) }
    var panelPosition by remember { mutableStateOf(Point3(0f, 0f, 0f)) }
    var panelRollDegrees by remember { mutableFloatStateOf(0f) }
    var uiBlockedUntilMillis by remember { mutableLongStateOf(0L) }
    var pendingDrawStart by remember { mutableStateOf<Point3?>(null) }
    var handUiBlockedUntilRelease by remember { mutableStateOf(false) }
    var handReadyForPinch by remember { mutableStateOf(false) }
    val latestState by rememberUpdatedState(state)
    val latestController by rememberUpdatedState(controllerData)
    val latestOnEvent by rememberUpdatedState(viewModel::onEvent)
    val latestTutorialShowing by rememberUpdatedState(tutorialShowing)

    fun closeTutorial() {
        tutorialPreferences.edit().putBoolean("completed", true).apply()
        tutorialShowing = false
        tutorialPage = 0
    }

    LaunchedEffect(handProvider, controllerProvider) {
        delay(1_500)
        val handResult = handProvider.start()
        val controllerResult = controllerProvider.start()
        Log.i(
            "AirRibbonTracking",
            "hand=$handResult/${handProvider.supportState}, controller=$controllerResult/${controllerProvider.supportState}",
        )
    }

    DisposableEffect(handProvider, controllerProvider) {
        onDispose {
            handProvider.stop()
            controllerProvider.stop()
            renderer.destroy()
            panelAnchor.destroy()
        }
    }

    LaunchedEffect(capturePreviewState) {
        when (capturePreviewState) {
            "DRAW" -> viewModel.onEvent(AirRibbonEvent.SelectMode(AppMode.DRAW))
            "PHOTO" -> viewModel.onEvent(AirRibbonEvent.SelectMode(AppMode.PHOTO))
        }
    }

    LaunchedEffect(handData, state.mode, tutorialShowing) {
        if (tutorialShowing) {
            if (state.isDrawing) viewModel.onEvent(AirRibbonEvent.FinishStroke())
            handPinched = false
            pendingDrawStart = null
            editLastPoint = null
            return@LaunchedEffect
        }
        val hand = handData.right
        if (hand == null) {
            if (handPinched && state.isDrawing) viewModel.onEvent(AirRibbonEvent.TrackingLost)
            handPinched = false
            handUiBlockedUntilRelease = false
            handReadyForPinch = false
            pendingDrawStart = null
            editLastPoint = null
            viewModel.onEvent(AirRibbonEvent.SetInputSource(InputSource.CONTROLLER))
            return@LaunchedEffect
        }
        viewModel.onEvent(AirRibbonEvent.SetInputSource(InputSource.HAND))
        val indexWorld = hand[Index.INDEX_TIP].position
        val thumbWorld = hand[Index.THUMB_TIP].position
        val index = renderer.worldToArtworkPoint(indexWorld)
        val editPoint = renderer.worldToScenePoint(indexWorld)
        val distance = indexWorld.toPoint3().distanceTo(thumbWorld.toPoint3())
        if (distance >= PINCH_RELEASE_METRES) handReadyForPinch = true
        val rawPinched = if (handPinched) distance < PINCH_RELEASE_METRES else distance < PINCH_START_METRES
        val nowPinched = rawPinched && (handPinched || handReadyForPinch)
        val overUi = panelAnchor.isPointNearPanel(indexWorld, panelPosition, toolbarHidden)
        if (!rawPinched) handUiBlockedUntilRelease = false
        if (overUi && nowPinched) handUiBlockedUntilRelease = true
        val uiInteracting = overUi || handUiBlockedUntilRelease ||
            SpatialInputPolicy.uiBlocksDrawing(System.currentTimeMillis(), uiBlockedUntilMillis)
        if (uiInteracting) {
            if (handPinched && state.isDrawing) viewModel.onEvent(AirRibbonEvent.FinishStroke())
            handPinched = false
            pendingDrawStart = null
            editLastPoint = null
            return@LaunchedEffect
        }
        if (state.mode != AppMode.DRAW) pendingDrawStart = null
        when (state.mode) {
            AppMode.DRAW -> when {
                nowPinched && !handPinched && index != null -> pendingDrawStart = index
                nowPinched && index != null && state.isDrawing -> viewModel.onEvent(AirRibbonEvent.AppendPoint(index))
                nowPinched && index != null -> pendingDrawStart?.let { start ->
                    if (SpatialInputPolicy.movedFarEnoughToDraw(start, index)) {
                        viewModel.onEvent(AirRibbonEvent.StartStroke(start))
                        viewModel.onEvent(AirRibbonEvent.AppendPoint(index))
                        pendingDrawStart = null
                    }
                }
                !nowPinched && handPinched -> {
                    pendingDrawStart = null
                    viewModel.onEvent(AirRibbonEvent.FinishStroke())
                }
            }
            AppMode.EDIT -> when {
                nowPinched && !handPinched -> editLastPoint = editPoint
                nowPinched && editPoint != null -> editLastPoint?.let { previous ->
                    viewModel.onEvent(AirRibbonEvent.TranslateGroup(editPoint - previous))
                    editLastPoint = editPoint
                }
                else -> editLastPoint = null
            }
            else -> Unit
        }
        if (nowPinched && !handPinched) handReadyForPinch = false
        handPinched = nowPinched
    }

    val controllerListener = remember(controllerProvider) {
        var drawingHand: ControllerHand? = null
        var previousPrimary = false
        var previousSecondary = false
        var previousGrip = false
        var lastStickTransformMillis = 0L
        ControllerTrackingProvider.ControllerActionListener { actions ->
            if (latestTutorialShowing) {
                drawingHand = null
                scope.launch {
                    latestOnEvent(AirRibbonEvent.FinishStroke())
                    controllerPendingDrawStart = null
                    controllerEditLastPoint = null
                    controllerTrigger = false
                    activeControllerHand = null
                }
                return@ControllerActionListener
            }
            val selectedHand = SpatialInputPolicy.selectDrawingHand(
                leftTriggerPressed = actions.left.triggerPressed,
                rightTriggerPressed = actions.right.triggerPressed,
                leftPoseAvailable = latestController.left != null,
                rightPoseAvailable = latestController.right != null,
                previous = drawingHand,
            )
            if (drawingHand == null && selectedHand != null) {
                drawingHand = selectedHand
                scope.launch {
                    latestOnEvent(AirRibbonEvent.SetInputSource(InputSource.CONTROLLER))
                    activeControllerHand = selectedHand
                    val point = latestController.pose(selectedHand)?.rayPoint()?.let(renderer::worldToArtworkPoint)
                    val editPoint = latestController.pose(selectedHand)?.rayPoint()?.let(renderer::worldToScenePoint)
                    if (latestState.mode == AppMode.DRAW) controllerPendingDrawStart = point
                    if (latestState.mode == AppMode.EDIT) controllerEditLastPoint = editPoint
                    controllerTrigger = true
                }
            }
            val currentHand = drawingHand
            if (currentHand != null && !actions.action(currentHand).triggerPressed) {
                drawingHand = null
                scope.launch {
                    latestOnEvent(AirRibbonEvent.FinishStroke())
                    controllerPendingDrawStart = null
                    controllerEditLastPoint = null
                    controllerTrigger = false
                    activeControllerHand = null
                }
            }
            val actionHand = selectedHand ?: when {
                latestController.left != null -> ControllerHand.LEFT
                else -> ControllerHand.RIGHT
            }
            val action = actions.action(actionHand)
            val primary = action.primaryPressed(actionHand)
            val secondary = action.secondaryPressed(actionHand)
            if (primary && !previousPrimary) scope.launch {
                when (latestState.mode) {
                    AppMode.DRAW -> {
                        val next = BrushType.entries[(latestState.brush.type.ordinal + 1) % BrushType.entries.size]
                        latestOnEvent(AirRibbonEvent.SelectBrush(next))
                    }
                    AppMode.EDIT -> latestOnEvent(AirRibbonEvent.RotateGroup(15f))
                    AppMode.PHOTO -> latestOnEvent(AirRibbonEvent.RequestExport)
                    AppMode.CLEAR_CONFIRM -> Unit
                }
            }
            if (secondary && !previousSecondary) scope.launch {
                if (latestState.mode == AppMode.PHOTO) latestOnEvent(AirRibbonEvent.SelectMode(AppMode.EDIT))
                else latestOnEvent(AirRibbonEvent.Undo)
            }
            if (action.gripPressed && !previousGrip) scope.launch {
                latestOnEvent(AirRibbonEvent.SelectMode(if (latestState.mode == AppMode.EDIT) AppMode.DRAW else AppMode.EDIT))
            }
            val stick = action.thumbstickValue
            val now = System.currentTimeMillis()
            if (latestState.mode == AppMode.EDIT && now - lastStickTransformMillis >= 70L) {
                if (kotlin.math.abs(stick.x) > 0.25f) scope.launch {
                    latestOnEvent(AirRibbonEvent.RotateGroup(stick.x * 4f))
                }
                if (kotlin.math.abs(stick.y) > 0.25f) scope.launch {
                    latestOnEvent(AirRibbonEvent.ScaleGroup(1f + stick.y * 0.025f))
                }
                lastStickTransformMillis = now
            }
            previousPrimary = primary
            previousSecondary = secondary
            previousGrip = action.gripPressed
        }
    }

    DisposableEffect(controllerProvider, controllerListener) {
        controllerProvider.addControllerActionListener(controllerListener)
        onDispose { controllerProvider.removeControllerActionListener(controllerListener) }
    }

    LaunchedEffect(controllerData, controllerTrigger, activeControllerHand) {
        if (controllerTrigger) {
            activeControllerHand?.let(controllerData::pose)?.rayPoint()?.let { worldPoint ->
                when (state.mode) {
                    AppMode.DRAW -> renderer.worldToArtworkPoint(worldPoint)?.let { point ->
                        when {
                            state.isDrawing -> viewModel.onEvent(AirRibbonEvent.AppendPoint(point))
                            controllerPendingDrawStart?.let { start ->
                                SpatialInputPolicy.movedFarEnoughToDraw(start, point)
                            } == true -> {
                                viewModel.onEvent(AirRibbonEvent.StartStroke(controllerPendingDrawStart!!))
                                viewModel.onEvent(AirRibbonEvent.AppendPoint(point))
                                controllerPendingDrawStart = null
                            }
                        }
                    }
                    AppMode.EDIT -> controllerEditLastPoint?.let { previous ->
                        renderer.worldToScenePoint(worldPoint)?.let { point ->
                            viewModel.onEvent(AirRibbonEvent.TranslateGroup(point - previous))
                            controllerEditLastPoint = point
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    LaunchedEffect(state.exportRequestId) {
        if (state.exportRequestId == 0L) return@LaunchedEffect
        val result = withContext(Dispatchers.IO) { exporter.export(state.strokes, state.groupTransform) }
        viewModel.onEvent(AirRibbonEvent.ExportFinished(result))
    }

    SpatialView(
        modifier = Modifier.size(1.dp),
        attachments = {
            AttachmentPanel(id = DOCK_ID) {
                if (state.mode == AppMode.DRAW || state.mode == AppMode.EDIT) MaterialDock(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onDrag = { deltaPixels ->
                        uiBlockedUntilMillis = System.currentTimeMillis() + UI_DRAG_BLOCK_MILLIS
                        viewModel.onEvent(AirRibbonEvent.FinishStroke())
                        panelPosition = SpatialInputPolicy.constrainPanelDrag(panelPosition, deltaPixels)
                    },
                    onRotate = { delta ->
                        uiBlockedUntilMillis = System.currentTimeMillis() + UI_DRAG_BLOCK_MILLIS
                        panelRollDegrees = (panelRollDegrees + delta).coerceIn(-30f, 30f)
                    },
                    onHide = { toolbarHidden = true },
                    onTutorial = {
                        viewModel.onEvent(AirRibbonEvent.FinishStroke())
                        tutorialPage = 0
                        tutorialShowing = true
                    },
                )
                else Spacer(Modifier.size(1.dp))
            }
            AttachmentPanel(id = OPEN_ID) { OpenToolbarPanel { toolbarHidden = false } }
            AttachmentPanel(id = STATUS_ID) { SafetyStatus(state) }
            AttachmentPanel(id = PHOTO_ID) {
                if (state.mode == AppMode.PHOTO) PhotoExportPanel(state, viewModel::onEvent)
                else Spacer(Modifier.size(1.dp))
            }
            AttachmentPanel(id = CLEAR_ID) {
                if (state.mode == AppMode.CLEAR_CONFIRM) ClearConfirmPanel(state, viewModel::onEvent)
                else Spacer(Modifier.size(1.dp))
            }
            AttachmentPanel(id = TUTORIAL_ID) {
                if (tutorialShowing) TutorialPanel(
                    page = tutorialPage,
                    onPrevious = { tutorialPage = (tutorialPage - 1).coerceAtLeast(0) },
                    onNext = {
                        if (tutorialPage >= TUTORIAL_LAST_PAGE) closeTutorial()
                        else tutorialPage++
                    },
                    onSkip = ::closeTutorial,
                ) else Spacer(Modifier.size(1.dp))
            }
        },
        initial = { content, attachments ->
            panelAnchor.components.set(
                AnchorComponent(
                    AnchorTarget.createCameraTarget(),
                    AnchorComponent.TrackingMode.ONCE,
                ).apply {
                    positionOffset = SpatialInputPolicy.MAIN_PANEL_OFFSET.toVector3()
                },
            )
            content.addEntity(panelAnchor)
            listOf(
                DOCK_ID to Vector3.ZERO,
                STATUS_ID to Vector3(0f, 0.48f, -0.05f),
                PHOTO_ID to Vector3.ZERO,
                CLEAR_ID to Vector3(0f, 0.12f, -0.08f),
                OPEN_ID to Vector3(0f, 0f, 0f),
                TUTORIAL_ID to Vector3(0f, 0f, -0.04f),
            ).forEach { (id, position) ->
                attachments.entity(id)?.apply {
                    components[TransformComponent::class.java]?.setPosition(position)
                    panelAnchor.addChild(this)
                }
            }
        },
        update = { content, attachments ->
            renderer.sync(content, state.strokes + listOfNotNull(state.currentStroke), state.groupTransform)
            attachments.entity(DOCK_ID)?.apply {
                enabled = !tutorialShowing && !toolbarHidden && (state.mode == AppMode.DRAW || state.mode == AppMode.EDIT)
                components[TransformComponent::class.java]?.setPosition(panelPosition.toVector3())
                components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, 0f, panelRollDegrees))
            }
            attachments.entity(STATUS_ID)?.apply {
                enabled = !tutorialShowing && !toolbarHidden
                components[TransformComponent::class.java]?.setPosition(
                    (panelPosition + Point3(0f, 0.48f, -0.05f)).toVector3(),
                )
                components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, 0f, panelRollDegrees))
            }
            attachments.entity(OPEN_ID)?.apply {
                enabled = !tutorialShowing && toolbarHidden
                components[TransformComponent::class.java]?.setPosition(panelPosition.toVector3())
                components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, 0f, panelRollDegrees))
            }
            attachments.entity(PHOTO_ID)?.apply {
                enabled = !tutorialShowing && state.mode == AppMode.PHOTO
                components[TransformComponent::class.java]?.setPosition(panelPosition.toVector3())
                components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, 0f, panelRollDegrees))
            }
            attachments.entity(CLEAR_ID)?.apply {
                enabled = !tutorialShowing && state.mode == AppMode.CLEAR_CONFIRM
                components[TransformComponent::class.java]?.setPosition(
                    (panelPosition + Point3(0f, 0.12f, -0.08f)).toVector3(),
                )
                components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, 0f, panelRollDegrees))
            }
            attachments.entity(TUTORIAL_ID)?.apply {
                enabled = tutorialShowing
                components[TransformComponent::class.java]?.setPosition((panelPosition + Point3(0f, 0f, -0.04f)).toVector3())
                components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(0f, 0f, panelRollDegrees))
            }
        },
    )
}

private fun Vector3.toPoint3() = Point3(x, y, z)
private fun Point3.toVector3() = Vector3(x, y, z)
private fun ControllerPose.rayPoint(): Vector3 {
    val forward = rotation.rotateVector(Vector3.BACK)
    return position + forward * 0.65f
}

private fun ControllerTrackingData.pose(hand: ControllerHand): ControllerPose? =
    if (hand == ControllerHand.LEFT) left else right

private fun com.pico.spatial.tracking.controller.ControllerActionData.action(hand: ControllerHand): ControllerAction =
    if (hand == ControllerHand.LEFT) left else right

private fun ControllerAction.primaryPressed(hand: ControllerHand) =
    if (hand == ControllerHand.LEFT) xButtonPressed else aButtonPressed

private fun ControllerAction.secondaryPressed(hand: ControllerHand) =
    if (hand == ControllerHand.LEFT) yButtonPressed else bButtonPressed

private const val PINCH_START_METRES = 0.018f
private const val PINCH_RELEASE_METRES = 0.030f
private const val DOCK_ID = "material_dock"
private const val STATUS_ID = "safety_status"
private const val PHOTO_ID = "photo_export"
private const val CLEAR_ID = "clear_confirm"
private const val OPEN_ID = "open_toolbar"
private const val TUTORIAL_ID = "tutorial"
private const val TUTORIAL_LAST_PAGE = 3
private const val UI_DRAG_BLOCK_MILLIS = 450L

private fun Entity.isPointNearPanel(worldPoint: Vector3, panelPosition: Point3, compact: Boolean): Boolean {
    val local = convertPositionFrom(worldPoint, null).toPoint3() - panelPosition
    val halfWidth = if (compact) 0.22f else 0.48f
    val halfHeight = if (compact) 0.12f else 0.42f
    return kotlin.math.abs(local.x) <= halfWidth &&
        kotlin.math.abs(local.y) <= halfHeight &&
        kotlin.math.abs(local.z) <= 0.18f
}
