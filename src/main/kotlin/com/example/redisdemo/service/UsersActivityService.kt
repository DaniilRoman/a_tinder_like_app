package com.example.redisdemo.service

import com.example.redisdemo.model.Constants
import org.springframework.data.redis.core.HyperLogLogOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class UsersActivityService(private val stringRedisTemplate: RedisTemplate<String, String>) {
    fun uniqueActivitiesPerDay(): Long {
        val hyperLogLogOps: HyperLogLogOperations<String, String> = stringRedisTemplate.opsForHyperLogLog()
        return hyperLogLogOps.size(Constants.TODAY_ACTIVITIES)
    }

    fun userOpenApp(userId: String): Long {
        val hyperLogLogOps: HyperLogLogOperations<String, String> = stringRedisTemplate.opsForHyperLogLog()
        return hyperLogLogOps.add(Constants.TODAY_ACTIVITIES, userId)
    }
}