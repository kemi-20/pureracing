# Third Party Notices

PureRacing includes adapted source files from the following open-source projects.

## AndroidLiquidGlass

- Project: AndroidLiquidGlass
- Author: Kyant
- Source: https://github.com/Kyant0/AndroidLiquidGlass/tree/2.0.0
- License: Apache License 2.0
- Vendored files: `composeApp/src/androidMain/kotlin/com/racingdaily/ui/liquidglass/`
- Local changes: package/import paths adjusted so the catalog example components can be called from PureRacing's Android source set.

The app also depends on `io.github.kyant0:backdrop:2.0.0` and `io.github.kyant0:shapes:1.2.0`.

Team logos are loaded from the official API response and are not bundled with PureRacing.

## Media Chrome

- Project: Media Chrome
- Author: Mux, Inc.
- Source: https://github.com/muxinc/media-chrome/tree/v4.19.2
- License: MIT
- Vendored file: `composeApp/src/commonMain/composeResources/files/article-player/media-chrome-4.19.2.iife.js`

## Chewie

- Project: Chewie
- Authors: Christian Muehle and contributors
- Source: https://github.com/fluttercommunity/chewie
- License: MIT
- Adapted component: `CupertinoControls`

## WebKit Modern Media Controls

- Project: WebKit
- Source: https://github.com/WebKit/WebKit/tree/main/Source/WebCore/Modules/modern-media-controls
- License: BSD 2-Clause
- Adapted resources: iOS play, pause, seek, volume, and fullscreen SVG paths

The article player template combines Chewie's open-source Cupertino control layout with WebKit's
official iOS control glyphs. Media Chrome binds those controls to the native HTML video element on
Android WebView and Windows WebView2 while keeping the original media URLs unchanged.
