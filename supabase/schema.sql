-- ============================================================
-- Hailgames Community App - Supabase Schema
-- Roles: user (common) | admin (ADM) | owner (ADM principal)
-- ============================================================

-- ---------- profiles ----------

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  username text not null default 'player',
  email text,
  avatar_url text,
  role text not null default 'user' check (role in ('user', 'admin', 'owner')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- auto-create profile on signup; FIRST registered user becomes the owner (ADM principal)
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  is_first boolean;
begin
  select not exists (select 1 from public.profiles) into is_first;
  insert into public.profiles (id, username, email, role)
  values (
    new.id,
    coalesce(new.raw_user_meta_data->>'username', split_part(coalesce(new.email, 'player'), '@', 1)),
    new.email,
    case when is_first then 'owner' else 'user' end
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- keep username updated from auth metadata
create or replace function public.sync_profile_username()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.profiles
  set username = coalesce(new.raw_user_meta_data->>'username', split_part(coalesce(new.email, 'player'), '@', 1)),
      email = new.email,
      updated_at = now()
  where id = new.id;
  return new;
end;
$$;

drop trigger if exists on_auth_user_updated on auth.users;
create trigger on_auth_user_updated
  after update of raw_user_meta_data, email on auth.users
  for each row execute function public.sync_profile_username();

-- ---------- categories ----------

create table if not exists public.categories (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  icon text,
  sort_order int not null default 0,
  created_at timestamptz not null default now()
);

-- ---------- content (games & items) ----------

create table if not exists public.content_items (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  description text,
  cover_url text,
  category_id uuid references public.categories(id) on delete set null,
  link_url text,
  file_url text,
  download_url text,
  author text,
  version text,
  size_mb numeric,
  created_by uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists content_items_category_idx on public.content_items(category_id);
create index if not exists content_items_created_at_idx on public.content_items(created_at desc);

-- ---------- helper functions (security definer to avoid RLS recursion) ----------

create or replace function public.current_user_role()
returns text
language sql
stable
security definer
set search_path = public
as $$
  select role from public.profiles where id = auth.uid();
$$;

create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select coalesce(public.current_user_role() in ('admin', 'owner'), false);
$$;

create or replace function public.is_owner()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select coalesce(public.current_user_role() = 'owner', false);
$$;

-- ---------- RLS ----------

alter table public.profiles enable row level security;
alter table public.categories enable row level security;
alter table public.content_items enable row level security;

-- profiles: any authenticated user can read the profile list (needed to find admins),
-- but role changes are restricted to the owner (see update policy).
drop policy if exists profiles_select on public.profiles;
create policy profiles_select on public.profiles
  for select using (auth.role() = 'authenticated');

drop policy if exists profiles_update on public.profiles;
create policy profiles_update on public.profiles
  for update using (
    -- owner can promote/demote any admin
    public.is_owner()
    -- a user can update their own profile BUT may never change the role column
    or (auth.uid() = id and public.current_user_role() is not null)
  )
  with check (
    (public.is_owner())
    or
    (auth.uid() = id and role = 'user')
  );

drop policy if exists profiles_insert on public.profiles;
create policy profiles_insert on public.profiles
  for insert with check (auth.uid() = id);

-- categories: readable by everyone, writable only by admins
drop policy if exists categories_select on public.categories;
create policy categories_select on public.categories
  for select using (true);

drop policy if exists categories_insert on public.categories;
create policy categories_insert on public.categories
  for insert with check (public.is_admin());

drop policy if exists categories_update on public.categories;
create policy categories_update on public.categories
  for update using (public.is_admin()) with check (public.is_admin());

drop policy if exists categories_delete on public.categories;
create policy categories_delete on public.categories
  for delete using (public.is_admin());

-- content_items: readable by everyone, writable only by admins
drop policy if exists content_select on public.content_items;
create policy content_select on public.content_items
  for select using (true);

drop policy if exists content_insert on public.content_items;
create policy content_insert on public.content_items
  for insert with check (public.is_admin());

drop policy if exists content_update on public.content_items;
create policy content_update on public.content_items
  for update using (public.is_admin()) with check (public.is_admin());

drop policy if exists content_delete on public.content_items;
create policy content_delete on public.content_items
  for delete using (public.is_admin());

-- ---------- seed categories ----------

insert into public.categories (name, icon, sort_order) values
  ('Jogos', 'gamepad', 1),
  ('Apps', 'apps', 2),
  ('Mods', 'extension', 3),
  ('Emuladores', 'devices', 4),
  ('Outros', 'category', 5)
on conflict (name) do nothing;
