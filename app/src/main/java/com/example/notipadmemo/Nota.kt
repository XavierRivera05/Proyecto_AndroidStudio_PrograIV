package com.example.notipadmemo

data class Nota(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val time: Long = 0L
)
