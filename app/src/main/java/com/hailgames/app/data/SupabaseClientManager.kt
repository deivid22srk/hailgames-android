package com.hailgames.app.data

import android.content.Context
import com.hailgames.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseClientManager {
    private lateinit var _client: SupabaseClient

    val client: SupabaseClient
        get() = _client

    fun init(context: Context) {
        if (::_client.isInitialized) return
        _client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            httpEngine = OkHttp
        }
    }
}
