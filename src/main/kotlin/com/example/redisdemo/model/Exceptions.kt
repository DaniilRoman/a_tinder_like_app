package com.example.redisdemo.model

class UserNotFoundException(userId: String): RuntimeException("User not found by $userId")