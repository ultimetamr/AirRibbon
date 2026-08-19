# AirRibbon

AirRibbon 是一款基于 PICO Spatial SDK 的中文三维涂鸦玩具。用户可以通过手势捏合或手柄扳机在空间中绘制轨迹，并快速组合成空间丝带雕塑。它面向轻量创作和娱乐体验，不是专业建模工具。

应用在 PICO 启动器中显示为“空间丝带”。

## 主要功能

- 手势绘制：拇指与食指捏合后移动约 3 cm 开始绘制，保持捏合移动，松开结束笔画。
- 手柄绘制：按住左手柄或右手柄扳机并移动约 3 cm 开始绘制，松开结束。
- 四种笔刷：霓虹丝带、彩虹丝带、泡沫和纸带。
- 三色板与三档粗细选择。
- 单笔最多保留 512 个采样点，超过后自动降采样。
- 手势追踪丢失或出现非法坐标时立即安全收笔，避免飞线。
- 绘制模式与作品编辑模式分离，减少移动作品时误画。
- 作品组以自身中心为轴进行移动、缩放和旋转。
- 支持撤销最近十笔、二次确认清空、摄影模式和 PNG 图片导出。
- 本地保存笔画数据及相对布局，不承诺跨房间持久空间锚定。
- 首次启动显示四步新手教程，可跳过，并可从主工具栏反复观看。

## 快速操作

### 手势绘制

1. 先张开手，让系统识别到松开状态。
2. 将拇指与食指捏合。
3. 保持捏合并移动约 3 cm，开始生成空间轨迹。
4. 松开手指，结束当前笔画。

### 手柄绘制

1. 使用左手柄或右手柄射线确定绘制位置。
2. 按住扳机并移动约 3 cm，开始绘制。
3. 保持扳机按下并移动手柄，生成轨迹。
4. 松开扳机，结束当前笔画。

### 编辑作品

切换到“作品编辑”模式后，可以整体移动作品，并使用面板或手柄摇杆缩放、旋转作品。缩放和旋转始终以作品自身中心为轴。

## 新手教程

应用首次启动时会依次介绍：

1. 手势捏合绘制；
2. 手柄扳机绘制；
3. 绘制与作品编辑模式；
4. 撤销、清空和图片导出。

教程显示期间会暂停绘制和作品编辑，只允许操作教程的“上一步”“下一步”“跳过”和“开始体验”按钮。完成后，可通过主工具栏的“教程”按钮重新打开。

## 技术信息

- Android 包名：`com.pico.swan.airribbon`
- 启动 Activity：`com.pico.swan.airribbon.platform.LaunchActivity`
- Spatial SDK：`0.13.3`
- 开发语言：Kotlin
- Java：17
- 运行形态：PICO Spatial SDK Stage / Mixed Reality

## 构建

确保已安装 Android SDK，并配置 Java 17：

```powershell
$Env:JAVA_HOME='C:\Users\Administrator\.jdks\corretto-17.0.13'
.\gradlew.bat testDebugUnitTest assembleDebug
```

调试 APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 安装与启动

连接 PICO 设备后，可使用 PICO CLI：

```powershell
pico-cli app install app/build/outputs/apk/debug/app-debug.apk --device <设备ID>
pico-cli app launch com.pico.swan.airribbon --activity .platform.LaunchActivity --device <设备ID>
```

手势追踪需要在 PICO 真机上验证，模拟器不能替代真实手势输入测试。

## 项目结构

```text
app/src/main/java/com/pico/swan/airribbon/
├─ data/             本地保存与图片导出
├─ domain/           笔画、作品变换及绘制用例
├─ platform/         SpatialApplication 与启动 Activity
└─ ui/airribbon/     SpatialUI、输入跟踪与轨迹网格
```

## 数据与性能约束

- 笔画以作品组局部坐标保存。
- 作品缩放范围限制为 25%–300%。
- 非有限坐标和异常采样跨度会触发安全收笔。
- 撤销栈只保留最近十笔。
- 泡沫笔刷限制球体数量，带状与管状笔刷限制网格顶点数量。
- 材质和网格资源避免在每帧重复创建。

## 说明

应用保存的是笔画数据和作品相对布局，不提供跨房间持久空间锚定保证。
