# Visual System Spec · AirRibbon

> Roles: `visual_designer`, `spatial_design_system_designer` | active revision: 4 | sources: interaction r5, PM r6, UXR r5 | CR-DS-2

## 1. Direct Description of Outputs

Stage 8 先比较三种独立空间视觉方向，选择后以结构化 design-effect review 作为 approved visual reference；组件与 token 在 Stage 11 补全。

## 2. Spatial Visual Direction Candidates

| Direction | Spatial Thesis | First View | Container / Depth | Hierarchy | Interaction Cues | Spatial Value | Dashboard Risk |
|---|---|---|---|---|---|---|---|
| V1 夜色光带工坊 | 作品是中央暗色空间里的发光丝带，工具只是一条低矮“材料码头” | 中央作品占 70%，左腕/胸前小托盘，状态环贴近主手 | Stage Mixed；作品 z 主体，托盘近前但不遮挡 | 作品 > 当前模式 > brush/color/width > history/export | 捏合点亮指尖环；模式用图形徽记 | 强调运动轨迹与深度 | Low；非卡片墙 |
| V2 白纸折带剧场 | 纸带与泡沫像立体纸艺，控制成为一块大白工作台 | 白色平面占中央，作品在其上方 | Stage+大平面 backing；深度浅 | 工作台控制 > 作品 | 控件清楚但空间作品被平面压住 | Medium | High，像设计工具/画板 |
| V3 彩色材料星群 | 三笔刷各有漂浮样品球，颜色/粗细围绕身体排列 | 多个环绕样品与作品 | 多个 world-space 控件环绕 | 材料样品与作品竞争 | 直接抓取样品 | High but noisy | Medium；伪空间与遮挡风险高 |

- **Selected / approved visual reference**：V1 `夜色光带工坊`。
- **Structured design-effect review**：PASS。空间构图有单一主角（作品）；域表达由丝带、泡沫、纸带的真实截面与材质承担；手势状态可见；Stage Mixed 的暗色实体 backing 不依赖 Window glass；小工具托盘避免 dashboard；实现可由低成本材质和基础几何完成。
- **Rejected**：V2 让平面工作台成为主角并趋近专业工具；V3 用漂浮控件制造空间感、遮挡且提高目光/手臂负担。
- **证据边界**：UXR C2/C3 visual 是 gap，本方向只从 AirRibbon 语义、风险与选定概念派生，不据竞品视觉作差异断言。
- **Preview render instruction**：深墨背景/透视网格仅作空间提示，中央用 SVG/CSS 伪 3D 霓虹丝带，底部/左侧一条窄材料码头；绘制态显示连续亮轨和手势状态，摄影态隐藏工具并出现构图框/导出结果。

## 3. Design Tokens

| Token | Value | Use |
|---|---|---|
| accent | #66F7FF | focus/active neon |
| surface | #101823 | solid Stage controls |
| surfaceRaised | #182434 | focus backing |
| brandPrimary | #7B6CFF | AirRibbon identity |
| coral | #FF6B7A | color choice |
| lime | #B9FF66 | color choice |
| textPrimary | #F7FAFF | primary copy |
| textSecondary | #AAB8C8 | secondary copy |

### 3.1 Typography

| role | family | size/line | weight |
|---|---|---|---|
| display | sans | 40/48sp | 700 |
| title | sans | 24/32sp | 650 |
| metric | mono | 20/28sp | 600 |
| body | sans | 16/24sp | 500 |
| caption | sans | 12/18sp | 500 |

### 3.2 colorSemantics

| key | color | shape | label | desc | aliases[] |
|---|---|---|---|---|---|
| ready | #66F7FF | circle | 可以绘制 | tracking/input ready | ready,就绪 |
| drawing | #B9FF66 | diamond | 正在绘制 | active stroke | drawing,绘制中 |
| safeStop | #FFD166 | dashed | 已安全收笔 | tracking/mode interruption | tracking_lost,安全收笔 |
| edit | #7B6CFF | square | 正在编辑作品 | group manipulation | edit,编辑 |
| error | #FF6B7A | triangle | 操作未完成 | save/export/permission error | error,failed,失败 |
| saved | #7DFFB2 | circle | 已保存到本机 | successful local write | saved,已保存 |

### 3.3 Materials

