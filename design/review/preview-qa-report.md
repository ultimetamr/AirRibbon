# Preview / QA Test Report · AirRibbon

> generation source: interaction r5 + visual r4 + design-system-review r8 | preview revision: 4 | QA report revision: 5 | scope: `web_design_validation_only`

## 1. Direct Description of Outputs

单文件 Preview 验证状态、组件元素、绑定/回退、变体/组件状态、三档 reflow 与 Reduce Motion 的逻辑映射；不验证 PICO 运行时或设备性能。

## 2. Test Scope and Verdict

- Object: `design/preview.html` r4
- Source facts: interaction5 / visual4 / design-system-review8
- Overall: independent QA passed active preview4. Stage16 changes governance records only and does not change any preview implementation input fact, so preview4/QA5 remain active; no preview5 exists or is claimed.

### 2.0 Reviewer Invocation Evidence

| Gate | role | invocationId | context | revision | rebuilt | verdict |
|---|---|---|---|---|---|---|
| Preview | prototype_qa_reviewer | `qa-airribbon-preview4-20260812-fresh-f1` | fresh_context | preview4+qa4+interaction5+visual4 | yes | pass |

### 2.1 Input Readiness

| Fact | Source | Assertion | Verdict |
|---|---|---|---|
| Design-system review | critique r8 | active pass | pass |
| states/transitions | interaction5 §10 | 7 states, 12 transitions with recovery/exit | pass |
| component structure | visual4 §5/§5.1 | 7×8=56 units | pass |
| renderSpec | visual4 components | 28 stable element ids | pass |
| dataBindings | visual4 components | 36 source paths with fallback/type | pass |
| variants/states | visual4 | triggerable demo groups | pass |
| responsive/motion | interaction5 §9/§13 | Large/Regular/Constrained + Reduce Motion | pass |
| visual grammar | visual4 §3–§4 | exact tokens/semantics/materials | pass |

### 2.2 Preview Coverage Manifest

#### 2.2.1 States / transitions

| Type | ID | Source | Trigger | Target/result | Confirm | included |
|---|---|---|---|---|---|---|
| state | ST-WELCOME | int §10 | direct/start | start window | N/A | yes |
| state | ST-DRAW-READY | int §10 | enter/ready | dock+artwork | N/A | yes |
| state | ST-DRAWING | int §10 | pinch | live endpoint | N/A | yes |
| state | ST-EDIT | int §10 | edit | group bounds | N/A | yes |
| state | ST-CLEAR-CONFIRM | int §10 | clear | blocking dialog | yes | yes |
| state | ST-PHOTO | int §10 | photo | frame/export | N/A | yes |
| state | ST-CONTROLLER | int §10 | controller | ray fallback | N/A | yes |
| transition | TR-ENTER | int §10 | requestEnterStage | DRAW-READY | yes | yes |
| transition | TR-PINCH | int §10 | pinchStarted | DRAWING | no | yes |
| transition | TR-RELEASE | int §10 | pinchEnded | DRAW-READY + stroke | no | yes |
| transition | TR-TRACK-LOST | int §10 | handLost | safe stop/READY | no | yes |
| transition | TR-EDIT | int §10 | selectEdit | EDIT | no | yes |
| transition | TR-DRAW | int §10 | selectDraw | READY | no | yes |
| transition | TR-CLEAR | int §10 | requestClear | CLEAR-CONFIRM | yes | yes |
| transition | TR-PHOTO | int §10 | selectPhoto | PHOTO | no | yes |
| transition | TR-EXPORT | int §10 | exportImage | success/error | permission | yes |
| transition | TR-UNDO | int §10 | undo | same state/count−1 | no | yes |
| transition | TR-EXIT | int §10 | exit/back | WELCOME | conditional | yes |
| transition | TR-CONTROLLER | int §10 | tracking unavailable | CONTROLLER | no | yes |

#### 2.2.2 renderSpec.elements[]

