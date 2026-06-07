# PureRacing

PureRacing 是一款面向 Android 与 Windows 桌面端的非官方赛车新闻与赛果客户端。它基于 Compose Multiplatform 构建，并使用 `origin/reverses` 中记录的公开 RacingDaily/PureRacing API 接口。

本应用专注于只读浏览。登录、注册、评论及用户聊天功能均为有意不予实现。

## 功能特性

- 新闻信息流，包含分类标签页、文章详情页、媒体渲染以及系统分享。
- 赛历，包含大奖赛详情、分段（session）开始时间、分段赛果、天气以及赛道链接。
- 车手与车队排名，并提供资料 / 详情入口。
- 赛道页面，包含赛道信息与历史纪录。
- “更多”板块，涵盖自定义锦标赛、MotoGP、TCR 以及应用信息。
- 基于 Kyant0 AndroidLiquidGlass / Backdrop 的液态玻璃（Liquid Glass）界面。
- 跨平台构建目标：
  - Android APK
  - Windows 桌面分发包

## 技术栈

- Kotlin Multiplatform
- Compose Multiplatform
- Ktor Client
- kotlinx.serialization
- Coil 3
- Kyant Backdrop `2.0.0-alpha03`
- Android Activity Compose
- Windows 桌面端使用 SWT Browser 渲染文章 HTML

## API

基础地址（Base URL）：

```text
https://api.romielf.com
```

API 通常返回如下结构：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

文章媒体资源需携带必需的 referer 进行加载：

```text
news.romielf.com
```

逆向工程笔记保存在：

```text
origin/reverses/
```

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
./gradlew :composeApp:assembleDebug
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