| name | desc | treatment | glassStyle | opacity |
|---|---|---|---|---|
| startWindow | readable Shared Space shell | glass | Thick | .94 |
| startCard | key body/form backing | opaque | none | .96 |
| stageDock | Stage does not rely on Window glass | matte | none | .94 |
| ribbonNeon | additive-looking unlit ribbon, capped brightness | matte | none | 1 |
| ribbonFoam | opaque soft tube | matte | none | 1 |
| ribbonPaper | double-sided matte strip | matte | none | 1 |

### 3.4 Scale

| scale | tiers | use |
|---|---|---|
| spacing | xs4/s8/m16/l24/xl32 | all gaps/padding |
| radius | s12/m20/l32 | chips/cards/window; window 32dp |
| iconSize | s20/m28/l36 | status/actions/mode |

## 4. Environment Adaptation

- Stage uses controllable dark matte dock/backplates; no glass outside WindowContainer. WC-START uses system Thick glass plus opaque key content backing.
- Vibrant: WC-START monochrome title/text `ultralight`; terminated before thumbnails/gradients; fallback opaque `startCard`. StageDock does not rely on Vibrant.
- Bright environment: dock opacity .98 and text stays #F7FAFF; dark environment: neon bloom capped, no large saturated surfaces.
- Minimum body 12sp (actual 16), targets ≥56dp; color always paired with shape+label.

## 5. Component Definition Spec

### 5.0 Window / Stage structures

**WC-START shell**：Planar 1280×760dp, min720×540, max1440×900, depth640dp; contentInset 32/32/32/32; TitleBar 96; no docked attachment.

```
┌──────────────── WC-START 1280×760 ────────────────┐
│ TitleBar 96: AirRibbon / input & save status       │
│ ┌──────────── StartPanel full content ──────────┐ │
│ ┊ Guide 7fr │ Recent summary 5fr               ┊ │
│ ┊           [进入创作空间 56h, owned here]      ┊ │
│ └───────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────┘
```

Large(max) / Regular(default) use StartPanel-internal 7:5 columns; Constrained(min) is one-column + StartPanel-owned sticky CTA. Region mapping: Guide→start-guide/persistence; Recent→restore-summary; CTA→enter-action. No SaveStatus component and no duplicate CTA.

**ST-AIR world structure**

```
                 [SafetyStatus near active hand]
          ┌──── ArtworkGroup / TrailCanvas ────┐
          │ central 1.4×1.2×1.0m comfort volume│
          └─────────────────────────────────────┘
 [MaterialDock .42×.52m]       [PhotoExport only photo]
```

World anchors/sizes are authoritative in interaction §14. Stage components use matte/opaque backing, never Window glass.

### Component: TrailCanvas

| Field | Content |
|---|---|
| derivedFromTasks | T2,T3 |
| derivedFromData | Stroke, StrokePoint, HandTrailMesh |
| Purpose | create and display safe bounded strokes |
| layoutRole | primary_hero |
| Priority | primary |
| runtimeRole | spatialTrailEditor |

**Anatomy · Layout (anatomy.layout)**
```
world anchor ArtworkGroup origin
┌ active HandTrailMesh (last valid point marker) ┐
│ completed stroke meshes, local coordinates     │
└ bounds only visible in edit mode ──────────────┘
```
World anchor `(0,1.25,-1.2m)` relative session origin; local range x±0.7/y±0.6/z±0.5m; orientation inherits ArtworkGroup quaternion. Active endpoint follows only a valid hand point.

**Anatomy · Sizing (sizing)**

| Tier | Size/budget | Notes |
|---|---|---|
| Regular | ≤10 rich strokes; each≤512 points | Stage default |
| Compact | same envelope; neon/paper 2 verts/sample; foam 6 radial | p95 CPU≥12ms warning; density changes, physical envelope stable |
| Constrained | same envelope; foam 4 radial; completed points adaptively reduced | any 5-frame budget breach; no safety/interaction loss |

**Anatomy · Internal metrics (metrics)**

| Metric | Value | Source |
|---|---|---|
| background | none | Stage world |
| radius | N/A geometry stroke | brush width |
| padding | N/A world | comfort volume |
| gap | sample spacing ≥6mm | interaction §12 |
| stroke | width thin8/medium16/thick28mm | BrushSpec |
| icon | endpoint 20mm ring | active marker |
| primary text | N/A | no text in trail |
| secondary text | N/A | SafetyStatus owns copy |
| hitTarget | group bounds expansion 30mm | direct grab |