| Component | element IDs item-by-item | Source | Visible/conditional | included |
|---|---|---|---|---|
| TrailCanvas | trail-mesh; trail-endpoint; trail-bounds | visual §5 | endpoint drawing only; bounds edit only | yes |
| MaterialDock | mode-options; brush-options; color-options; width-options; undo-action; clear-action | visual §5 | dock hidden photo/welcome | yes |
| ArtworkManipulator | group-bounds; grab-hint; transform-readout | visual §5 | edit only | yes |
| SafetyStatus | status-shape; status-label; status-recovery | visual §5 | recovery error only | yes |
| PhotoExport | photo-frame; export-action; photo-back; export-result | visual §5 | photo only/result conditional | yes |
| StartPanel | start-title; start-guide; persistence-note; enter-action; restore-summary | visual §5 | welcome only | yes |
| ClearConfirmDialog | clear-title; clear-count; clear-cancel; clear-confirm | visual §5 | clear state only | yes |

> Each semicolon-delimited ID is one manifest item and maps one-to-one in §3.2; total 28, not component-count substitution.

#### 2.2.3 dataBindings[]

| Component | source paths item-by-item | normal | fallback/error | type | included |
|---|---|---|---|---|---|
| TrailCanvas | strokes[]; currentStroke.lastValidPoint; currentStroke.closedReason; artworkGroup.bounds; performance.frameBudgetState | 3 strokes/valid/release/bounds/normal | empty/hide/safeStop/hide/constrained | display+semantic | yes |
| MaterialDock | mode; brush.type; brush.color; brush.width; undo.depth; strokes.count | draw/neon/cyan/medium/3/3 | draw/neon/cyan/medium/0/0 | mixed | yes |
| ArtworkManipulator | artwork.bounds; group.transform; group.scale; editHint | bounds/translated/100%/hint | hide/identity/100%/default hint | display | yes |
| SafetyStatus | tracking.status; save.status; export.status; input.source; status.recovery | ready/saved/success/hand/none | controller/error/error/controller/retry | semantic | yes |
| PhotoExport | export.available; export.status; export.uri; camera.view; priorMode | yes/success/image/current/edit | disabled/error/hidden/current/draw | mixed | yes |
| StartPanel | stage.available; input.availability; save.summary; scope.persistence; app.name; guide.copy | yes/hand/3 strokes/fixed/AirRibbon/guide | no/controller/empty/fixed/AirRibbon/default | mixed | yes |
| ClearConfirmDialog | strokes.count; clear.request; clear.cancel; clear.confirm; clear.copy | 3/open/cancel/confirm/title | work/hidden/no-op/no-op/error title | mixed | yes |

> Paths are itemized and map one-to-one in §3.3; total 36.

##### 2.2.3A Authoritative per-binding denominator

