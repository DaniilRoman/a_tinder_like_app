package com.example.redisdemo.service

import com.example.redisdemo.model.Constants.USER_GEO_CONSUMER_GROUP_NAME
import com.example.redisdemo.model.Constants.USER_GEO_STREAM_NAME
import com.example.redisdemo.model.UserPoint
import io.lettuce.core.RedisBusyException
import mu.KotlinLogging
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamListener
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct


@Service
class UserPointsConsumer(
    private val userGeoService: UserGeoService
) : StreamListener<String, ObjectRecord<String, UserPoint>> {

    private val userPoints = mutableMapOf<String, Point>()
    private val SWAP_THRESHOLD = 100

    override fun onMessage(record: ObjectRecord<String, UserPoint>) {
        swapToPointsStoreIfNeeded(userPoints)
        val userPoint = record.value
        userPoints[userPoint.id] = userPoint.point
    }

    private fun swapToPointsStoreIfNeeded(userPoints: MutableMap<String, Point>) {
        if (userPoints.size == SWAP_THRESHOLD) {
            userGeoService.addUserPoints(userPoints)
            userPoints.clear()
        }
    }
}

@Service
class UserPointProducer(val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>) {
    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun initConsumerGroup() {
        val streamOps = reactiveRedisTemplate.opsForStream<String, UserPoint>()
        streamOps.createGroup(USER_GEO_STREAM_NAME, USER_GEO_CONSUMER_GROUP_NAME)
            .doOnError {
                if (it.cause is RedisBusyException) {
                    log.info { "Consumer group $USER_GEO_CONSUMER_GROUP_NAME in stream $USER_GEO_STREAM_NAME has already existed" }
                }
            }
            .onErrorResume { Mono.empty() }
            .subscribe()
    }

    fun publishUserPoint(userPoint: UserPoint) {
        val userPointRecord = ObjectRecord.create(USER_GEO_STREAM_NAME, userPoint)
        reactiveRedisTemplate
            .opsForStream<String, Any>()
            .add(userPointRecord)
            .subscribe { println("Send RecordId: $it") }
    }

}
