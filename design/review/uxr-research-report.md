# User Research Report · AirRibbon

> Role: `research_analyst` | active revision: 5 | source intent: pm r1 | observed 2026-08-12 | CR-PE-4

## 1. Direct Description of Outputs

本报告提供五类证据、三项竞品基准与面向轻量空间涂鸦的领域模型。没有用户研究实测的项目明确写为 evidence gap。

## 2. Research Goals and Questions

- 验证：新手能否凭“捏合—移动—松开”完成首笔；模式切换是否防误画；追踪丢失是否绝不连飞线；十笔是否保持流畅；手柄回退是否可完成核心闭环。
- 方法计划：5 名 XR 新手 + 3 名有绘画/建模经验者，任务观察、真机追踪丢失注入、帧时间记录、导出回读。
- 当前样本：尚未招募；下述 persona 为有来源边界的 proto-persona，不伪装为访谈事实。

## 3. Five Categories of Research Evidence

| ID/Category | Evidence / Gap | Source / Type | Scope / Confidence | Observation Time | Validation Plan |
|---|---|---|---|---|---|
| M1 market | 公开文档可确认 3D 画笔、多笔刷、保存/分享/相机等类别能力；“菜单造成新手负担”仅为分析假设，不是实测事实 | [Open Brush](https://openbrush.app/), [Open Brush UI](https://docs.openbrush.app/user-guide/using-the-open-brush-tools-quick-tools-and-menu-panels), external first-party | 功能清单 high；可用性推断 low | 2026-08-12 access | 三产品同任务走查：首笔点击数、首次撤销时长、错误恢复步骤；5 名新手测试 |
| U1 user | 用户明确目标是快速得到丝带雕塑，而非专业建模 | 用户原始需求，user_supplied | AirRibbon，high | 2026-08-12 | 首次任务 60 秒内成形测试 |
| U2 user gap | 手势捏合阈值、左右利手、可接受疲劳与反馈偏好未知 | none, assumption | 目标用户，low | 2026-08-12 | 8 人分层测试并记录误触/疲劳 |
| D1 domain | 一笔必须是离散采样点序列 + 笔刷快照 + 生命周期；跨追踪 gap 不得插值 | 用户约束，user_supplied | 数据与网格生成，high | 2026-08-12 | 单元测试与可视化轨迹检查 |
| D2 domain | 每笔 512 点上限及超限降采样是性能与形状保真间的显式决策 | 用户约束，user_supplied | 笔画数据，high | 2026-08-12 | 直线/急弯/闭环基准形状误差测试 |
| P1 platform | 目标锁定 PICO 4 Ultra / OS 6+ / Spatial SDK 0.13.3；采用插件内 Stage/Full Space 与 Planar sizing methodology，但具体 API 签名和版本规则尚未 primary-doc 审计 | skill `official-rules.json` + window-sizing methodology, local methodology (primary URL not captured) | 目标版本 high；API guarantee low | plugin 0.4.1 / 2026-08-12 | 下游以 PICO Developer Center primary URL/rule ID 逐项复核；差异触发变更请求 |
| P2 platform gap | 目标 SDK 的手追踪丢失回调、截图 API 与存储权限的具体签名未在设计阶段核验 | none, assumption | 工程实现，low | 2026-08-12 | 下游环境 doctor + SDK docs 核对，不在本包猜 API |
| S1 safety | 用户要求追踪短暂丢失自动结束当前笔，禁止飞线；清空需二次确认 | 用户原始需求，user_supplied | 所有输入，high | 2026-08-12 | 注入 trackingLost 与取消/确认清空测试 |
| S2 safety | 本设计把大面积/大位移内容运动视为舒适风险，禁用强制相机运动并提供 Reduce Motion 淡入淡出 | local skill methodology/safety baseline（未捕获 primary URL） | 设计安全基线 high；官方版本保证 low | 2026-08-12 | 下游 primary docs 复核 + 真机主观舒适度，当前 not_performed |

- **冲突处理**：本地技能规则用于设计约束；具体 SDK API/版本若与下游官方文档冲突，以目标 SDK 官方文档为准并触发变更审查。

## 3A. Competitive Benchmark

| # | Competitor / Platform | Feature needs | Interaction experience | Visual experience | Spatial-capability usage | Source / observation |
|---|---|---|---|---|---|---|
| C1 | Open Brush / 多 XR | **文档事实**：3D 空间绘制、多笔刷、选择、相机、保存/上传等 | **文档事实**：手柄面板与 Quick Tools；**低置信分析假设**：多层入口可能增加新手首笔负担，待走查 | **文档观察**：多种表现型笔触；**低置信视觉假设**：面板信息量可能偏高，不复用 | **事实**：在 3D 空间画笔；**低置信假设**：房间尺度可能诱导走动，待测试 | [官网](https://openbrush.app/)、[UI 文档](https://docs.openbrush.app/user-guide/using-the-open-brush-tools-quick-tools-and-menu-panels)、[Brush 文档](https://docs.openbrush.app/user-guide/brushes)，2026-08-12 |
| C2 | Gravity Sketch / VR+桌面 | **文档事实**：3D 绘制/建模/编辑/导出/协作、1:1 比例评估 | **文档事实**：内置教程、选择与空间移动；**低置信分析假设**：专业功能可能提高新手学习投入 | 当前资料只支持产品/人体工学定位；层级、密度、材料呈现的可靠界面观察为 **gap**，Stage 8 不得据此主张视觉差异 | **事实**：尺度、深度、空间评审；**项目范围判断**：复杂建模能力超出 AirRibbon 玩具目标 | [产品](https://gravitysketch.com/)、[首次草图](https://gravitysketch.com/blog-post/updates/your-first-sketch-in-gravitysketch/)、2026-08-12；需当前版本截图走查 |
| C3 | ShapesXR / VR+MR+Web | **文档事实**：无代码空间原型、资产导入、交互原型、多人协作 | **事实**：团队空间、实时协作与分享；**低置信分析假设**：个人即时绘画不是首要路径 | 当前资料只支持空间原型/评审定位；层级、密度、笔触/材料与空间视觉线索为 **gap**，Stage 8 不得据此主张视觉差异 | **事实**：空间编排和协作；与自由绘画的迁移价值需走查 | [About](https://learn.shapesxr.com/)、[Collaboration](https://learn.shapesxr.com/collaboration)，2026-08-12；需当前版本截图走查 |

| # | Strengths worth absorbing (need/opportunity only) | Weaknesses / anti-patterns to avoid |
|---|---|---|
| C1 | 立即绘制、笔触表现、相机/保存闭环（事实层能力） | 需验证：大笔刷目录/多层入口是否拖慢新手；不把假设当事实 |
| C2 | 作品整体变换、选择、教程反馈（文档能力） | 项目范围避免专业建模工具链；并非竞品缺陷判断 |
| C3 | 空间编排、无代码与协作表达（文档能力） | 对个人即时绘画的适配是 evidence gap，不作负面事实判断 |

- **我们的差异机会**：将空间画笔压缩为三种材料隐喻与三色三粗细；用“绘制/编辑/摄影”三态明确互斥；追踪丢失立即安全收笔；整体作品组而非逐点建模；在 PICO Stage Mixed 中保留环境感知并提供手势优先、手柄可用的完整闭环。
- **样本与缺口**：达到 3 个相邻产品；Open Brush 与 Gravity Sketch 同类度高，ShapesXR 为相邻空间创作工具。未做当前版本真机走查，具体可用性结论仍是 gap。
- **吸收边界**：只吸收能力覆盖与风险机会，不复制任何竞品布局、状态图、组件组合、色彩或动效。

## 4. Domain Model

- **工作流**：进入/安全确认 → 选笔刷/色/粗细 → 捏合开笔 → 空间采样 → 松开或 trackingLost 收笔 → 低成本网格生成 → 多笔组成作品组 → 编辑组变换 → 撤销/清空 → 摄影构图 → 导出 → 自动保存相对布局。
- **决策变量**：当前模式、input source、pinch state、tracking confidence、点间距/角度阈值、point count、brush type、color、width、stroke lifecycle、undo depth、group transform、save state、export state。
- **数据实体与时效**：`Stroke`（创建时快照，永久本地）、`StrokePoint`（绘制帧级）、`BrushSpec`（选择时）、`HandTrailMesh`（绘制时增量、结束后冻结）、`ArtworkGroupTransform`（编辑时）、`UndoStack`（最近 10 笔）、`SaveDocument`（修改后 debounce）、`ExportJob`（一次性状态）。
- **专业风险**：追踪 gap 插值产生飞线；超过 512 点无控制导致顶点爆炸；笔刷材质 overdraw；绘制与编辑并存导致误画；变换未保存；清空误操作；截图输出失败却无反馈。
- **用户心智模型**：“手是笔、空气是画布、所有笔画是一件作品、抓住作品就能摆放、相机拍的是当前构图”。
- **成熟模式与反模式**：模式互斥、直接操控、命令可撤销；反模式为隐式模式、无限历史、每笔独立专业 gizmo、丢失后补连、把 Web 预览当性能证据。

## 5. Persona

### Proto-persona：林晓 / 想在一分钟内做出“能绕着看”的东西

| Dimension | Content |
|---|---|
| 基本信息 | 18–35 岁，非专业建模者，XR 新手到中等；来源：用户目标推导，待访谈 |
| 场景与频率 | 家中短时娱乐，预计每周 1–3 次；evidence gap |
| 目标 | 不读教程也能画出有质感的空间丝带并保存分享 |
| 痛点 | 专业建模界面、手势误触、作品突然跳动或飞线 |
| 空间习惯 | 坐/站皆可，偏向原地前方操作，不要求走动 |
| 无障碍 | 色觉差异需形状/文字双通道；手追踪不可用时必须手柄回退 |
| 用户原话 | “快速得到空间丝带雕塑，而不是专业建模工具。”（用户需求） |

## 6. Journey Map

| Stage | 进入 | 首次动手 | 核心创作 | 编辑/摄影 | 退出/回访 |
|---|---|---|---|---|---|
| Goal | 知道怎么开始 | 成功画第一笔 | 连续画十笔 | 摆好作品并导出 | 下次恢复 |
| Behavior | 确认进入 Stage | 捏合-移动-松开 | 选材质/色/粗细、撤销 | 切编辑、双手变换、切摄影 | 退出自动保存 |
| Emotion | 😐 | 😀 | 😀/追踪丢失时😞 | 😀 | 😐 |
| Pain | 权限/净空不明 | 无反馈或误画 | 飞线、掉帧、工具过多 | 模式误切、导出失败 | 锚定预期不清 |
| Opportunity | 明确入口/稳定退出 | 实时起笔反馈 | 安全收笔+点预算 | 三态互斥+结果反馈 | 说明相对布局、不承诺跨房间锚定 |

- **情绪低点**：追踪丢失后出现飞线；优先级高于保留未完成笔画。

## 7. Key Findings

| # | Finding | Evidence | Confidence | Design Implication |
|---|---|---|---|---|
| F1 | “零飞线”是信任底线 | 用户 S1 + domain D1 | high | trackingLost 立即 finalize/cancel current segment，下一次起笔新建 Stroke |
| F2 | 模式互斥是避免误画的关键 | 用户明确要求；竞品面板仅为低置信分析参照 | high（用户约束）/low（竞品可用性推断） | 绘制/编辑/摄影作为顶层互斥状态，切换时先结束当前笔 |
| F3 | 轻量差异来自限制而非更多能力 | M1/C1–C3 | medium | 只保留 3 笔刷×3 色×3 粗细与作品组变换 |
| F4 | Web 无法证明帧率、精度或舒适度 | P2/S2 | high | QA 固定 `deviceValidation.status=not_performed` |

## 8. Wearing Posture and Field-of-View Insights

- 坐/站原地；主创作体积位于肩到胸前方，避免需要行走。
- 舒适手臂范围和持续时间尚无 AirRibbon 实测；核心控制维持中央 65°×40°，次要不超过 85°×55°。
- 真机疲劳阈值是 evidence gap；首轮测试单次上限 15 分钟并主动询问疲劳。

## 9. Eye-Hand Interaction Usability

- 捏合命中率、追踪丢失频率无本地实测，不能宣称达标。
- 所有控件支持目光聚焦+捏合；绘制使用直接手部位置；手柄射线/按键为回退。
- hover、armed、drawing、trackingLost 必须有不同文字/形状反馈。

## 10. Duration Baseline Data

| Decision Type | Duration Anchor | Source |
|---|---|---|
| Glance mode/brush check | design target ≤2s | project assumption，待测试 |
| First stroke | design target ≤60s from Stage entry | quality target，待测试 |
| Undo | design target ≤2s | user task target，待测试 |

## 11. Motion Sickness / Fatigue and Safety

- 风险：整体作品快速大位移/缩放、边缘闪烁、用户为绕看而行走。
- High Motion 标记：不需要主动相机运动；作品组变换限定舒缓反馈，摄影态相机固定于头部视角。
- 建议首次 10–15 分钟并原地操作；具体节奏待真机研究，当前 not_performed。

## 12. Minimum Completeness Gate

| Check Item | Evidence | Verdict |
|---|---|---|
| Five categories | §3 M/U/D/P/S，gap 显式 | pass |
| Competitive benchmark | §3A 三产品、四维、吸收/避免 | pass |
| Domain model | §4 | pass |
| User evidence | §5–§7，proto-persona 边界明确 | pass |
| Quantitative and safety | §8–§11，缺口有验证计划 | pass |

| Field | Value |
|---|---|
| minimumCompletenessGate | pass |

## 13. Delivery

交给 product_strategist、task_decision_designer、interaction_xr_designer 与 visual_designer；竞品事实仅用于能力/机会层。
