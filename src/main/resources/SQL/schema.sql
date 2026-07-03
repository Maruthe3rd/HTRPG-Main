CREATE TABLE IF NOT EXISTS meta_timeline_flags (
    flag_key TEXT PRIMARY KEY,
    flag_value BOOLEAN NOT NULL DEFAULT 0,
    description TEXT
);

CREATE TABLE IF NOT EXISTS playthrough_history (
    run_id INTEGER PRIMARY KEY AUTOINCREMENT,
    character_name TEXT NOT NULL,
    play_order INTEGER NOT NULL,
    achieved_end TEXT NOT NULL,
    run_completed BOOLEAN DEFAULT 0
);

CREATE TABLE IF NOT EXISTS paths_choices (
    choice_id TEXT PRIMARY KEY,
    character_who_made_it TEXT NOT NULL,
    choice_value TEXT NOT NULL,
    impacts_future_runs BOOLEAN DEFAULT 1
);

CREATE TABLE IF NOT EXISTS hero_state (
    hero_id TEXT PRIMARY KEY,
    current_health INTEGER DEFAULT 100,
    current_mana INTEGER DEFAULT 100,
    last_known_scene TEXT
);

CREATE TABLE IF NOT EXISTS relationships (
    npc_id TEXT PRIMARY KEY,
    affection_level INTEGER DEFAULT 0,
    met_in_previous_run BOOLEAN DEFAULT 0
);

CREATE TABLE IF NOT EXISTS inventory (
    item_id TEXT PRIMARY KEY,
    item_name TEXT NOT NULL,
    quantity INTEGER DEFAULT 1,
    transcends_timeline BOOLEAN DEFAULT 0
);