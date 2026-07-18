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

## Team And Manufacturer Logos

PureRacing embeds vector marks only to identify championship entries. Team names,
manufacturer names, logos, and trademarks remain the property of their respective owners.

- Current and historical Formula 1 team marks: Wikimedia Commons, Wikipedia file archives,
  and public GitHub mirrors of constructor identity artwork.
- Ferrari SF shield source mirror: `guedera/2025-aulas` (Brandlogos.net artwork).
- McLaren Speedmark source: Wikimedia Commons, recolored to McLaren orange (`#FF8000`).
- Red Bull and Williams identity paths: `JakobZoebl/f1-predictions`; metadata, sponsor
  names, and non-identity artwork were removed before embedding.
- Alpine and Haas emblem paths: `JakobZoebl/f1-predictions`; only the standalone
  constructor emblems are embedded.
- Racing Point roundel: Wikimedia Commons (`Racing_Point.svg`).
- Sauber standalone S mark: `zakott7876/brl-stream`; the surrounding frame and wordmark
  were removed before embedding.
- Mercedes three-pointed star: existing Wikimedia-derived outline with a local static
  silver gradient for the metallic highlight.
- Manufacturer marks: Simple Icons (CC0-1.0), including the archived Alfa Romeo mark from Simple Icons 15.0.0.
- Sponsor names and title-partner artwork are removed or cropped from embedded team marks; only the constructor identity is displayed.
- API identity reference: `https://api.romielf.com/rank/team`.

Logo selection uses both `team_id` and season because the API reuses IDs across rebrands,
including Alfa Romeo / Sauber / Audi and AlphaTauri / Racing Bulls. If no embedded vector
matches an API entry, PureRacing displays the `team_logo` URL returned by the API.