**Render elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| trail-mesh | 空间丝带 | mesh | strokes[].mesh | artwork |
| trail-endpoint | 正在绘制 | ring | currentStroke.lastValidPoint | drawing semantic |
| trail-bounds | 作品边界 | bounds | artworkGroup.bounds | edit only |

**Data bindings**

| path | target/property | fallback | type |
|---|---|---|---|
| strokes[] | trail-mesh/geometry | empty guidance via SafetyStatus | display |
| currentStroke.lastValidPoint | endpoint/transform | hide when invalid, never extrapolate | semantic |
| currentStroke.closedReason | endpoint/state | safeStop label | semantic |
| artworkGroup.bounds | trail-bounds/geometry | hide in draw/empty | display |
| performance.frameBudgetState | mesh LOD + boundaryDisabled | constrained LOD, keep last valid | semantic |

**Variants**：neon=ribbon+emissive cap; foam=tube 6/4 sides; paper=wide double-sided strip.

**States**

| state | trigger | visual | size/motion | accessibility | precedence |
|---|---|---|---|---|---|
| empty | no strokes | endpoint guide only | none | text guide | below dock |
| drawing | pinch held+valid | bright endpoint+live mesh | incremental/no flicker | diamond+label | highest trail |
| buffering | resample work | last valid mesh frozen | no spatial jump | “正在整理轨迹” | below safeStop |
| safeStop | tracking lost | dashed endpoint then hide | 120ms | label+tone | above drawing |
| edit | mode edit | bounds visible, no endpoint | 180ms outline | square+label | bounds above meshes |
| boundaryDisabled | point cap/perf | geometry LOD | no pulse | “已优化轨迹” | below errors |
| error | mesh failure | keep last valid mesh | static | retry/undo | highest |

Stacking: error > safeStop > drawing > edit bounds > completed mesh.

### Component: MaterialDock

| Field | Content |
|---|---|
| derivedFromTasks | T1,T6,T4,T7 |
| derivedFromData | BrushSpec, mode, UndoStack |
| Purpose | two-step-or-less choices and explicit modes |
| layoutRole | supporting_control |
| Priority | primary |
| runtimeRole | modeAndBrushControl |

**Anatomy · Layout (anatomy.layout)**
```
┌ mode: 绘制 | 编辑 | 摄影 ┐
├ brush: 霓虹 | 泡沫 | 纸带 ┤
├ color: 青 | 珊瑚 | 青柠  ┤
├ width: 细 | 中 | 粗       ┤
└ undo  清空                ┘
```
Anchor `(-0.55,1.15,-0.75m)` (mirrored for left hand); yaw faces head, pitch 0–15°; local size .42×.52×.03m, reachable reposition range ±.08m. 5 rows, 3 columns; 8mm gaps.

**Anatomy · Sizing (sizing)**

| Tier | Size | Notes |
|---|---|---|
| Regular | .42×.52m | Stage default |
| Compact | .36×.44m | manual limited-reach variant; labels retained; angular target calibrated on device |
| Constrained | .32×.40m + one category expanded | controller/layout fallback; ≥56dp-equivalent targets; no global scale |

**Anatomy · Internal metrics (metrics)**

| Metric | Value | Source |
|---|---|---|
| background | customColor #101823 | stageDock matte; no glass |
| radius | m20dp equivalent | scale |
| padding | l24 | scale |
| gap | s8/m16 | scale |
| stroke | 2dp #66F7FF selected | token |
| icon | m28 | scale |
| primary text | body 16/24 | typography |
| secondary | caption12/18 | typography |
| hitTarget | 56×56dp equivalent | platform floor |

**Render elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| mode-options | 绘制 / 编辑 / 摄影 | segmented | mode | mutually exclusive |
| brush-options | 霓虹 / 泡沫 / 纸带 | segmented | brush.type | brush |
| color-options | 青 / 珊瑚 / 青柠 | swatch+text | brush.color | color dual label |
| width-options | 细 / 中 / 粗 | line+text | brush.width | width |
| undo-action | 撤销 | button | undo.depth | action |
| clear-action | 清空 | button | strokes.count | dangerous action |

**Data bindings**

| path | target | fallback | type |
|---|---|---|---|
| mode | mode-options.selected | default draw | semantic |
| brush.type | brush-options.selected | neon | display |
| brush.color | color-options.selected | cyan+“青” | semantic |
| brush.width | width-options.selected | medium | display |
| undo.depth | undo-action.enabled | disabled label | semantic |
| strokes.count | clear-action.enabled | disabled | semantic |

**Variants**：hand-left/right mirrors dock; controller adds ray focus; photo collapses/hides whole dock.

**States**

| state | trigger | visual | size/motion | accessibility | precedence |
|---|---|---|---|---|---|
| default | none | matte | none | labels | base |
| focused | gaze/ray | accent stroke | 120ms≤1.03 | outline+tone | above default |
| selected | pinch/trigger | filled chip + glyph | 100ms | text “已选” | above focus |
| disabled | unavailable | .5 opacity | none | reason tooltip | wins over focus |
| error | action fail | triangle+copy | static | retry | highest |
| constrained | perf/layout | one open category | 180ms | categories named | layout state |

Stacking: error > disabled > selected+focused > focused > default.

### Component: ArtworkManipulator

| Field | Content |
|---|---|
| derivedFromTasks | T4,T5 |
| derivedFromData | ArtworkGroupTransform, strokes |
| Purpose | move/uniform-scale/rotate whole sculpture only in edit |
| layoutRole | primary_explore |
| Priority | primary |
| runtimeRole | groupManipulator |

**Anatomy · Layout (anatomy.layout)**
```
┌ dashed world bounds ┐
│ all strokes as one group │
└ corner/axis gizmos intentionally absent ┘
```
Direct-grab bounds around union; local origin at group centroid, initial `(0,1.25,-1.2m)`, identity quaternion; translation clamp x±0.8/y .55–1.9/z -.45–-2.2m; bounds follow content +30mm.

**Anatomy · Sizing (sizing)**

| Tier | Size | Notes |
|---|---|---|
| Regular | content bounds +30mm | Stage default |
| Compact | bounds only, same world scale | controller/limited reach; no handles |
| Constrained | scale .2–5× + comfort translation clamp | boundary feedback; no gizmo |

**Anatomy · Internal metrics (metrics)**

| Metric | Value | Source |
|---|---|---|
| background | none | world |
| radius | N/A | 3D bounds |
| padding | 30mm | grab expansion |
| gap | N/A | single group |
| stroke | 3mm dashed #7B6CFF | edit semantic |
| icon | m28 center grab glyph | scale |
| primary text | body16 mode label | typography |
| secondary | caption12 transform hint | typography |
| hitTarget | bounds+30mm | direct grab |

**Render elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| group-bounds | 整个作品 | bounds | artwork.bounds | selection |
| grab-hint | 抓住整个作品 | text/glyph | editHint | instruction |
| transform-readout | 缩放 | text | group.scale | feedback |

**Data bindings**

| path | target | fallback | type |
|---|---|---|---|
| artwork.bounds | group-bounds.geometry | empty disables | display |
| group.transform | bounds.transform | identity | display |
| group.scale | transform-readout.text | 100% | display |
| editHint | grab-hint.text/visibility | “抓住整个作品” | display |

**Variants**：one-hand translate; two-hand rotate+uniform scale; controller grip/two-controller.

**States**

| state | trigger | visual | size/motion | accessibility | precedence |
|---|---|---|---|---|---|
| empty | no strokes | no bounds, guidance | none | text | base |
| focused | gaze/grab proximity | solid edge | 120ms | glyph | above empty |
| dragging | grab | accent bounds | continuous direct | haptic/controller | highest active |
| scaling | two-hand | percentage visible | direct/no inertia | numeric readout | highest active |
| boundaryDisabled | .2/5× | amber dashed | stop at bound | label | above active |
| error | invalid transform | revert last valid | static | “已恢复” | highest |

Stacking: error > boundaryDisabled > dragging/scaling > focused > empty.

### Component: SafetyStatus

| Field | Content |
|---|---|
| derivedFromTasks | T2,T3,T8,T9 |
| derivedFromData | tracking.status, save.status, input.source |
| Purpose | make trust and recovery visible |
| layoutRole | critical_primary |
| Priority | primary |
| runtimeRole | statusBadge |

**Anatomy · Layout (anatomy.layout)**
```
[shape icon] [human label]
              [optional recovery]
```
2-column ×2-row; anchor valid hand + `(0.12,0.08,0m)` or dock-top fallback; billboard to head; local metric range .14–.24m wide and .06–.08m high.