|#|component · path|distinct target|normal|fallback|error|
|---:|---|---|---|---|---|
|1|TrailCanvas · strokes[]|trail-mesh.geometry|3笔|空|最后有效|
|2|TrailCanvas · currentStroke.lastValidPoint|trail-endpoint.transform|有效|隐藏|追踪丢失|
|3|TrailCanvas · currentStroke.closedReason|trail-endpoint.state|松开|安全收笔|安全收笔|
|4|TrailCanvas · artworkGroup.bounds|trail-bounds.geometry|有效|隐藏|上次有效|
|5|TrailCanvas · performance.frameBudgetState|trail-mesh.LOD|正常|已优化|4分片|
|6|MaterialDock · mode|mode-options.selected|绘制|绘制|绘制|
|7|MaterialDock · brush.type|brush-options.selected|霓虹|霓虹|霓虹|
|8|MaterialDock · brush.color|color-options.selected|青|青|青|
|9|MaterialDock · brush.width|width-options.selected|中|中|中|
|10|MaterialDock · undo.depth|undo-action.enabled|3|0禁用|0|
|11|MaterialDock · strokes.count|clear-action.enabled|3|0禁用|当前数|
|12|ArtworkManipulator · artwork.bounds|group-bounds.geometry|有效|空|已恢复|
|13|ArtworkManipulator · group.transform|group-bounds.transform|更新|默认|已恢复|
|14|ArtworkManipulator · group.scale|transform-readout.text|100%|100%|100%|
|15|ArtworkManipulator · editHint|grab-hint.text|抓住作品|先画一笔|变换未完成|
|16|SafetyStatus · tracking.status|status-shape+label|可以绘制|切手柄|操作未完成|
|17|SafetyStatus · save.status|status-label|已保存|尚未保存|保存失败|
|18|SafetyStatus · export.status|status-label/retry|已导出|未导出|可重试|
|19|SafetyStatus · input.source|status variant|手势|手柄|手柄|
|20|SafetyStatus · status.recovery|status-recovery visibility/action|隐藏|切换输入|重试|
|21|PhotoExport · export.available|export-action.enabled|可导出|禁用|重试|
|22|PhotoExport · export.status|export-result.text|已导出|未导出|可重试|
|23|PhotoExport · export.uri|export-result.detail|图片|隐藏|无|
|24|PhotoExport · camera.view|photo-frame.transform|当前视角|当前视角|当前视角|
|25|PhotoExport · priorMode|photo-back.action|编辑|绘制|绘制|
|26|StartPanel · stage.available|enter-action.enabled|可进入|不可进入|不可进入|
|27|StartPanel · input.availability|start-guide.detail|手势/手柄|手柄|权限不可用|
|28|StartPanel · save.summary|restore-summary.text|最近3笔|无作品|上次成功|
|29|StartPanel · scope.persistence|persistence-note.text|相对布局|仅相对|不承诺锚定|
|30|StartPanel · app.name|start-title.text|AirRibbon|AirRibbon|AirRibbon|
|31|StartPanel · guide.copy|start-guide.text|捏合/松开|射线/扳机|检查权限|
|32|ClearConfirmDialog · strokes.count|clear-count.text|3|当前作品|当前数|
|33|ClearConfirmDialog · clear.request|dialog.visibility|显示|隐藏|仍显示保留作品|
|34|ClearConfirmDialog · clear.cancel|clear-cancel.action|取消不改|取消|取消保留|
|35|ClearConfirmDialog · clear.confirm|clear-confirm.action|确认清空|no-op|未完成保留|
|36|ClearConfirmDialog · clear.copy|clear-title.text|确认清空|无内容|清空未完成|

#### 2.2.4 Variants / component states

| Component | triggerable denominator | Trigger | Observable result | included |
|---|---|---|---|---|
| TrailCanvas | neon;foam;paper;empty;drawing;buffering;safeStop;edit;boundaryDisabled;error | brush/state controls | mesh style/status/bounds/LOD/error | yes |
| MaterialDock | left;right;controller;photo;default;focused;selected;disabled;error;constrained | variant/state controls | mirror/hide/focus/disable | yes |
| ArtworkManipulator | oneHand;twoHand;controller;empty;focused;dragging;scaling;boundaryDisabled;error | variant/state | bounds/readout/clamp/error | yes |
| SafetyStatus | ready;drawing;safeStop;edit;saved;error;controller | status scenario | shape+label+recovery | yes |
| PhotoExport | normal;permission;controller;framing;exporting;success;permissionDenied;error | state/export | frame/dialog/result/retry | yes |
| StartPanel | new;restore;controllerOnly;permissionError;default;focused;loading;empty;error;constrained | start scenario | summary/guide/CTA/recovery/reflow | yes |
| ClearConfirmDialog | nonempty;empty;controller;constrained;hidden;open;focusedCancel;focusedConfirm;clearing;error | clear scenario | block/actions/count/error | yes |

> Denominator 64 named variant/state items; UI scenario control can trigger each by its stable `data-component-state` value.

##### 2.2.4A Authoritative per-scenario denominator

