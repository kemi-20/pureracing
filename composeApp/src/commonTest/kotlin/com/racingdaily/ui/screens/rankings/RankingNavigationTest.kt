package com.racingdaily.ui.screens.rankings

import com.racingdaily.data.model.RankingNavData
import com.racingdaily.data.model.RankingNavItem
import com.racingdaily.data.model.RankingOption
import kotlin.test.Test
import kotlin.test.assertEquals

class RankingNavigationTest {
    @Test
    fun f1SeasonsIgnoreOtherChampionshipGroupsAndDuplicates() {
        val navigation = RankingNavData(
            list = listOf(
                RankingNavItem(options = listOf(RankingOption(chp_id = 9, id = 2026))),
                RankingNavItem(
                    options = listOf(
                        RankingOption(chp_id = 6, id = 2026),
                        RankingOption(chp_id = 6, id = 2026),
                        RankingOption(chp_id = 6, id = 2025)
                    )
                )
            )
        )

        assertEquals(listOf(2026, 2025), navigation.f1Seasons().map { it.id })
    }
}
