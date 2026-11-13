package com.example.gastikmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform