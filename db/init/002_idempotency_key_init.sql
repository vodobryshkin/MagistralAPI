-- Тип данных "Статус состояния ключа идемпотентности"
CREATE TYPE IDEMPOTENCY_KEY_STATUS AS ENUM (
    'COMPLETED',
    'IN_PROGRESS'
);

CREATE TYPE HTTP_METHOD AS ENUM (
    'POST',
    'PATCH'
);

-- Таблица с ключами идемпотентности
CREATE TABLE IF NOT EXISTS idempotency_keys (
    id UUID PRIMARY KEY,
    status IDEMPOTENCY_KEY_STATUS NOT NULL,

    method HTTP_METHOD NOT NULL,
    path TEXT NOT NULL,
    request_hash BYTEA NOT NULL,
    request_hash_algorithm TEXT NOT NULL DEFAULT 'hmac-sha256',
    request_hash_key_id TEXT NOT NULL DEFAULT 'hmac-sha256-v1',

    response_status INTEGER,
    response_headers JSONB,
    response_body JSONB,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL DEFAULT (now() + interval '24 hours')
);

-- Функция, которая привязана к триггеру на обновление временной точки, когда был обновлён пользователь
CREATE OR REPLACE FUNCTION set_updated_at_idempotency_key()
RETURNS trigger AS $$
BEGIN
NEW.updated_at := now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_set_updated_at ON idempotency_keys;

-- Триггер на обновление временной точки, когда был обновлён пользователь, который срабатывает при обновлении пользователя
CREATE TRIGGER trg_users_set_updated_at
    BEFORE UPDATE ON idempotency_keys
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at_idempotency_key();
