package com.example.redisdemo.service

import com.example.redisdemo.model.Constants
import com.example.redisdemo.model.User
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class UserService(private val userRedisTemplate: RedisTemplate<String, User>) {
    fun addUser(user: User) {
        val hashOps: HashOperations<String, String, User> = userRedisTemplate.opsForHash()
        hashOps.put(Constants.USERS, user.name, user)
    }

    fun getUser(userId: String): User {
        val userOps: HashOperations<String, String, User> = userRedisTemplate.opsForHash()
        return userOps.get(Constants.USERS, userId)?: User("user1", "empty", mutableListOf())
    }
}