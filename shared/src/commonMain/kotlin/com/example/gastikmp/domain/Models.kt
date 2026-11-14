package com.example.gastikmp.domain

data class User(
    val email: String,
    val name: String,
    val token: String = "token_session"
)
