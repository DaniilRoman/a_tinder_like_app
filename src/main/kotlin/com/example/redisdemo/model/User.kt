package com.example.redisdemo.model

import org.springframework.data.geo.Point
import java.io.Serializable


data class User(val id: String, val name: String, val labels: MutableList<String> = mutableListOf(),
                val toLikes: MutableSet<UserLike> = mutableSetOf(),
                val fromLikes: MutableSet<UserLike> = mutableSetOf()): Serializable

data class UserLike(val fromUserId: String, val toUserId: String, val isLike: Boolean): Serializable

data class UserPoint(val id: String, val point: Point): Serializable