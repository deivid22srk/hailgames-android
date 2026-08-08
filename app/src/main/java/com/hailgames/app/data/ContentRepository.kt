package com.hailgames.app.data

import com.hailgames.app.data.model.Category
import com.hailgames.app.data.model.ContentItem
import com.hailgames.app.data.model.ContentItemInput
import io.github.jan.supabase.postgrest.decodeList
import io.github.jan.supabase.postgrest.decodeSingle
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ContentRepository(
    private val clientManager: SupabaseClientManager = SupabaseClientManager
) {
    suspend fun fetchCategories(): List<Category> =
        clientManager.client.from("categories")
            .select { order("sort_order", Order.ASCENDING) }
            .decodeList<Category>()

    suspend fun fetchContentItems(): List<ContentItem> =
        clientManager.client.from("content_items")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<ContentItem>()

    suspend fun fetchContentItem(id: String): ContentItem =
        clientManager.client.from("content_items")
            .select { filter { eq("id", id) } }
            .decodeSingle<ContentItem>()

    suspend fun createContentItem(input: ContentItemInput): ContentItem =
        clientManager.client.from("content_items")
            .insert(input) { select() }
            .decodeSingle<ContentItem>()

    suspend fun updateContentItem(id: String, input: ContentItemInput): ContentItem =
        clientManager.client.from("content_items")
            .update({
                this["title"] = input.title
                this["description"] = input.description
                this["cover_url"] = input.coverUrl
                this["category_id"] = input.categoryId
                this["link_url"] = input.linkUrl
                this["file_url"] = input.fileUrl
                this["download_url"] = input.downloadUrl
                this["author"] = input.author
                this["version"] = input.version
                this["size_mb"] = input.sizeMb
            }) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle<ContentItem>()

    suspend fun deleteContentItem(id: String) {
        clientManager.client.from("content_items")
            .delete { filter { eq("id", id) } }
    }

    fun watchContentItems(): Flow<List<ContentItem>> = flow {
        emit(fetchContentItems())
    }
}
