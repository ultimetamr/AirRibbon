# AirRibbon contributor guide

## Product boundary

AirRibbon is a Chinese PICO Spatial SDK Stage toy for quickly drawing spatial ribbon sculptures. It is not a precision modeller. The Android package is `com.pico.swan.airribbon`; the Stage launches in Mixed mode.
The launcher-facing Chinese app label is `空间丝带`.

## Architecture

- `platform/`: `SpatialApplication` and the thin `LaunchActivity`.
- `Main.kt`: Stage/PicoTheme entry wiring only.
- `domain/model/`: stroke, brush, mode, transform, save/export types.
- `domain/usecase/`: drawing state machine and adaptive point reduction.
- `data/repository/`: local JSON persistence of relative artwork data. Do not add spatial-anchor persistence claims.
- `data/export/`: still-image PNG export.
- `ui/airribbon/`: MVI state, ViewModel, tracking orchestration and SpatialUI attachments.
- `ui/airribbon/components/`: bounded procedural ECS trail meshes and panels.

## Interaction and safety invariants

- Pinch starts a stroke, tracked motion appends points, release finishes it.
- A pinch requires thumb/index tips within 1.8 cm and must move at least 3 cm before it arms drawing; stationary pinch is reserved for SpatialUI clicks. Release is recognized at 3 cm.
- Direct hand proximity and actual UI drag interactions finish drawing; do not infer system pointer hits from controller or finger-bone rays because those directions do not match the system cursor reliably.
- Hand drawing is disarmed after tracking acquisition or UI interaction until an open-hand/release state is observed.
- Any hand-tracking loss finishes the current stroke immediately. Never join across a tracking gap.
- Drawing and artwork editing are mutually exclusive modes.
- A stroke is capped at 512 points; overflow is adaptively reduced before sampling continues.
- Undo retains only the latest ten completed strokes.
- Clear requires explicit confirmation.
- Either controller trigger can ray-draw and drag the artwork in Edit; the stick rotates/scales, A/X rotates or exports in Photo, B/Y undoes or exits Photo, and standard controller rays operate all SpatialUI controls.
- Tracking poses are Stage-global and must be converted with ECS entity conversion helpers before drawing or editing. Stroke points are stored in `AirRibbonArtworkGroup` local space.
- Drawing converts only to the unscaled scene root, then applies a bounded inverse group transform. Non-finite or >10 m local sample jumps safely end the stroke.

## UI and rendering rules

- Use PICO SpatialUI components and `PicoTheme` roles. Do not introduce Material 2/3 UI.
- Keep Stage content free of a full-screen opaque background. Controls live in `AttachmentPanel`s.
- Place the control stack 0.9 m in front of the camera once, then keep it world-stable. The title bar is hand-draggable; hide leaves a compact reopen control.
- A four-step first-run tutorial pauses all drawing/edit input and hides other panels; only tutorial navigation remains interactive. Completion is stored locally, and the main dock can reopen it.
- Neon uses three same-color additive layers; rainbow uses seven unequal transverse stripes; paper uses a lit translucent PBR layer; foam is capped at 128 twelve-vertex matte PBR bubbles per stroke.
- Prefer unlit materials and avoid per-frame material/resource creation.

## Verification

Use Java 17 and run:

```powershell
$Env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-17.0.13'
./gradlew.bat testDebugUnitTest assembleDebug
./gradlew.bat connectedDebugAndroidTest
```

Then install and launch the APK with `pico-cli app install` and `pico-cli app launch`. Validate hand gestures on a physical PICO device; the managed emulator cannot provide real hand input.
