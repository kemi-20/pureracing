package com.racingdaily.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.racingdaily.resources.*
import com.racingdaily.util.alpineLogoColorFilter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TeamLogo(
    teamId: Int,
    seasonId: Int,
    teamName: String,
    fallbackUrl: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val localLogo = localTeamLogoResource(teamId, seasonId, teamName)
    if (localLogo != null) {
        val darkTheme = isSystemInDarkTheme()
        val localColorFilter = when {
            localLogo == Res.drawable.team_cadillac -> ColorFilter.tint(
                if (darkTheme) Color(0xFFE8D18A) else Color(0xFFB2863F)
            )
            localLogo == Res.drawable.team_alpine && darkTheme ->
                ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            localLogo.usesContentTint() -> ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            else -> null
        }
        Image(
            painter = painterResource(localLogo),
            contentDescription = teamName.ifBlank { null },
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = localColorFilter
        )
    } else {
        AsyncImage(
            model = fallbackUrl,
            contentDescription = teamName.ifBlank { null },
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = alpineLogoColorFilter(teamId)
        )
    }
}

private fun localTeamLogoResource(teamId: Int, seasonId: Int, teamName: String): DrawableResource? {
    val identity = teamName.lowercase().filterNot { it.isWhitespace() || it in "-_." }
    when (teamId) {
        79 -> return when {
            identity.containsAny("奥迪", "audi") -> Res.drawable.team_audi
            identity.containsAny("阿尔法罗密欧", "alfaromeo") -> Res.drawable.history_alfa_romeo
            identity.containsAny("索伯", "sauber") -> Res.drawable.history_sauber
            seasonId >= 2026 -> Res.drawable.team_audi
            seasonId >= 2024 -> Res.drawable.history_sauber
            seasonId >= 2019 -> Res.drawable.history_alfa_romeo
            else -> null
        }
        80 -> return Res.drawable.team_ferrari
        81 -> return Res.drawable.team_mercedes
        82 -> return Res.drawable.team_aston_martin
        83 -> return Res.drawable.team_mclaren
        84 -> return Res.drawable.team_williams
        85 -> return Res.drawable.team_red_bull
        86 -> return Res.drawable.team_haas
        87 -> return if (seasonId >= 2024) Res.drawable.team_racing_bulls else Res.drawable.history_alpha_tauri
        88 -> return Res.drawable.team_alpine
        210004 -> return Res.drawable.history_renault_f1
        210005 -> return Res.drawable.history_toro_rosso
        210010 -> return Res.drawable.history_force_india
        210015 -> return Res.drawable.history_sauber
        210211 -> return Res.drawable.history_racing_point
        210212 -> return Res.drawable.team_cadillac
    }

    return when {
        identity.containsAny("奥迪", "audi") -> Res.drawable.team_audi
        identity.containsAny("法拉利", "ferrari") -> Res.drawable.team_ferrari
        identity.containsAny("梅赛德斯", "奔驰", "mercedes") -> Res.drawable.team_mercedes
        identity.containsAny("阿斯顿马丁", "astonmartin") -> Res.drawable.team_aston_martin
        identity.containsAny("迈凯伦", "mclaren") -> Res.drawable.team_mclaren
        identity.containsAny("威廉姆斯", "williams") -> Res.drawable.team_williams
        identity.containsAny("红牛二队", "racingbulls", "vcarb") -> Res.drawable.team_racing_bulls
        identity.containsAny("红牛", "redbull") -> Res.drawable.team_red_bull
        identity.containsAny("哈斯", "haas") -> Res.drawable.team_haas
        identity.containsAny("阿尔派", "alpine") -> Res.drawable.team_alpine
        identity.containsAny("凯迪拉克", "cadillac") -> Res.drawable.team_cadillac
        identity.containsAny("赛点", "racingpoint") -> Res.drawable.history_racing_point
        identity.containsAny("印度力量", "forceindia") -> Res.drawable.history_force_india
        identity.containsAny("小红牛", "tororosso") -> Res.drawable.history_toro_rosso
        identity.containsAny("alphatauri") -> Res.drawable.history_alpha_tauri
        identity.containsAny("阿尔法罗密欧", "alfaromeo") -> Res.drawable.history_alfa_romeo
        identity.containsAny("索伯", "sauber") -> Res.drawable.history_sauber
        identity.containsAny("雷诺", "renault") -> Res.drawable.brand_renault
        identity.containsAny("路特斯", "lotus") -> Res.drawable.brand_lotus
        identity.containsAny("宝马", "bmw") -> Res.drawable.brand_bmw
        identity.containsAny("福特", "ford") -> Res.drawable.brand_ford
        identity.containsAny("本田", "honda") -> Res.drawable.brand_honda
        identity.containsAny("丰田", "toyota") -> Res.drawable.brand_toyota
        identity.containsAny("保时捷", "porsche") -> Res.drawable.brand_porsche
        else -> null
    }
}

private fun String.containsAny(vararg values: String): Boolean = values.any(::contains)

private fun DrawableResource.usesContentTint(): Boolean = this == Res.drawable.team_aston_martin ||
    this == Res.drawable.history_alfa_romeo ||
    this == Res.drawable.history_racing_point ||
    this == Res.drawable.history_sauber ||
    this == Res.drawable.brand_renault ||
    this == Res.drawable.brand_bmw ||
    this == Res.drawable.brand_ford ||
    this == Res.drawable.brand_honda ||
    this == Res.drawable.brand_toyota ||
    this == Res.drawable.brand_porsche
