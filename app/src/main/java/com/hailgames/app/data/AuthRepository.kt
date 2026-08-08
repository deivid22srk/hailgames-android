package com.hailgames.app.data

import com.hailgames.app.data.model.Profile
import com.hailgames.app.data.model.UserRole
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AuthRepository(
    private val clientManager: SupabaseClientManager = SupabaseClientManager
) {
    private val auth get() = clientManager.client.auth

    val sessionStatus: Flow<SessionStatus> get() = auth.sessionStatus

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String, username: String?) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("username", JsonPrimitive(username ?: email.substringBefore("@")))
            }
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun currentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun fetchCurrentProfile(): Profile? {
        val uid = currentUserId() ?: return null
        return runCatching {
            clientManager.client.from("profiles")
                .select { filter { eq("id", uid) } }
                .decodeSingle<Profile>()
        }.getOrNull()
    }

    suspend fun isCurrentUserAdmin(): Boolean =
        fetchCurrentProfile()?.userRole?.isAdmin == true

    suspend fun currentUserRole(): UserRole =
        fetchCurrentProfile()?.userRole ?: UserRole.USER

    suspend fun fetchAllProfiles(): List<Profile> =
        clientManager.client.from("profiles")
            .select()
            .decodeList<Profile>()

    suspend fun setRole(userId: String, role: UserRole) {
        clientManager.client.from("profiles")
            .update({ this["role"] = role.dbValue }) {
                filter { eq("id", userId) }
            }
    }
}
