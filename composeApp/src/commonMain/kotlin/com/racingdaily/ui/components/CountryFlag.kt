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
import com.racingdaily.util.runSuspendCatching
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
            runSuspendCatching { Res.readBytes(path) }.getOrNull()
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
        identity.containsAny("australia", "australian", "albert park", "melbourne", "澳大利亚", "澳洲", "阿尔伯特公园") -> "au"
        identity.containsAny("portugal", "portuguese", "estoril", "fernanda pires da silva", "algarve", "portimao", "portimão", "葡萄牙", "埃斯托里尔", "阿尔加维", "波尔蒂芒") -> "pt"
        identity.containsAny("china", "chinese", "shanghai international circuit", "shanghai", "jiading", "中国", "上海", "嘉定") -> "cn"
        identity.containsAny("japan", "japanese", "suzuka international racing course", "suzuka", "fuji speedway", "oyama", "日本", "铃鹿", "富士赛道", "小山町") -> "jp"
        identity.containsAny("bahrain", "bahraini", "bahrain international circuit", "sakhir", "巴林", "萨基尔") -> "bh"
        identity.containsAny("saudi", "jeddah corniche circuit", "jeddah", "qiddiya speed park", "qiddiya", "speed park track", "沙特", "吉达", "奇迪亚", "基迪亚") -> "sa"
        identity.containsAny("emilia", "imola", "enzo e dino ferrari", "mugello", "scarperia", "italian", "italy", "monza", "意大利", "伊莫拉", "恩佐与迪诺·法拉利", "穆杰罗", "蒙扎") -> "it"
        identity.containsAny("monaco", "monegasque", "circuit de monaco", "monte carlo", "摩纳哥", "蒙特卡洛") -> "mc"
        identity.containsAny("spain", "spanish", "barcelona-catalunya", "barcelona", "montmelo", "montmeló", "jerez", "angel nieto", "ángel nieto", "motorland aragon", "motorland aragón", "alcaniz", "alcañiz", "madring", "madrid", "西班牙", "巴塞罗那", "蒙特梅洛", "赫雷斯", "安赫尔·涅托", "阿拉贡", "马德里") -> "es"
        identity.containsAny("canada", "canadian", "circuit gilles villeneuve", "gilles villeneuve", "montreal", "加拿大", "吉尔·维伦纽夫", "蒙特利尔") -> "ca"
        identity.containsAny("austria", "austrian", "red bull ring", "spielberg", "奥地利", "红牛环", "斯皮尔伯格") -> "at"
        identity == "uk" || identity.containsAny("britain", "british", "silverstone circuit", "silverstone", "united kingdom", "英国", "银石") -> "gb"
        identity.containsAny("belgium", "belgian", "circuit de spa-francorchamps", "spa-francorchamps", "spa ", "比利时", "斯帕") -> "be"
        identity.containsAny("hungary", "hungarian", "hungaroring", "mogyorod", "mogyoród", "budapest", "匈牙利", "亨格罗林", "布达佩斯") -> "hu"
        identity.containsAny("netherlands", "dutch", "circuit zandvoort", "zandvoort", "荷兰", "赞德沃特") -> "nl"
        identity.containsAny("azerbaijan", "azerbaijani", "baku city circuit", "baku", "阿塞拜疆", "巴库") -> "az"
        identity.containsAny("singapore", "singaporean", "marina bay street circuit", "marina bay", "新加坡", "滨海湾") -> "sg"
        identity.containsAny("mexico", "mexican", "hermanos rodriguez", "hermanos rodríguez", "mexico city", "墨西哥", "罗德里格斯兄弟") -> "mx"
        identity.containsAny("brazil", "brazilian", "jose carlos pace", "josé carlos pace", "sao paulo", "são paulo", "interlagos", "巴西", "若泽·卡洛斯·帕切", "英特拉格斯", "圣保罗") -> "br"
        identity.containsAny("qatar", "qatari", "lusail international circuit", "lusail", "卡塔尔", "卢赛尔", "罗塞尔") -> "qa"
        identity.containsAny("abu dhabi", "yas marina circuit", "yas marina", "dubai autodrome", "dubai", "united arab emirates", "阿联酋", "阿布扎比", "亚斯码头", "迪拜赛车场", "迪拜") -> "ae"
        identity.containsAny("miami international autodrome", "miami", "las vegas strip circuit", "las vegas", "circuit of the americas", "austin", "indianapolis motor speedway", "indianapolis", "united states", "american", "usa", "美国", "迈阿密", "拉斯维加斯", "奥斯汀", "美洲赛道", "印第安纳波利斯") -> "us"
        identity.containsAny("france", "french", "circuit paul ricard", "paul ricard", "nevers magny-cours", "magny-cours", "le castellet", "法国", "保罗·里卡德", "马尼库尔", "勒卡斯泰莱") -> "fr"
        identity.containsAny("germany", "german", "hockenheimring", "hockenheim", "nürburgring", "nurburgring", "nürburg", "nurburg", "德国", "霍根海姆", "纽博格林", "纽伯格林") -> "de"
        identity.containsAny("malaysia", "malaysian", "petronas sepang", "sepang international circuit", "sepang", "马来西亚", "雪邦") -> "my"
        identity.containsAny("turkey", "turkish", "tosfed istanbul park", "tosfed i̇stanbul park", "stanbul park", "istanbul", "tuzla", "土耳其", "伊斯坦布尔", "图兹拉") -> "tr"
        identity.containsAny("russia", "russian", "igora drive", "novozhilovo", "moscow raceway", "volokolamsk", "sochi", "俄罗斯", "伊戈拉", "莫斯科赛道", "沃洛科拉姆斯克", "索契") -> "ru"
        identity.containsAny("india", "indian", "buddh international circuit", "greater noida", "印度", "佛陀国际赛道", "大诺伊达") -> "in"
        identity.containsAny("south korea", "korean", "korea international circuit", "yeongam", "韩国", "韩国国际赛道", "灵岩") -> "kr"
        identity.containsAny("kuwait", "kuwaiti", "kuwait motor town", "ahmadi", "科威特", "科威特汽车城", "艾哈迈迪") -> "kw"
        identity.containsAny("south africa", "south african", "kyalami", "南非") -> "za"
        identity.containsAny("algeria", "algerian", "阿尔及利亚") -> "dz"
        identity.containsAny("new zealand", "new zealander", "kiwi", "新西兰") -> "nz"
        identity.containsAny("argentina", "argentine", "argentinian", "阿根廷") -> "ar"
        identity.containsAny("thailand", "thai", "chang international circuit", "buriram", "泰国", "昌国际赛道", "武里南") -> "th"
        identity.containsAny("finland", "finnish", "芬兰") -> "fi"
        else -> null
    } ?: return null
    return "files/flags-svg/flag_$code.svg"
}

private fun String.containsAny(vararg candidates: String): Boolean = candidates.any(::contains)
