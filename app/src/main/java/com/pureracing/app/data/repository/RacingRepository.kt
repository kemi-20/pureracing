package com.pureracing.app.data.repository

import com.pureracing.app.data.api.RacingApiService
import com.pureracing.app.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RacingRepository @Inject constructor(private val api: RacingApiService) {

    suspend fun getSeasons() = api.getSeasons()
    suspend fun getSchedule(seasonId: Int) = api.getSchedule(seasonId)
    suspend fun getRaceDetail(raceId: Int) = api.getRaceDetail(raceId)
    suspend fun getRankings(seasonId: Int, type: String) = api.getRankings(seasonId, type)
    suspend fun getTeams(seasonId: Int) = api.getTeams(seasonId)
    suspend fun getTeamDetail(id: Int) = api.getTeamDetail(id)
    suspend fun getNews(page: Int = 1) = api.getNews(page)
    suspend fun toggleCollection(type: String, id: Int, action: Int) =
        api.toggleCollection(CollectionRequest(type, id, action))
}
