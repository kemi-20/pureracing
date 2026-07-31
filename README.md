# PureRacing

PureRacing 是一款面向 Android 与 Windows 桌面端的非官方赛车新闻与赛果客户端，基于 Compose Multiplatform 构建。

应用无需登录即可使用，支持浏览公开评论与回复，但不提供登录、注册、发表评论或用户聊天功能。

## 功能特性

- 分类新闻信息流、关键词搜索、阅读位置保留与后台预加载。
- 原生文章阅读体验，支持图片、视频、完整标题滚动、系统分享，以及评论和回复浏览。
- 基于 WebKit iOS 媒体控件资源打造的视频播放器，支持首帧预览、进度拖动、前后 15 秒跳转和全屏播放。
- 当前赛季完整赛历，提供比赛与各赛段时间、天气、赛道资料、完整成绩和轮胎策略。
- 车手与车队积分榜，支持赛季切换、历年成绩，以及车手和车队详情。
- 内置高精度 SVG 国旗，并针对赛历、比赛详情和车手国籍统一展示。
- MotoGP、TCR 与自定义锦标赛入口，以及跟随系统、浅色、深色三种主题模式。
- 赛历和排名缓存优先显示，并在后台静默刷新最新数据。
- 使用 Kyant0 Backdrop 2.0.0 的跨平台液态玻璃组件与动态交互。
- 同一套共享界面支持 Android APK 与 Windows 桌面分发包。

## 技术栈

- Kotlin `2.4.10` 与 Kotlin Multiplatform
- Compose Multiplatform `1.11.0`、Compose Material 3 与 Compose Resources
- Ktor Client `3.x`，Android 使用 OkHttp，Desktop 使用 CIO
- kotlinx.serialization 与 kotlinx.coroutines
- Coil `3.4.0`，包含 Ktor 网络加载与 SVG 解码
- Kyant0 Backdrop `2.0.0` 与 Kyant Shapes `1.2.0`
- Android Activity Compose、Android WebView 与系统分享桥接
- Windows Compose Desktop、SWT Browser 与 WebView2 文章渲染
- Media Chrome 播放状态层与 WebKit modern-media-controls 开源视觉资源
- Gradle Version Catalog、GitHub Actions、Android APK 签名与 Windows EXE 打包

## 项目结构

```text
composeApp/
  src/
    commonMain/     共享的 Compose UI、数据模型、API 服务、平台契约
    androidMain/    Android Activity、WebView、分享/返回处理、Android 玻璃组件
    desktopMain/    Windows 桌面入口、SWT 文章视图、桌面处理器
gradle/
  libs.versions.toml
.github/
  workflows/build.yml
```

## 构建

本仓库主要通过 GitHub Actions 进行构建。

该工作流会构建：

- `racingdaily-android-release`：已签名的 Android 发布版 APK
- `racingdaily-windows`：Windows 桌面分发包

推送到 `main` 后，可通过以下命令查看构建情况：

```bash
gh run list --branch main --limit 5
gh run view <run-id>
```

构建产物可从成功的工作流运行页面下载。

## 本地开发

如果你想在本地运行 Gradle，请先安装合适的 JDK。

Android：

```bash
./gradlew :androidApp:assembleDebug
```

桌面端：

```bash
./gradlew :composeApp:run
```

Windows 分发包：

```bash
./gradlew :composeApp:createDistributable
```

## 第三方代码

PureRacing 包含改编自 Kyant0 AndroidLiquidGlass 目录示例的源码，并依赖 Kyant Backdrop 库。详见：

```text
THIRD_PARTY_NOTICES.md
```

## 许可证

本项目基于 MIT 许可证授权。详情参见 `LICENSE`。

PureRacing 是非官方的第三方客户端，与 RacingDaily 或其上游 API 提供方无任何关联。
