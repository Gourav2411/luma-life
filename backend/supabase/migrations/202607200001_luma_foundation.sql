create extension if not exists pgcrypto;

create type public.life_area as enum (
  'academics', 'skills', 'health', 'relationships', 'fun', 'self_direction'
);
create type public.plan_horizon as enum ('direction', 'semester', 'week', 'day', 'session');
create type public.plan_state as enum (
  'draft', 'active', 'completed', 'skipped', 'rescheduled', 'archived'
);
create type public.flexibility as enum ('fixed', 'flexible', 'protected');
create type public.plan_source as enum ('user', 'calendar', 'luma');
create type public.proposal_state as enum ('draft', 'approved', 'partially_approved', 'rejected');

create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default '',
  locale text not null default 'en-IN',
  timezone text not null default 'Asia/Kolkata',
  college_stage text not null default '',
  sleep_start_minute integer not null default 1350 check (sleep_start_minute between 0 and 1439),
  sleep_end_minute integer not null default 390 check (sleep_end_minute between 0 and 1439),
  preferred_focus_minutes integer not null default 45 check (preferred_focus_minutes between 10 and 180),
  sync_version bigint not null default 0,
  consent_version text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.life_area_preferences (
  user_id uuid not null references public.profiles(id) on delete cascade,
  area public.life_area not null,
  protection_weight integer not null default 50 check (protection_weight between 0 and 100),
  is_protected boolean not null default false,
  primary key (user_id, area)
);

create table public.directions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  statement text not null,
  horizon_months integer not null default 12 check (horizon_months between 6 and 12),
  life_areas public.life_area[] not null default '{}',
  state public.plan_state not null default 'active',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.semester_outcomes (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  direction_id uuid references public.directions(id) on delete set null,
  semester_label text not null,
  semester_start date not null,
  semester_end date not null,
  title text not null,
  life_area public.life_area not null,
  evidence_target text not null default '',
  progress_percent integer not null default 0 check (progress_percent between 0 and 100),
  state public.plan_state not null default 'active',
  check (semester_end >= semester_start)
);

create table public.goals (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  parent_goal_id uuid references public.goals(id) on delete set null,
  semester_outcome_id uuid references public.semester_outcomes(id) on delete set null,
  title text not null,
  life_area public.life_area not null,
  target_date date,
  state public.plan_state not null default 'active',
  created_at timestamptz not null default now()
);

create table public.skills (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  goal_id uuid references public.goals(id) on delete set null,
  title text not null,
  target_role text not null default '',
  progress_percent integer not null default 0 check (progress_percent between 0 and 100),
  state public.plan_state not null default 'active'
);

create table public.projects (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  skill_id uuid references public.skills(id) on delete set null,
  title text not null,
  outcome text not null default '',
  state public.plan_state not null default 'draft',
  created_at timestamptz not null default now()
);

create table public.plan_items (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  parent_goal_id uuid references public.goals(id) on delete set null,
  title text not null,
  date date not null,
  start_minute integer check (start_minute between 0 and 1439),
  duration_minutes integer not null check (duration_minutes between 1 and 720),
  life_area public.life_area not null,
  flexibility public.flexibility not null,
  source public.plan_source not null,
  state public.plan_state not null default 'draft',
  energy text not null check (energy in ('low', 'steady', 'high', 'overwhelmed')),
  travel_before_minutes integer not null default 0 check (travel_before_minutes between 0 and 240),
  travel_after_minutes integer not null default 0 check (travel_after_minutes between 0 and 240),
  recurrence_rule jsonb,
  external_event_id text,
  external_etag text,
  client_mutation_id uuid,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (user_id, client_mutation_id)
);
create index plan_items_day_idx on public.plan_items(user_id, date, start_minute);
create unique index plan_items_external_event_idx
  on public.plan_items(user_id, external_event_id)
  where external_event_id is not null;

create table public.week_plans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  start_date date not null,
  priorities jsonb not null default '[]',
  capacity_minutes integer not null check (capacity_minutes >= 0),
  committed_minutes integer not null default 0 check (committed_minutes >= 0),
  sync_version bigint not null default 0,
  unique (user_id, start_date)
);

create table public.day_plans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  date date not null,
  top_outcome_ids uuid[] not null default '{}',
  energy text not null default 'steady',
  capacity_minutes integer not null default 480,
  sync_version bigint not null default 0,
  unique (user_id, date)
);

