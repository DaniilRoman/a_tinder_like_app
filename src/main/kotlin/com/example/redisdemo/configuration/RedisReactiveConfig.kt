package com.example.redisdemo.configuration

import com.example.redisdemo.model.Constants.USER_GEO_CONSUMER_GROUP_NAME
import com.example.redisdemo.model.Constants.USER_GEO_STREAM_NAME
import com.example.redisdemo.model.UserPoint
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.stream.StreamListener
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.Subscription
import java.net.InetAddress
import java.time.Duration

@Configuration
class RedisStreamsConfig(private val streamListener: StreamListener<String, ObjectRecord<String, UserPoint>>) {

    @Bean
    @Qualifier("lettuceConnectionFactory")
    fun lettuceConnectionFactory(): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory()
    }

    @Bean
    fun reactiveRedisTemplate(lettuceConnectionFactory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer: Jackson2JsonRedisSerializer<Any> = Jackson2JsonRedisSerializer(Any::class.java)
        val builder: RedisSerializationContext.RedisSerializationContextBuilder<String, Any> =
            RedisSerializationContext.newSerializationContext(keySerializer)
        val context: RedisSerializationContext<String, Any> = builder.value(valueSerializer).build()
        return ReactiveRedisTemplate(lettuceConnectionFactory, context)
    }

    @Bean
    fun subscription(jedisConnectionFactory: RedisConnectionFactory): Subscription {
        val options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .targetType(UserPoint::class.java)
            .build()

        val listenerContainer = StreamMessageListenerContainer.create(jedisConnectionFactory, options)

        val subscription = listenerContainer.receiveAutoAck(
            Consumer.from(USER_GEO_CONSUMER_GROUP_NAME, InetAddress.getLocalHost().hostName),
            StreamOffset.create(USER_GEO_STREAM_NAME, ReadOffset.lastConsumed()),
            streamListener
        )
        listenerContainer.start()
        return subscription
    }
}