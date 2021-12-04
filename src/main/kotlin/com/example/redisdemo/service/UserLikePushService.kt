package com.example.redisdemo.service

import com.example.redisdemo.model.UserLike
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component


@Component
class PushListener(val objectMapper: ObjectMapper): MessageListener {
    private val log = KotlinLogging.logger {}

    override fun onMessage(userLikeMessage: Message, pattern: ByteArray?) {
        // websocket functionality would be here
        log.info("Received: ${objectMapper.readValue(userLikeMessage.body, UserLike::class.java)}")
    }
}

@Component
class PushProducer(val redisTemplate: RedisTemplate<String, String>, val pushTopic: ChannelTopic, val objectMapper: ObjectMapper) {

    fun publish(userLike: UserLike) {
        redisTemplate.convertAndSend(pushTopic.topic, objectMapper.writeValueAsString(userLike))
    }
}