package com.example.redisdemo.service

import com.example.redisdemo.model.Constants
import com.example.redisdemo.model.User
import com.example.redisdemo.model.UserLike
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class UserLikeService(private val userLikeRedisTemplate: RedisTemplate<String, UserLike>,
                      private val pushProducer: PushProducer) {

    private val USERS_BATCH_LIMIT = 100L

    fun putUserLike(userFrom: String, userTo: String, like: Boolean) {
        val userLike = UserLike(userFrom, userTo, like)
        val listOps: ListOperations<String, UserLike> = userLikeRedisTemplate.opsForList()
        listOps.rightPush(Constants.USER_LIKES, userLike)
    }

    fun processUserLikes() {
        val userLikes = getUserLikesLast(USERS_BATCH_LIMIT).filter { it.isLike }
        pushLikesToUsers(userLikes)
        userLikes.forEach { updateUserLike(it) }
    }

    private fun getUserLikesLast(number: Long): List<UserLike> {
        val listOps: ListOperations<String, UserLike> = userLikeRedisTemplate.opsForList()
        return (listOps.range(Constants.USER_LIKES, 0, number)?: mutableListOf()).filterIsInstance(UserLike::class.java)
            .also {
                listOps.trim(Constants.USER_LIKES, number, -1)
            }
    }

    private fun pushLikesToUsers(userLikes: List<UserLike>) {
        GlobalScope.launch(Dispatchers.IO) {
            userLikes.forEach {
                pushProducer.publish(it)
            }
        }
    }

    private fun updateUserLike(userLike: UserLike) {
        val userOps: HashOperations<String, String, User> = userLikeRedisTemplate.opsForHash()
        val fromUser = (userOps.get(Constants.USERS, userLike.fromUserId)?: IllegalArgumentException()) as User
        fromUser.fromLikes.add(userLike)
        val toUser = (userOps.get(Constants.USERS, userLike.toUserId)?: IllegalArgumentException()) as User
        toUser.fromLikes.add(userLike)
        userOps.putAll(Constants.USERS, mapOf(userLike.fromUserId to fromUser, userLike.toUserId to toUser))
    }
}