**Anatomy · Sizing (sizing)**

| Tier | Size | Notes |
|---|---|---|
| Regular | .24×.08m | full label |
| Compact | .18×.07m | hand unavailable/dock anchor; one line |
| Constrained | .14×.06m | controller/peripheral placement; essential label |

**Anatomy · Internal metrics (metrics)**

| Metric | Value | Source |
|---|---|---|
| background | customColor #182434 | solid contrast |
| radius | s12 | scale |
| padding | s8/m16 | scale |
| gap | s8 | scale |
| stroke | 2dp semantic | colorSemantics |
| icon | s20 | scale |
| primary text | body16 | typography |
| secondary | caption12 | typography |
| hitTarget | 56dp if retry | floor |

**Render elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| status-shape | 状态 | shape | status.semantic | noncolor |
| status-label | 可以绘制 | text | status.label | human semantic |
| status-recovery | 重试 | button | status.recovery | conditional action |

**Data bindings**

| path | target | fallback | type |
|---|---|---|---|
| tracking.status | shape+label | “手部不可用，已切换手柄” | semantic |
| save.status | label | “尚未保存” | semantic |
| export.status | label/retry | “未导出，可重试” | semantic |
| input.source | shape+label variant | “手势” or “手柄” | semantic |
| status.recovery | recovery visibility/action | hide when none | semantic |

**Variants**：ready=circle+“可以绘制”; drawing=diamond+live label; safeStop=dashed+last-valid copy; edit=square+no draw cue; saved=circle+local-only copy; error=triangle+retry; controller=square+“手柄模式” and anchor moves to dock.

**States**

| state | trigger | visual | size/motion | accessibility | precedence |
|---|---|---|---|---|---|
| ready | valid | cyan circle | static | label | base |
| drawing | pinch | lime diamond | no flicker | label | above ready |
| safeStop | loss | amber dashed | 120ms | “已安全收笔” | above drawing |
| error | save/export | coral triangle | static | retry+label | highest |
| controller | fallback | violet square | static | source label | below error |

Stacking: error > safeStop > controller > drawing > ready.

### Component: PhotoExport

| Field | Content |
|---|---|
| derivedFromTasks | T7 |
| derivedFromData | export.status, export.uri, artwork |
| Purpose | uncluttered composition and image export |
| layoutRole | primary_explore |
| Priority | primary |
| runtimeRole | captureControl |

**Anatomy · Layout (anatomy.layout)**
```
┌ photo frame guides ┐
│ artwork only       │
└ [导出图片] [返回] ┘
```
Head-relative frame centered at 1.5m apparent distance; orientation follows view without forced camera. Safe frame stays within 65°×40°; controls bottom-center local y=-.32m.

**Anatomy · Sizing (sizing)**

| Tier | Size | Notes |
|---|---|---|
| Regular | frame within 65°×40° | default |
| Compact | 50°×32° minimal corners | controller/limited FOV; reduced UI |
| Constrained | 42°×28°, buttons bottom ≥56dp equivalent | permission/error focus; no whole-scale |

**Anatomy · Internal metrics (metrics)**

| Metric | Value | Source |
|---|---|---|
| background | none | capture clean |
| radius | m20 buttons | scale |
| padding | m16 | scale |
| gap | m16 | scale |
| stroke | 2dp #F7FAFF frame | token |
| icon | m28 | scale |
| primary text | body16 | typography |
| secondary | caption12 | typography |
| hitTarget | 56dp | floor |

**Render elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| photo-frame | 构图框 | corners | camera.view | composition |
| export-action | 导出图片 | button | export.available | action |
| photo-back | 返回创作 | button | priorMode | stable exit |
| export-result | 已导出到图片 | toast/status | export.status | result |

**Data bindings**

| path | target | fallback | type |
|---|---|---|---|
| export.available | export-action.enabled | disabled+reason | semantic |
| export.status | export-result | “未导出，可重试” | semantic |
| export.uri | result detail | hide path, show “图片” | display |
| camera.view | photo-frame/transform | current head view, no synthetic move | display |
| priorMode | photo-back/action target | draw-ready | semantic |

**Variants**：normal=frame+export/back; permission-request=blocking allow/cancel; controller=ray-focused buttons; success=nonblocking result; error=retry visible and state retained.

**States**

