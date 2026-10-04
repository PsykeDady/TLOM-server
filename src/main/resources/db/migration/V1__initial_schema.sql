CREATE TABLE players (
    id VARCHAR(128) PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    locale VARCHAR(32) NOT NULL,
    avatar_body VARCHAR(128) NOT NULL,
    avatar_eyes VARCHAR(128) NOT NULL,
    avatar_top VARCHAR(128) NOT NULL,
    avatar_hair VARCHAR(128) NOT NULL,
    revision VARCHAR(64) NOT NULL
);

CREATE TABLE package_installation (
    id VARCHAR(255) PRIMARY KEY,
    player_id VARCHAR(128) NOT NULL REFERENCES players(id),
    package_id VARCHAR(255) NOT NULL,
    package_version VARCHAR(64) NOT NULL,
    configuration_json TEXT NOT NULL,
    CONSTRAINT uq_package_installation_player_package_version UNIQUE (player_id, package_id, package_version)
);

CREATE TABLE goal_occurrence (
    id VARCHAR(255) PRIMARY KEY,
    player_id VARCHAR(128) NOT NULL REFERENCES players(id),
    goal_id VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    CONSTRAINT ck_goal_occurrence_status CHECK (status IN ('PENDING', 'COMPLETED', 'SKIPPED'))
);

CREATE TABLE ledger_entry (
    id VARCHAR(255) PRIMARY KEY,
    player_id VARCHAR(128) NOT NULL REFERENCES players(id),
    currency_id VARCHAR(255) NOT NULL,
    amount INTEGER NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    source_id VARCHAR(255) NOT NULL,
    source_key VARCHAR(512) NOT NULL,
    CONSTRAINT uq_ledger_entry_player_source_key UNIQUE (player_id, source_key)
);

CREATE INDEX ix_ledger_entry_player_currency ON ledger_entry (player_id, currency_id);

CREATE TABLE purchase (
    id VARCHAR(255) PRIMARY KEY,
    player_id VARCHAR(128) NOT NULL REFERENCES players(id),
    request_id VARCHAR(255) NOT NULL,
    store_id VARCHAR(255) NOT NULL,
    store_item_id VARCHAR(255) NOT NULL,
    price_currency_id VARCHAR(255) NOT NULL,
    price_amount INTEGER NOT NULL CHECK (price_amount > 0),
    purchased_at VARCHAR(64) NOT NULL,
    CONSTRAINT uq_purchase_player_request_id UNIQUE (player_id, request_id)
);