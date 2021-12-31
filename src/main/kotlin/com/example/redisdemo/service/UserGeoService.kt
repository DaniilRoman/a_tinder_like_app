package com.example.redisdemo.service

import com.example.redisdemo.model.Constants.USER_GEO_POINT
import com.example.redisdemo.model.UserPoint
import org.springframework.data.geo.Distance
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.GeoOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class UserGeoService(private val stringRedisTemplate: RedisTemplate<String, String>) {

    fun getNearUserIds(userId: String, distance: Double = 1000.0): List<String> {
        val geoOps: GeoOperations<String, String> = stringRedisTemplate.opsForGeo()
        return geoOps.radius(USER_GEO_POINT, userId, Distance(distance, RedisGeoCommands.DistanceUnit.KILOMETERS))
            ?.content?.map { it.content.name }?.filter { it != userId }?: listOf()
    }

    fun addUserPoint(userPoint: UserPoint) {
        val geoOps: GeoOperations<String, String> = stringRedisTemplate.opsForGeo()
        geoOps.add(USER_GEO_POINT, userPoint.point, userPoint.id)
    }
}