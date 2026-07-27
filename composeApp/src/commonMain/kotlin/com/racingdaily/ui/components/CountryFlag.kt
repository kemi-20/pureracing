package com.racingdaily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.svg.SvgDecoder
import com.racingdaily.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
fun NationalityFlags(
    nationality: String,
    remoteFallbackUrl: String,
    modifier: Modifier = Modifier
) {
    val countries = remember(nationality) {
        nationality
            .split(Regex("""\s*(?:\+|＋|/|、|,|，|;|；|\|)\s*"""))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
    val visibleCountries = countries.ifEmpty { listOf(nationality) }
    val isMultiple = visibleCountries.size > 1

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visibleCountries.forEach { country ->
            HighResolutionFlag(
                identity = country,
                remoteFallbackUrl = remoteFallbackUrl.takeIf { !isMultiple }.orEmpty(),
                contentDescription = country.ifBlank { "国籍" },
                modifier = if (isMultiple) {
                    Modifier.width(32.dp).height(24.dp)
                } else {
                    Modifier.width(44.dp).height(33.dp)
                }
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HighResolutionFlag(
    identity: String,
    remoteFallbackUrl: String,
    contentDescription: String?,
    modifier: Modifier
) {
    val imageModifier = modifier
        .clip(RoundedCornerShape(7.dp))
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.18f))
    val localFlagPath = remember(identity) { countryFlagPath(identity) }
    val localFlagBytes by produceState<ByteArray?>(initialValue = null, localFlagPath) {
        value = localFlagPath?.let { path ->
            runCatching { Res.readBytes(path) }.getOrNull()
        }
    }
    val remoteLogo = remoteFallbackUrl.takeIf { it.isNotBlank() }
    var useRemote by remember(localFlagPath, remoteLogo) { mutableStateOf(false) }
    val platformContext = LocalPlatformContext.current
    val model = when {
        !useRemote && localFlagBytes != null -> ImageRequest.Builder(platformContext)
            .data(localFlagBytes)
            .decoderFactory(SvgDecoder.Factory())
            .memoryCacheKey(localFlagPath)
            .diskCacheKey(localFlagPath)
            .build()
        remoteLogo != null -> remoteLogo
        else -> null
    }

    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = ContentScale.Crop,
            onError = {
                if (!useRemote && remoteLogo != null && localFlagBytes != null) {
                    useRemote = true
                }
            }
        )
    } else {
        Box(imageModifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Flag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun countryFlagPath(rawIdentity: String): String? {
    val identity = rawIdentity.trim().lowercase()
    val code = when {
        identity.containsAny("australia", "australian", "melbourne", "澳大利亚", "澳洲") -> "au"
        identity.containsAny("china", "chinese", "shanghai", "中国", "上海") -> "cn"
        identity.containsAny("japan", "japanese", "suzuka", "日本", "铃鹿") -> "jp"
        identity.containsAny("bahrain", "bahraini", "sakhir", "巴林") -> "bh"
        identity.containsAny("saudi", "jeddah", "沙特", "吉达") -> "sa"
        identity.containsAny("emilia", "imola", "italian", "italy", "monza", "意大利", "伊莫拉", "蒙扎") -> "it"
        identity.containsAny("monaco", "monegasque", "monte carlo", "摩纳哥") -> "mc"
        identity.containsAny("spain", "spanish", "barcelona", "madrid", "西班牙", "巴塞罗那", "马德里") -> "es"
        identity.containsAny("canada", "canadian", "montreal", "加拿大", "蒙特利尔") -> "ca"
        identity.containsAny("austria", "austrian", "spielberg", "奥地利") -> "at"
        identity == "uk" || identity.containsAny("britain", "british", "silverstone", "united kingdom", "英国", "银石") -> "gb"
        identity.containsAny("belgium", "belgian", "spa-francorchamps", "spa ", "比利时", "斯帕") -> "be"
        identity.containsAny("hungary", "hungarian", "budapest", "匈牙利", "布达佩斯") -> "hu"
        identity.containsAny("netherlands", "dutch", "zandvoort", "荷兰", "赞德沃特") -> "nl"
        identity.containsAny("azerbaijan", "azerbaijani", "baku", "阿塞拜疆", "巴库") -> "az"
        identity.containsAny("singapore", "singaporean", "marina bay", "新加坡", "滨海湾") -> "sg"
        identity.containsAny("mexico", "mexican", "墨西哥") -> "mx"
        identity.containsAny("brazil", "brazilian", "sao paulo", "interlagos", "巴西", "圣保罗") -> "br"
        identity.containsAny("qatar", "qatari", "lusail", "卡塔尔", "卢赛尔", "罗塞尔") -> "qa"
        identity.containsAny("abu dhabi", "yas marina", "united arab emirates", "阿联酋", "阿布扎比", "亚斯码头") -> "ae"
        identity.containsAny("miami", "las vegas", "austin", "united states", "american", "usa", "美国", "迈阿密", "拉斯维加斯", "奥斯汀", "美洲赛道") -> "us"
        identity.containsAny("france", "french", "paul ricard", "法国") -> "fr"
        identity.containsAny("germany", "german", "hockenheim", "nurburgring", "德国") -> "de"
        identity.containsAny("malaysia", "malaysian", "sepang", "马来西亚", "雪邦") -> "my"
        identity.containsAny("turkey", "turkish", "istanbul", "土耳其", "伊斯坦布尔") -> "tr"
        identity.containsAny("russia", "russian", "sochi", "俄罗斯", "索契") -> "ru"
        identity.containsAny("south africa", "south african", "kyalami", "南非") -> "za"
        identity.containsAny("algeria", "algerian", "阿尔及利亚") -> "dz"
        identity.containsAny("new zealand", "new zealander", "kiwi", "新西兰") -> "nz"
        identity.containsAny("argentina", "argentine", "argentinian", "阿根廷") -> "ar"
        identity.containsAny("thailand", "thai", "泰国") -> "th"
        identity.containsAny("finland", "finnish", "芬兰") -> "fi"
        else -> null
    } ?: return null
    return "files/flags-svg/flag_$code.svg"
}

private fun String.containsAny(vararg candidates: String): Boolean = candidates.any(::contains)