create table public.focus_sessions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  plan_item_id uuid not null references public.plan_items(id) on delete cascade,
  outcome text not null,
  duration_minutes integer not null check (duration_minutes between 1 and 240),
  microsteps jsonb not null default '[]',
  blockers jsonb not null default '[]',
  completion_evidence_id uuid,
  started_at timestamptz,
  completed_at timestamptz
);

create table public.evidence (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  project_id uuid references public.projects(id) on delete set null,
  kind text not null check (kind in ('link', 'text', 'file', 'reflection')),
  label text not null,
  value text not null,
  created_at timestamptz not null default now()
);

alter table public.focus_sessions
  add constraint focus_completion_evidence_fk
  foreign key (completion_evidence_id) references public.evidence(id) on delete set null;

create table public.reflections (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  date date not null,
  cadence text not null check (cadence in ('day', 'week')),
  energy text not null,
  worked_well text not null default '',
  needs_adjustment text not null default '',
  created_at timestamptz not null default now()
);

create table public.conversations (
  id uuid primary key,
  user_id uuid not null references public.profiles(id) on delete cascade,
  retention_until timestamptz not null default (now() + interval '30 days'),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.conversation_messages (
  id uuid primary key default gen_random_uuid(),
  conversation_id uuid not null references public.conversations(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  role text not null check (role in ('user', 'assistant')),
  content text not null,
  trace_id uuid,
  created_at timestamptz not null default now()
);

create table public.memory_facts (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  category text not null,
  statement text not null,
  is_confirmed boolean not null default false,
  is_sensitive boolean not null default false,
  source_conversation_id uuid references public.conversations(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.plan_proposals (
  id uuid primary key,
  user_id uuid not null references public.profiles(id) on delete cascade,
  conversation_id uuid references public.conversations(id) on delete set null,
  horizon public.plan_horizon not null,
  summary text not null,
  assumptions jsonb not null default '[]',
  unresolved_questions jsonb not null default '[]',
  confidence numeric(4,3) not null check (confidence between 0 and 1),
  is_feasible boolean not null,
  validation_messages jsonb not null default '[]',
  state public.proposal_state not null default 'draft',
  proposal_json jsonb not null,
  decided_at timestamptz,
  created_at timestamptz not null default now()
);

create table public.proposal_decisions (
  id uuid primary key default gen_random_uuid(),
  proposal_id uuid not null references public.plan_proposals(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  decision text not null check (decision in ('approve', 'reject', 'partial')),
  accepted_change_ids uuid[] not null default '{}',
  expected_sync_version bigint not null,
  created_at timestamptz not null default now()
);

create table public.calendar_connections (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  provider text not null check (provider in ('google')),
  encrypted_refresh_token text not null,
  sync_cursor text,
  revoked_at timestamptz,
  created_at timestamptz not null default now(),
  unique (user_id, provider)
);

create table public.calendar_outbox (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.profiles(id) on delete cascade,
  proposal_id uuid not null references public.plan_proposals(id) on delete cascade,
  change_id uuid not null,
  idempotency_key text not null unique,
  payload jsonb not null,
  state text not null default 'pending' check (state in ('pending', 'processing', 'applied', 'failed', 'cancelled')),
  attempts integer not null default 0,
  last_error text,
  created_at timestamptz not null default now(),
  applied_at timestamptz
);

create table public.audit_records (
  id bigint generated always as identity primary key,
  user_id uuid not null references public.profiles(id) on delete cascade,
  action text not null,
  entity_type text not null,
  entity_id uuid,
  detail jsonb not null default '{}',
  trace_id uuid,
  created_at timestamptz not null default now()
);

create or replace function public.create_profile_for_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  insert into public.profiles (id, display_name)
  values (new.id, coalesce(new.raw_user_meta_data ->> 'full_name', ''));
  insert into public.life_area_preferences (user_id, area, protection_weight, is_protected)
  select new.id, value, 50, value in ('health', 'relationships', 'fun')
  from unnest(enum_range(null::public.life_area)) value;
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.create_profile_for_new_user();

create or replace function public.apply_plan_proposal(
  p_user_id uuid,
  p_proposal_id uuid,
  p_accepted_change_ids uuid[],
  p_expected_sync_version bigint,
  p_decision text
) returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  v_proposal public.plan_proposals%rowtype;
  v_current_version bigint;
  v_change jsonb;
  v_change_id uuid;
  v_after jsonb;
  v_before jsonb;
  v_outbox_ids uuid[] := '{}';
  v_outbox_id uuid;
  v_conflict_count integer;
begin
  select * into v_proposal
  from public.plan_proposals
  where id = p_proposal_id and user_id = p_user_id
  for update;
  if not found then raise exception 'proposal_not_found'; end if;
  if v_proposal.state <> 'draft' then raise exception 'proposal_already_decided'; end if;

  select sync_version into v_current_version
  from public.profiles where id = p_user_id for update;
  if v_current_version <> p_expected_sync_version then raise exception 'stale_sync_version'; end if;

  if p_decision = 'reject' then
    update public.plan_proposals set state = 'rejected', decided_at = now() where id = p_proposal_id;
    insert into public.proposal_decisions
      (proposal_id, user_id, decision, accepted_change_ids, expected_sync_version)
    values (p_proposal_id, p_user_id, 'reject', '{}', p_expected_sync_version);
    return jsonb_build_object(
      'plan', jsonb_build_object('sync_version', v_current_version),
      'sync', jsonb_build_object('state', 'not_required', 'outbox_ids', '[]'::jsonb)
    );
  end if;

  if not v_proposal.is_feasible then raise exception 'proposal_not_feasible'; end if;
  if p_decision not in ('approve', 'partial') then raise exception 'invalid_decision'; end if;

  for v_change in select value from jsonb_array_elements(v_proposal.proposal_json -> 'changes')
  loop
    v_change_id := (v_change ->> 'id')::uuid;
    if p_accepted_change_ids is not null and not (v_change_id = any(p_accepted_change_ids)) then
      continue;
    end if;
    v_after := v_change -> 'after';
    v_before := v_change -> 'before';

    if v_change ->> 'action' = 'delete' then
      update public.plan_items
      set state = 'archived', updated_at = now()
      where id = (v_change ->> 'target_id')::uuid and user_id = p_user_id;
    elsif v_change ->> 'action' in ('create', 'move', 'resize', 'protect') then
      insert into public.plan_items (
        id, user_id, parent_goal_id, title, date, start_minute, duration_minutes,
        life_area, flexibility, source, state, energy,
        travel_before_minutes, travel_after_minutes, updated_at
      ) values (
        (v_after ->> 'id')::uuid,
        p_user_id,
        nullif(v_after ->> 'parent_goal_id', '')::uuid,
        v_after ->> 'title',
        (v_after ->> 'date')::date,
        nullif(v_after ->> 'start_minute', '')::integer,
        (v_after ->> 'duration_minutes')::integer,
        (v_after ->> 'life_area')::public.life_area,
        (v_after ->> 'flexibility')::public.flexibility,
        (v_after ->> 'source')::public.plan_source,
        'active',
        v_after ->> 'energy',
        coalesce((v_after ->> 'travel_before_minutes')::integer, 0),
        coalesce((v_after ->> 'travel_after_minutes')::integer, 0),
        now()
      )
      on conflict (id) do update set
        title = excluded.title,
        date = excluded.date,
        start_minute = excluded.start_minute,
        duration_minutes = excluded.duration_minutes,
        life_area = excluded.life_area,
        flexibility = excluded.flexibility,
        state = 'active',
        energy = excluded.energy,
        travel_before_minutes = excluded.travel_before_minutes,
        travel_after_minutes = excluded.travel_after_minutes,
        updated_at = now()
      where public.plan_items.user_id = p_user_id;
    end if;

    if coalesce(v_before ->> 'source', v_after ->> 'source') = 'calendar' then
      insert into public.calendar_outbox (
        user_id, proposal_id, change_id, idempotency_key, payload
      ) values (
        p_user_id,
        p_proposal_id,
        v_change_id,
        p_proposal_id::text || ':' || v_change_id::text,
        v_change
      )
      returning id into v_outbox_id;
      v_outbox_ids := array_append(v_outbox_ids, v_outbox_id);
    end if;
  end loop;

  select count(*) into v_conflict_count
  from public.plan_items first_item
  join public.plan_items second_item
    on first_item.user_id = second_item.user_id
   and first_item.date = second_item.date
   and first_item.id < second_item.id
   and first_item.start_minute is not null
   and second_item.start_minute is not null
   and first_item.state not in ('archived', 'skipped')
   and second_item.state not in ('archived', 'skipped')
   and int4range(
         first_item.start_minute - first_item.travel_before_minutes,
         first_item.start_minute + first_item.duration_minutes + first_item.travel_after_minutes,
         '[)'
       ) &&
       int4range(
         second_item.start_minute - second_item.travel_before_minutes,
         second_item.start_minute + second_item.duration_minutes + second_item.travel_after_minutes,
         '[)'
       )
  where first_item.user_id = p_user_id;
  if v_conflict_count > 0 then raise exception 'hard_constraint_conflict'; end if;

  update public.profiles
  set sync_version = sync_version + 1, updated_at = now()
  where id = p_user_id
  returning sync_version into v_current_version;

  update public.plan_proposals
  set state = case when p_decision = 'partial' then 'partially_approved' else 'approved' end,
      decided_at = now()
  where id = p_proposal_id;

  insert into public.proposal_decisions
    (proposal_id, user_id, decision, accepted_change_ids, expected_sync_version)
  values (
    p_proposal_id,
    p_user_id,
    p_decision,
    coalesce(
      p_accepted_change_ids,
      array(
        select (value ->> 'id')::uuid
        from jsonb_array_elements(v_proposal.proposal_json -> 'changes')
      )
    ),
    p_expected_sync_version
  );
  insert into public.audit_records (user_id, action, entity_type, entity_id, detail)
  values (
    p_user_id,
    p_decision,
    'plan_proposal',
    p_proposal_id,
    jsonb_build_object('accepted_change_ids', p_accepted_change_ids, 'sync_version', v_current_version)
  );

  return jsonb_build_object(
    'plan', jsonb_build_object(
      'sync_version', v_current_version,
      'items', (
        select coalesce(jsonb_agg(to_jsonb(item) order by item.date, item.start_minute), '[]'::jsonb)
        from public.plan_items item
        where item.user_id = p_user_id and item.state <> 'archived'
      )
    ),
    'sync', jsonb_build_object(
      'state', case when cardinality(v_outbox_ids) > 0 then 'queued' else 'not_required' end,
      'outbox_ids', to_jsonb(v_outbox_ids)
    )
  );
end;
$$;

create or replace function public.get_plan_hierarchy(
  p_user_id uuid,
  p_horizon text,
  p_anchor date
) returns jsonb
language sql
security definer
set search_path = public
as $$
  select jsonb_build_object(
    'horizon', p_horizon,
    'anchor', p_anchor,
    'sync_version', profile.sync_version,
    'capacity_minutes', coalesce(day.capacity_minutes, 480),
    'items', coalesce(
      (
        select jsonb_agg(to_jsonb(item) order by item.start_minute)
        from public.plan_items item
        where item.user_id = p_user_id
          and item.date = p_anchor
          and item.state <> 'archived'
      ),
      '[]'::jsonb
    ),
    'conflicts', '[]'::jsonb
  )
  from public.profiles profile
  left join public.day_plans day on day.user_id = profile.id and day.date = p_anchor
  where profile.id = p_user_id;
$$;

do $$
declare table_name text;
begin
  foreach table_name in array array[
    'life_area_preferences', 'directions', 'semester_outcomes', 'goals',
    'skills', 'projects', 'plan_items', 'week_plans', 'day_plans', 'focus_sessions',
    'evidence', 'reflections', 'conversations', 'conversation_messages', 'memory_facts',
    'plan_proposals', 'proposal_decisions', 'calendar_connections', 'calendar_outbox',
    'audit_records'
  ]
  loop
    execute format('alter table public.%I enable row level security', table_name);
    execute format(
      'create policy %I on public.%I for all using (auth.uid() = user_id) with check (auth.uid() = user_id)',
      table_name || '_owner',
      table_name
    );
  end loop;
end;
$$;

alter table public.profiles enable row level security;
create policy profiles_owner on public.profiles
  for all using (auth.uid() = id) with check (auth.uid() = id);

revoke all on function public.apply_plan_proposal(uuid, uuid, uuid[], bigint, text) from public;
revoke all on function public.get_plan_hierarchy(uuid, text, date) from public;
grant execute on function public.apply_plan_proposal(uuid, uuid, uuid[], bigint, text) to service_role;
grant execute on function public.get_plan_hierarchy(uuid, text, date) to service_role;
