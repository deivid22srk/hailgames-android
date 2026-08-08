# Hailgames App — Progress & Session Notes

## Status: Backend READY, App coded, CI GREEN

### Supabase (DONE)
- Deleted all old orgs' projects (4 projects). **Orgs shells cannot be deleted via API** — only dashboard. Remaining empty org shells: `Bank`, `WinlatorHub`, `CompraFácil`, `Chat flutter`, `OmniTV`, `SimpsonsHit`. User can delete them in dashboard if desired.
- Created org `Hailgames` (`plxqxvynkifmrjfsbjqr`).
- Created project `hailgames` ref `tsriuhkellkwwaqficid` (region sa-east-1, free). Status ACTIVE_HEALTHY.
- Config in `supabase/local-config.txt` (NOT committed).

### Schema (DONE — `supabase/schema.sql`)
- `profiles` (id→auth.users, username, avatar_url, role: user/admin/owner)
- `categories` (seeded: Jogos, Apps, Mods, Emuladores, Outros)
- `content_items` (title, description, cover_url, category_id, link_url, file_url, download_url, author, version, size_mb, created_by, timestamps)
- Triggers: first user = **owner** (ADM principal); profile auto-created on signup.
- Helpers: `current_user_role()`, `is_admin()`, `is_owner()` (security definer).
- RLS:
  - content: SELECT all, INSERT/UPDATE/DELETE only admins.
  - categories: same as content.
  - profiles: SELECT all authenticated; UPDATE self (role stays user) or owner; INSERT self.
- Storage bucket `content` (public) with RLS: read public, write admins.
- Auth: email signup enabled.

### Design reference (Metrolist — DONE research, no code copied)
- Single-activity Compose, M3, dynamic color (dynamicLight/DarkColorScheme) + seed fallback via material-kolor.
- NavHost + bottom NavigationBar (adaptive NavigationRail on wide screens).
- LazyColumn home with cards; LazyColumn grid; detail hero + TopAppBar back.
- Patterns to emulate: seed-color theme fallback, tonal card highlighting, 8–12dp card radii, list rows 64dp.

### Android app (CODED — build verified via CI)
- Stack: Kotlin, Jetpack Compose, Material 3, Navigation Compose, Supabase-KTX, Coil, Gradle version catalog, single :app module.
- minSdk 26, target/compile SDK 36.
- Screens: Auth (login/signup), Home (grid + search + category filters), Content detail (hero, edit/delete), Settings (profile, Painel ADM entry, sign out), AdminPanel (CRUD content), AdminForm (create/edit), AdminManage (role management, owner only).
- Theme: M3 dynamic color + brand seed fallback.

### GitHub / CI (DONE — GREEN build)
- Repo: `deivid22srk/hailgames-android` (public). NOTE: `deivid22srk/hailgames` is a different project (HailGames Channel Hub website) — kept intact.
- `build.yml`: ubuntu-latest, JDK 17, setup-android, sdkmanager build-tools/platforms 36, `:app:assembleDebug`, upload APK artifact.
- Supabase anon key in buildConfigField (public anon key, RLS is the real protection). Secrets file `supabase/local-config.txt` NOT committed.
- Fixes needed for green build (Supabase 3.6 / Kotlin 2.3 API):
  - `kotlinOptions { jvmTarget }` → `kotlin { compilerOptions { jvmTarget } }`.
  - `decodeList`/`decodeSingle` are now **member functions** on `PostgrestResult` (no import needed).
  - `signUpWith { data }` expects `JsonObject` (`buildJsonObject { put(..., JsonPrimitive(...)) }`).
  - `httpEngine = OkHttp.create()` (Ktor 3 engine factory, not the object).
  - `SessionStatus`: `Initializing` replaces `Unknown`; sealed when must be exhaustive.
  - Compose: import `androidx.compose.foundation.lazy.items` for LazyRow (don't rely on grid.items); `ExposedDropdownMenu` is a scope member, not a top-level import.

## Next steps
1. Verify RLS/security against real Supabase API (auth flow, role checks, storage upload).
2. Install & smoke-test APK on device.