|Component|individually numbered facts|
|---|---|
|TrailCanvas|1 neon; 2 foam; 3 paper; 4 empty; 5 drawing; 6 buffering; 7 safeStop; 8 edit; 9 boundaryDisabled; 10 error|
|MaterialDock|11 left; 12 right; 13 controller; 14 photo; 15 default; 16 focused; 17 selected; 18 disabled; 19 error; 20 constrained|
|ArtworkManipulator|21 oneHand; 22 twoHand; 23 controller; 24 empty; 25 focused; 26 dragging; 27 scaling; 28 boundaryDisabled; 29 error|
|SafetyStatus|30 ready; 31 drawing; 32 safeStop; 33 edit; 34 saved; 35 error; 36 controller|
|PhotoExport|37 normal; 38 permission; 39 controller; 40 framing; 41 exporting; 42 success; 43 permissionDenied; 44 error|
|StartPanel|45 new; 46 restore; 47 controllerOnly; 48 permissionError; 49 default; 50 focused; 51 loading; 52 empty; 53 error; 54 constrained|
|ClearConfirmDialog|55 nonempty; 56 empty; 57 controller; 58 constrained; 59 hidden; 60 open; 61 focusedCancel; 62 focusedConfirm; 63 clearing; 64 error|

Each selection changes `#app[data-component][data-component-state]`, proof shape, enabled/disabled action, stacking label, and the relevant scene/component. It is not a name-only selector.

#### 2.2.5 Responsive / Reduce Motion

| Scenario | Source | Window tier/content | Trigger | Expected | included |
|---|---|---|---|---|---|
| Large | int §9/visual §5.0 | 1440×900 / 1376×740 | responsive control | 7:5, whitespace | yes |
| Regular | int §9 | 1280×760 /1216×600 | responsive control | 7:5 default | yes |
| Constrained | int §9 | 720×540 /656×380 | responsive control | one column/sticky CTA | yes |
| Reduce Motion | int §13 | N/A | motion toggle | no translation/scale, short fade | yes |

### 2.3 Declarative Checklist

| Check | Source | Selector/structure | Trigger | Expected | Actual (generation) | Verdict |
|---|---|---|---|---|---|---|
| Manifest | §2.2 | report rows | compare facts | 7/12/28/36/64/4 | counts declared | pass |
| State machine | int §10 | `[data-state]`, `renderScene` | state controls | distinct focus | implemented | pass |
| Transitions | 12 rows | `[data-action]` | each action | correct target/dialog/exit | implemented | pass |
| render DOM | 28 IDs | `[data-preview-id]` | scene/state | visible/hide | implemented | pass |
| bindings modes | 36 paths | `[data-binding]` | normal/fallback/error | copy/style changes | implemented | pass |
| component scenarios | 64 items | `[data-component-state]` | scenario selector | observable inspector + scene changes | implemented | pass |
| confirmation | TR-ENTER/TR-CLEAR | `#confirm-dialog` | entry/clear | cancel/confirm block | implemented | pass |
| responsive/motion | 4 rows | `[data-responsive]`, `#reduce-motion` | controls | reflow/not scale | implemented | pass |

### 2.4 Denominator Reconciliation

| Type | design facts | Manifest | QA rebuilt | Difference | Verdict |
|---|---:|---:|---:|---:|---|
| states | 7 | 7 | 7 | 0 | pass |
| transitions | 12 | 12 | 12 | 0 | pass |
| elements | 28 | 28 | 28 | 0 | pass |
| bindings | 36 | 36 | 36 | 0 | pass |
| variants/states | 64 | 64 | 64 | 0 | pass |
| responsive/motion | 4 | 4 | 4 | 0 | pass |

### 2.5 Preview Hard Gate

Independent QA rebuilt every denominator and passed preview4; no difference and no blocking finding.

## 3. Preview Coverage

Independent verified: states 7, transitions 12, elements 28, bindings 36, scenarios 64, responsive/motion 4; all itemized, difference 0.

### 3.1 State / transition mapping

| Items | Source | Trigger | Selector | Result | Generation actual | Verdict |
|---|---|---|---|---|---|---|
| 7 state IDs listed §2.2.1 | int §10 | state buttons/actions | `[data-state]` | state scene | all render in `renderScene` | pass |
| 12 transition IDs listed §2.2.1 | int §10 | `[data-action=ID]` | action buttons incl. TR-EXIT | target/visible result | JS table + dialog/retry paths | pass |

