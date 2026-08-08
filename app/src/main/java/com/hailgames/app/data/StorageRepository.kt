package com.hailgames.app.data

import io.github.jan.supabase.storage.storage
import java.util.UUID

class StorageRepository(
    private val clientManager: SupabaseClientManager = SupabaseClientManager
) {
    private val bucket get() = clientManager.client.storage.from("content")

    suspend fun uploadImage(fileName: String, bytes: ByteArray): String {
        val cleanName = fileName.substringBeforeLast('.').ifBlank { "image" }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
        val ext = fileName.substringAfterLast('.', "png").lowercase()
        val path = "covers/${UUID.randomUUID()}_$cleanName.$ext"
        bucket.upload(path, bytes)
        return path
    }

    fun publicUrl(path: String): String = bucket.publicUrl(path)

    suspend fun deleteImage(path: String) {
        runCatching { bucket.delete(path) }
    }
}
