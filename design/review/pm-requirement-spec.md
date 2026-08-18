# Spatial App Requirement Spec · AirRibbon

> Role: `product_strategist` | active revision: 6 | source: 用户原始需求 + UXR r5 | CR-PE-4

## 1. Direct Description of Outputs

本文件承载 AirRibbon 的冻结意图与质量契约。Revision 2 在竞品与领域证据基础上冻结验收锚点。

## 2. Background and Problem

- **一句话需求**：AirRibbon 是一款中文三维涂鸦玩具，用户以捏合开始、移动手留下轨迹、松开结束，快速得到可整体编辑与导出的空间丝带雕塑，而不是进入专业建模流程。
- **目标用户**：希望快速表达、没有 3D 建模经验的 PICO 用户；也兼容习惯手柄的用户。
- **使用场景**：室内安全净空中的短时站立或坐姿创作；单人本地使用。
- **佩戴姿态**：优先站立原地转身，兼容坐姿；不要求行走。
- **频率与时长**：未知；设计假设单次 5–15 分钟、间歇使用，列入假设验证。
- **空间必要性初判**：轨迹的位置、深度、尺度和身体运动共同构成作品；2D 平面无法直接表达“手在空间中雕刻”的关键时刻。

## 3. Key Moment

- **屏幕不能达到的时刻**：用户捏合后用一段自然手势在身体前方留下有厚度、连续且无飞线的丝带，松手即成为可抓取的空间作品。
- **沉浸谱系**：启动控制窗为 Shared Space Planar；明确确认后进入 Full Space 的 Stage Mixed 创作；摄影态仍在 Stage 内，结束 Stage 稳定返回控制窗。
- **入口**：先在窗口说明空间、输入与安全边界，用户主动选择“进入创作空间”。

## 4. Product Research Baseline

| Dimension | Content | Source |
|---|---|---|
| 竞品基线 | XR 空间绘画通常覆盖自由绘制、笔刷、撤销、变换与导出；详细差异由 Stage 2 记录 | Stage 2 UXR §3A |
| 决策时长 | 高频模式切换和撤销应为数秒内；当前没有 AirRibbon 实测 | evidence gap，UXR §10 |
| 安全舒适 | 丢失追踪立即结束笔画；禁止强制相机移动和持续闪烁；真机舒适度待验证 | 用户约束 + local skill methodology（primary official URL 未捕获） |

## 5. Intent Definition (frozen)

- **领域 / 子领域**：创意娱乐 / 空间自由手绘与轻量作品编排。
- **风险级别**：中等；主要风险为身体活动、误操作、追踪丢失造成飞线、性能掉帧和误清空。
- **默认空间**：启动为 Shared Space；创作为显式进入的 Full Space Stage Mixed。
- **核心场景**：首次进入；手势绘制；笔刷/颜色/粗细选择；手势丢失安全收笔；作品组变换；撤销/清空；摄影与图片导出；本地保存恢复；手柄回退。
- **数据 / AI / 传感器 / 权限**：无 AI、无云；本地笔画与相对布局；手部追踪/手势输入与可能的存储/媒体写入权限；截图使用系统允许的捕获能力。空间锚定不承诺跨房间持久。
- **协作**：不包含多人协作。

## 6. Assumptions List

| # | Assumption | Confidence | Impact | Validation Plan |
|---|---|---|---|---|
| A1 | 单次会话通常 5–15 分钟 | low | 影响疲劳、提示与保存节奏 | 5 名新手会话测试并记录时长/休息 |
| A2 | 用户可在前方约 1.5m 范围内安全挥手且无需行走 | medium | 影响 Stage 布局和安全范围 | 首次进入净空确认 + 真机边界测试 |
| A3 | 手势追踪短暂丢失可在 150ms 内识别并安全收笔 | low | 直接决定是否出现飞线 | 真机注入追踪丢失，测量回调延迟；若更慢仍不得跨 gap 连线 |
| A4 | 图片导出可通过平台截图/媒体能力完成 | medium | 影响摄影态交付 | 下游对目标 SDK 版本核对官方 API 与权限 |
| A5 | 十笔、每笔 ≤512 点的低成本网格可满足 PICO 4 Ultra / OS 6+ / Spatial SDK 0.13.3 的预算 | low | 影响“连续十笔仍流畅” | 72Hz 单帧预算 13.9ms；10×512 点、霓虹最坏 overdraw 下 p95 CPU frame <13ms，且无连续 5 帧超 13.9ms；超限先降低管段径向分片，再对已结束笔画抽稀，始终保持安全收笔 |
| A6 | 用户能正确理解“恢复相对布局但不恢复同一房间位置”的范围承诺 | low | 误解会导致重开体验失望 | 5 名用户完成“保存—离开—换位置重开”任务；≥4 人操作前能复述限制。另记录接受度（≥4/5 人评分≥3/5）；若理解或接受任一失败，强化说明并提供手动重定位入口，仍不虚构持久锚定 |

## 7. Quality Contract