| state | trigger | visual | size/motion | accessibility | precedence |
|---|---|---|---|---|---|
| framing | enter | tools hidden/frame | 240ms | label | base |
| exporting | action | progress label | static | busy text | above framing |
| success | write ok | saved circle+label | 160ms | text | above exporting |
| permissionDenied | denied | error triangle+retry | static | explicit | highest |
| error | capture/write fail | keep photo state | static | retry | highest |

Stacking: error/permission > exporting > success > framing.

### Component: StartPanel

| Field | Content |
|---|---|
| derivedFromTasks | T8,T9,T2 |
| derivedFromData | input.availability, save.summary, scope.copy |
| Purpose | informed Stage entry and restore choice |
| layoutRole | primary_hero |
| Priority | primary |
| runtimeRole | entryDecision |

**Anatomy · Layout (anatomy.layout)**
```
[AirRibbon title]
[捏合开始·松开结束] [relative-save notice]
[进入创作空间]
```
Owning WC default content 1216×600dp: internal 7:5 columns; title/guide/notice left, restore summary right, CTA owned once at internal bottom. Constrained 656×380dp is one-column scroll + sticky CTA.

**Anatomy · Sizing (sizing)**

| Tier | Size | Notes |
|---|---|---|
| Regular | 1216×600dp | WC default Regular, exact content area |
| Compact | 1376×740dp | WC Large/max; internal 7:5 with capped line width |
| Constrained | 656×380dp scroll + sticky CTA | WC min, exact content area |

**Anatomy · Internal metrics (metrics)**

| Metric | Value | Source |
|---|---|---|
| background | customColor #101823 | opaque key copy |
| radius | l32 | scale |
| padding | xl32 | scale |
| gap | l24 | scale |
| stroke | 1dp #AAB8C8 | token |
| icon | l36 | scale |
| primary text | title24/display40 | type |
| secondary | body16/caption12 | type |
| hitTarget | 56dp | floor |

**Render elements**

| id | label | type | bind | role |
|---|---|---|---|---|
| start-title | AirRibbon | heading | app.name | identity |
| start-guide | 捏合开始，松开结束 | text | guide.copy | instruction |
| persistence-note | 恢复相对布局，不恢复房间位置 | notice | scope.persistence | trust |
| enter-action | 进入创作空间 | button | stage.available | high-risk entry |
| restore-summary | 最近作品 | summary | save.summary | restore |

**Data bindings**

| path | target | fallback | type |
|---|---|---|---|
| stage.available | enter enabled | “当前无法进入创作空间” | semantic |
| input.availability | guide detail | “可使用手柄” | semantic |
| save.summary | restore-summary | “还没有本地作品” | display |
| scope.persistence | persistence-note | fixed scope copy | display |
| app.name | start-title.text | AirRibbon | display |
| guide.copy | start-guide.text | “捏合开始，松开结束” | display |

**Variants**：new=empty summary+enter; restore=recent summary+restore copy; controller-only=ray/trigger guide; permission-error=enter disabled+recovery; constrained=one column+sticky owned CTA.

**States**

| state | trigger | visual | size/motion | accessibility | precedence |
|---|---|---|---|---|---|
| default | available | CTA active | none | labels | base |
| focused | gaze | accent stroke | 120ms | outline | above base |
| loading | save read | skeleton summary | no shift | “正在读取” | above base |
| empty | no save | friendly copy | none | text | base |
| error | cannot enter/read | triangle+recovery | static | explicit | highest |
| constrained | min | one column | no global scale | sticky CTA | layout |

Stacking: error > loading > focused > default/empty.

### Component: ClearConfirmDialog

| Field | Content |
|---|---|
| derivedFromTasks | T6 |
| derivedFromData | strokes.count, clear.request |
| Purpose | block irreversible clear until explicit confirmation |
| layoutRole | critical_modal |
| Priority | primary |
| runtimeRole | destructiveConfirmation |

**Anatomy · Layout (anatomy.layout)**

```
anchor head-forward (0,1.35,-0.85m), billboard to head
┌ 确认清空作品？ ┐
│ 将删除 N 笔    │
│ [取消] [清空]  │
└────────────────┘
```

World panel .42×.24×.02m; buttons two columns, 16mm gap; blocks MaterialDock input beneath.

**Anatomy · Sizing (sizing)**

