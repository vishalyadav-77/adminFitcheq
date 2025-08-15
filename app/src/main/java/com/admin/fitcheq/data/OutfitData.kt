package com.admin.fitcheq.data

data class OutfitData(
    val id: String = "",
    val link: String ="",
    val imageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val title: String = "",
    val price: String = "",
    val website: String = "",
    val gender: String = "",
    val tags: List<String> = emptyList(),
    var isFavorite: Boolean = false,

    val category: String = "",
    val type: String = "",
    val color: String = "",
    val style: List<String> = emptyList(),
    val occasion: List<String> = emptyList(),
    val season: List<String> = emptyList(),
    val fit: String = "",
    val material: String = "",
)