### 3.2 renderSpec → DOM mapping

| Component.elements | Source | Visible/hide | Selector rule | Role | Actual | Verdict |
|---|---|---|---|---|---|---|
| TrailCanvas: trail-mesh,trail-endpoint,trail-bounds | visual §5 | by mode | `[data-preview-id="ID"]` | art/live/edit | unique | pass |
| MaterialDock: mode-options,brush-options,color-options,width-options,undo-action,clear-action | visual §5 | not photo/welcome | same | controls | unique | pass |
| ArtworkManipulator: group-bounds,grab-hint,transform-readout | visual §5 | edit | same | group edit | unique | pass |
| SafetyStatus: status-shape,status-label,status-recovery | visual §5 | recovery conditional | same | trust | unique | pass |
| PhotoExport: photo-frame,export-action,photo-back,export-result | visual §5 | photo | same | capture | unique | pass |
| StartPanel: start-title,start-guide,persistence-note,enter-action,restore-summary | visual §5 | welcome | same | entry | unique | pass |
| ClearConfirmDialog: clear-title,clear-count,clear-cancel,clear-confirm | visual §5 | modal | same | risk | unique | pass |

### 3.3 bindings mapping

| Paths | Target | Type | Normal trigger | Fallback/error trigger | DOM/JS | Verdict |
|---|---|---|---|---|---|---|
| 36 rows §2.2.3A | matching `[data-binding="path"]` or action selector | per row | normal switch | fallback/error switch incl. retry | `sampleData.normal/fallback/error` + `applyBindings` | pass |

### 3.4 variants / states mapping

| Items | Source | Trigger | Expected | Evidence | Verdict |
|---|---|---|---|---|---|
| 64 itemized §2.2.4 | visual component states | component + scenario select | inspector label and relevant scene style/state | `componentScenarios` + `[data-component-state]` | pass |

### 3.5 Responsive / Reduce Motion mapping

| Scenario | Source | tier | Trigger | Preserve | Change | Actual | Verdict |
|---|---|---|---|---|---|---|---|
| Large | int §9 | max | button | focus/56dp | wide shell | class `tier-large` | pass |
| Regular | int §9 | default | button | same | default 7:5 | class `tier-regular` | pass |
| Constrained | int §9 | min | button | same | one-column/sticky CTA | class `tier-constrained` | pass |
| Reduce Motion | int §13 | N/A | checkbox | semantics | disables translation/scale | class `reduce-motion` | pass |

## 4. Requirements Traceability

| Requirement | State | Component | Validation | Status |
|---|---|---|---|---|
| draw/safe stop/512 | READY/DRAWING | TrailCanvas/SafetyStatus | state + boundary scenario | covered |
| brushes/colors/width | READY | MaterialDock | controls | covered |
| group transform/mode exclusion | EDIT | ArtworkManipulator/MaterialDock | state | covered |
| undo10/clear confirm | READY/CLEAR | Dock/Dialog | actions/dialog | covered |
| photo/export | PHOTO | PhotoExport | success/error | covered |
| local relative save | WELCOME | StartPanel/SafetyStatus | normal/fallback | covered |
| controller fallback | CONTROLLER | all variants | state | covered |

Coverage 7/7 = 100% logical design scope.

## 5. Sample Data

Normal/fallback/error samples are defined in preview JS for all 36 paths; visible values are Chinese human copy and semantic enums translate via label/shape.

## 6. Web Logic Tolerance

Exact ID relationship/token presence only; excludes screenshot diff, CSS pixel→PICO physical size, device color delta, Web/PICO parity.

## 7. Device Validation Boundary

`deviceValidation.status=not_performed`：physical distance/readability, occlusion/FOV, fatigue, hand/controller precision, PICO 72Hz performance/API behavior remain device-owned. Web logical coverage = pass.

## 8. Defects

Independent QA pass; no active blocking defect.

## 9. Delivery

Preview r4 passed Stage14; QA r1 readiness/manifest predates preview1 generation, and QA5 is the active independent result. Stage16 governance-only patch does not invalidate preview facts. No runtime claim.
