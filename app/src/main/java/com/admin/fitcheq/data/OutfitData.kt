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

    val category: String = "",            // e.g. "tshirt"
    val type: String = "",                // e.g. "polo"
    val color: String = "",               // e.g. "beige"
    val style: List<String> = emptyList(),         // e.g. ["oldmoney", "minimalist"]
    val occasion: List<String> = emptyList(),      // e.g. ["college", "office", "date"]
    val season: List<String> = emptyList(),       // e.g. ["summer", "spring"]
    val fit: String = "",                // e.g. "oversized", "slim"
    val material: String = "",           //linen
)
