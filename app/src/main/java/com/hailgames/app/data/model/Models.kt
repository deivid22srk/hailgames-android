package com.hailgames.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UserRole(val dbValue: String) {
    USER("user"),
    ADMIN("admin"),
    OWNER("owner");

    val isAdmin: Boolean get() = this == ADMIN || this == OWNER

    companion object {
        fun fromDb(value: String?): UserRole = entries.firstOrNull { it.dbValue == value } ?: USER
    }
}

@Serializable
data class Profile(
    val id: String,
    val username: String = "player",
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: String = "user",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    val userRole: UserRole get() = UserRole.fromDb(role)
}

@Serializable
data class Category(
    val id: String,
    val name: String,
    val icon: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ContentItem(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("link_url") val linkUrl: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("download_url") val downloadUrl: String? = null,
    val author: String? = null,
    val version: String? = null,
    @SerialName("size_mb") val sizeMb: Double? = null,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ContentItemInput(
    val title: String,
    val description: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("link_url") val linkUrl: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("download_url") val downloadUrl: String? = null,
    val author: String? = null,
    val version: String? = null,
    @SerialName("size_mb") val sizeMb: Double? = null
)
