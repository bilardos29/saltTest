package com.example.newsportal.db

data class News(
    val articles: List<Article>,
    val status: String,
    val totalResults: Int
)