| Tier | Size | Notes |
|---|---|---|
| Regular | .42×.24m | Stage default |
| Compact | .36×.22m | shorter copy, both actions retained |
| Constrained | .32×.24m | stacked 56dp-equivalent actions |

**Anatomy · Internal metrics (metrics)**

| Metric | Value | Source |
|---|---|---|
| background | customColor #182434 | Stage opaque, no glass |
| radius | m20 | scale |
| padding | l24 | scale |
| gap | m16 | scale |
| stroke | 2dp #FF6B7A | error semantic |
| icon | m28 triangle | scale |
| primary text | title24 | typography |
| secondary | body16 | typography |
| hitTarget | ≥56dp equivalent | floor |

**Render elements renderSpec.elements[]**

| id | label | type | bind | role |
|---|---|---|---|---|
| clear-title | 确认清空作品？ | heading | clear.copy | risk |
| clear-count | 将删除 N 笔 | text | strokes.count | consequence |
| clear-cancel | 取消 | button | clear.cancel | stable return |
| clear-confirm | 清空 | button | clear.confirm | destructive action |

**Data bindings dataBindings[]**

| path | target | fallback | type |
|---|---|---|---|
| strokes.count | clear-count.text | “当前作品” | display |
| clear.request | dialog.visibility | hidden | semantic |
| clear.cancel | clear-cancel.action | close without mutation | semantic |
| clear.confirm | clear-confirm.action | no action until explicit trigger | semantic |
| clear.copy | clear-title.text | normal “确认清空作品？”; error “清空未完成” | display-only |

**Variants**：nonempty=shows count and both actions; empty=dialog never opens/clear disabled; controller=ray focus; constrained=buttons stack vertically.

**States**

| state | trigger | visual | size/motion | accessibility | precedence |
|---|---|---|---|---|---|
| hidden | no request | absent | none | N/A | below all |
| open | request | opaque modal/backdrop | 180ms fade | focus trapped | above all Stage UI |
| focusedCancel | gaze/ray cancel | cyan outline | 120ms | “取消” | above open |
| focusedConfirm | gaze/ray clear | coral triangle+outline | 120ms | risk label | above open |
| clearing | confirm | buttons disabled | static | “正在清空” | highest |
| error | atomic clear failure | retain work + retry | static | explicit copy | highest |

Stacking: error > clearing > focused action > open > hidden; system Back always maps to cancel.

### 5.1 Structure checklist

| Component | base | layout | sizing | metrics | render | bindings | variants | states/precedence | Verdict |
|---|---|---|---|---|---|---|---|---|---|
| TrailCanvas | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| MaterialDock | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| ArtworkManipulator | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| SafetyStatus | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| PhotoExport | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| StartPanel | yes | yes | yes | yes | yes | yes | yes | yes | pass |
| ClearConfirmDialog | yes | yes | yes | yes | yes | yes | yes | yes | pass |

### 5.2 Coverage reconciliation

**A Data → binding**

| Entity/variable | Timeliness | Binding | Method | Gap |
|---|---|---|---|---|
| Stroke/points/mesh/closedReason | frame/session | TrailCanvas bindings | geometry + semantic stop | none |
| BrushSpec | selection | MaterialDock | selected labels | none |
| mode | immediate | MaterialDock/SafetyStatus | segment+shape/label | none |
| UndoStack/stroke count | action | MaterialDock | enable/disable | none |
| ArtworkGroupTransform/bounds | manipulation | ArtworkManipulator | world transform/readout | none |
| tracking/input source | frame | SafetyStatus/StartPanel | semantic label+shape | none |
| SaveDocument/summary/status | debounce/reopen | StartPanel/SafetyStatus | trust copy | none |
| export status/uri | job | PhotoExport | human status, URI hidden | none |
| performance budget | frame | TrailCanvas boundaryDisabled | “已优化轨迹” | none |
| clear request/count | action | ClearConfirmDialog bindings | blocking consequence + actions | none |

**B Decision → interaction**

