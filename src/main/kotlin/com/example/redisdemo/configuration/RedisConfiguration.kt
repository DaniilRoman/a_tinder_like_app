package com.example.redisdemo.configuration

import com.example.redisdemo.model.User
import com.example.redisdemo.model.UserLike
import com.example.redisdemo.service.PushListener
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@Configuration
class RedisConfiguration(val pushListener: PushListener) {
    @Bean
    @Primary
    @Qualifier("jedisConnectionFactory")
    fun jedisConnectionFactory(): JedisConnectionFactory {
        return JedisConnectionFactory()
    }

    fun <T>redisTemplate(jedisConnectionFactory: JedisConnectionFactory): RedisTemplate<String, T> {
        val template = RedisTemplate<String, T>()
        template.setConnectionFactory(jedisConnectionFactory)
        return template
    }

    @Bean
    fun userLikeRedisTemplate(jedisConnectionFactory: JedisConnectionFactory): RedisTemplate<String, UserLike> = redisTemplate(jedisConnectionFactory)

    @Bean
    fun userRedisTemplate(jedisConnectionFactory: JedisConnectionFactory): RedisTemplate<String, User> = redisTemplate(jedisConnectionFactory)

    @Bean
    fun pushTopic() = ChannelTopic("user-push")

    @Bean
    fun messageListener() = MessageListenerAdapter(pushListener)

    @Bean
    fun redisContainer(jedisConnectionFactory: JedisConnectionFactory, messageListener: MessageListenerAdapter, pushTopic: ChannelTopic): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(jedisConnectionFactory)
        container.addMessageListener(messageListener, pushTopic)
        return container
    }
}