- **必须达成的用户结果**：Q1 用户 60 秒内完成首笔；Q2 手势追踪丢失不跨 gap 产生飞线；Q3 连续十笔后仍可继续绘制/撤销/切模式；Q4 绘制与作品编辑互斥，用户能整体移动、均匀缩放、旋转；Q5 撤销最近十笔、清空二次确认；Q6 摄影态可构图并导出图片；Q7 产品范围承诺为“重开恢复笔画和相对作品组布局”，同时清楚告知不恢复同一房间物理位置、不承诺跨房间持久锚定（这是一项范围决定，不声称已验证足够）；Q8 手柄回退至少完成选择、撤销、组变换、导出，理想情况下可射线+按键简化绘制。
- **成功/效率**：进入 Stage 后 60 秒首笔；常用笔刷/色/粗细切换 ≤2 步；撤销 ≤2 秒；模式切换后 1 秒内出现明确状态反馈；每笔保存点数 ≤512；PICO 4 Ultra/OS6+/SDK0.13.3 以 72Hz（13.9ms）测试，十笔最坏负载 p95 CPU frame <13ms 且无连续 5 帧超预算。
- **风险与绝不能失败**：trackingLost 立即结束笔画且下一段新起笔；切出绘制态前结束当前笔；清空必须 Dialog 确认且可取消；导出失败可重试；保存失败不覆盖上次有效文件；禁止强制相机运动、持续闪烁、跨 gap 插值。
- **默认可见主窗口**：Shared Space 默认仅 1 个 Planar 启动/返回窗口；创作中为 1 个 Stage Mixed + 不默认叠加主要窗口，避免遮挡作品。
- **领域组件丰富度**：核心必须体现 `Stroke/HandTrailMesh/BrushSpec/ModeGate/ArtworkGroup/Undo/Export`，不以通用 dashboard 卡片代替。
- **数据可信**：保存/导出/追踪状态显示人类可读状态与回退；不把内部 enum 直接显示；“已保存”只在写入成功后出现。
- **PICO/空间约束**：目标锁定 PICO 4 Ultra / OS 6+ / Spatial SDK 0.13.3；设计事实遵循本技能携带的 PICO methodology（Stage/Full Space、窗口 sizing、≥56dp、正文 ≥12dp、65°×40°、Reduce Motion 等）。具体 API 签名与版本规则仍 provisional，下游须用 PICO Developer Center primary URL/rule ID 复核，差异触发变更请求；真机精度/舒适度/性能均 not_performed。
- **原创性**：落实 UXR §3A 差异机会：吸收立即绘制、作品组变换、相机/保存闭环；避免大量笔刷目录、专业 gizmo、团队原型流程；独立派生三态互斥与“安全收笔”体验，不复制竞品布局/状态/视觉。
- **设计/可读性/下游验收计划**：六份核心文档通过最小完整性；每个核心组件 8 段结构；Preview manifest 与五映射表逐项 100% 对齐；独立 reviewers 重建证据；下游设备验证覆盖 ten-stroke frame time、tracking loss、hit precision、截图 API 与权限。

## 8. Requirements Traceability

| Requirement | Implementation Node | Validation Method |
|---|---|---|
| 捏合-移动-松开绘制 | T1/T2; `TrailCanvas`; ST-DRAWING | Preview 状态机 + 真机手追踪 |
| 三笔刷/三色/粗细 | `BrushPalette`; BrushSpec | Preview 变体触发 |
| 每笔 512 点与降采样 | Stroke.sampleBudget; `TrailCanvas` 状态 | 数据样例 + 下游单测/性能测试 |
| 丢失追踪不飞线 | TR-TRACK-LOST; `SafetyStatus` | Preview exception + 真机注入 |
| 作品组移动/缩放/旋转 | ST-EDIT; `ArtworkManipulator` | Preview 组变换 + 真机双手/手柄 |
| 绘制/编辑明确切换 | `ModeGate`; ST-DRAW/ST-EDIT | Preview 状态互斥 |
| 撤销十笔/清空确认 | `HistoryControls`; TR-CLEAR | 撤销计数 + Dialog confirm/cancel |
| 摄影/图片导出 | ST-PHOTO; `PhotoExport` | Preview success/error + 设备 API |
| 本地保存相对布局 | SaveDocument; `SaveStatus` | fallback 样例 + 重启回读测试 |
| 手柄回退 | interaction §12 controller matrix | Preview input toggle + 真机 controller |
| 低成本网格/材质 | HandTrailMesh budget | 设计 budget + 设备 GPU/帧时间 |
| 截图绘制中/摄影态 | preview.html scenes | 导出 Web 截图作为设计证据，不冒充设备截图 |

## 9. Minimum Completeness Gate

| Check Item | Evidence Anchor | Verdict |
|---|---|---|
| Background and intent | §2–§5 | pass |
| Assumption governance | §6 | pass |
| Quality contract | §7 九项完整且可验收 | pass |
| Requirements traceability | §8 覆盖全部强制需求 | pass |

| Field | Value |
|---|---|
| minimumCompletenessGate | pass |

## 10. Delivery and Recipients

冻结意图与质量契约交给 task_decision_designer、interaction_xr_designer 与所有 reviewer。