| Task/output | Type | Catching interaction | Gap |
|---|---|---|---|
| T1 BrushSpec | actionable | MaterialDock options pinch | none |
| T2 finalized Stroke | actionable | TrailCanvas pinch/release/safe stop | none |
| T3 sample/resample | system action | TrailCanvas buffering/boundaryDisabled | none |
| T4 mode | actionable | MaterialDock mode | none |
| T5 group transform | actionable | ArtworkManipulator grab/two-hand | none |
| T6 undo/clear | actionable | MaterialDock undo/clear-action + ClearConfirmDialog cancel/confirm | none |
| T7 export | actionable | PhotoExport export/back | none |
| T8 save/restore | system action + trust decision | automatic load to StartPanel.restore-summary; SafetyStatus reports write state; no manual restore action promised | none |
| T9 fallback | actionable | StartPanel/SafetyStatus + controller variants | none |

**C Primary substates**

| Component/subcomponent | substates | primitive | binding |
|---|---|---|---|
| TrailCanvas/mesh | empty,drawing,buffering,safeStop,edit,boundaryDisabled,error | mesh/ring/bounds | strokes/currentStroke |
| MaterialDock/options | default,focused,selected,disabled,error,constrained | chips/buttons | mode/BrushSpec/undo |
| ArtworkManipulator/bounds | empty,focused,dragging,scaling,boundaryDisabled,error | bounds/readout | group transform |
| SafetyStatus/badge | ready,drawing,safeStop,error,controller | shape/text/retry | tracking/save/export |
| PhotoExport/control | framing,exporting,success,permissionDenied,error | frame/button/status | export status |
| StartPanel/entry | default,focused,loading,empty,error,constrained | heading/CTA/notice | availability/save |
| ClearConfirmDialog/actions | hidden,open,focusedCancel,focusedConfirm,clearing,error | modal/buttons/count | clear.request/strokes.count/actions |

## 6. Material and Depth Semantics

- Near=important: active endpoint/status nearest; completed mesh subject; controls off-center; environment farthest.
- WC glass: startWindow Thick; StartPanel opaque (custom color, no glass stack). Stage components matte/opaque only.
- Neon brightness capped; foam/paper opaque; no alpha-sorted multilayer on each mesh.
- Shared/MR WC readability: Thick glass + opaque StartPanel. Stage Mixed: solid StageDock; artwork itself is non-text.
- Vibrant list: WC title/body ultralight, terminates at any thumbnail; fallback opaque. No Vibrant on gradients/images/Stage meshes.

## 7. Data Display and Semantic Contract

- Data states: loading/fresh/partial/permission_denied/error/offline applicable to local save/export; no real-time backend.
- Trust: show “已保存到本机” only after atomic write; stale last-good shown as “上次成功保存”; never imply room anchor.
- displayOnlyPaths: `app.name`, `guide.copy`, `scope.persistence`, `save.summary`, `group.scale`, `export.uri` (URI rendered as “图片”).
- semanticEnumPaths: tracking.status, save.status, export.status, mode → §3.2 aliases/labels.

| Rule | Input | Output | fallback | states |
|---|---|---|---|---|
| points | stroke.points.count | “N / 512 点” only debug/preview | hide in consumer UI | fresh/buffering |
| scale | group.scale | percentage rounded | 100% | fresh |
| save | save.status | human semantic label | 尚未保存 | loading/error |
| export | export.status | human label | 未导出，可重试 | permission/error |

## 8. PICO Platform Numeric Spec

- Window radius 32dp; minimum body 12dp (project actual ≥16); hit target ≥56dp; core FOV 65°×40°, secondary ≤85°×55°; Planar legal range 320×180–2700×1800dp and depth 640dp.

## 9. Asset Delivery

- No bitmap dependency required. Icons SVG/tintable 28dp grid: brush-neon/foam/paper, mode-draw/edit/photo, undo/clear/export/status shapes.
- 3D assets are generated meshes: ribbon 2 vertices/sample; foam 6→4 radial; per stroke ≤512 samples; indexed geometry. No textures >512px, no PBR multi-pass.
- Audio optional short one-shot start/end/safe-stop/export cues, no continuous loop. Motion values source interaction §13.

## 10. Minimum Completeness Gate

| Check | Evidence | Verdict |
|---|---|---|
| Visual direction | §2, 3 directions + design-effect pass | pass |
| Visual language | §3–§4 | pass |
| Window structure | §5.0 | pass |
| Component structure | seven independent 8-section blocks / 56 evidence units | pass |
| Coverage reconciliation | §5.1–§5.2 | pass |
| Semantics/trust | §6–§8 | pass |

| Field | Value |
|---|---|
| minimumCompletenessGate | pass |

## 11. Delivery

V1 remains sole approved visual reference; document is ready for design-system review.
