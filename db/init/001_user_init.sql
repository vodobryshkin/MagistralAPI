-- Тип данных "Тип пользовательского аккаунта"
CREATE TYPE USER_TYPE AS ENUM(
    'INDIVIDUAL',
    'BUSINESS'
);

-- Таблица, в которой хранятся пользователи приложения
CREATE TABLE IF NOT EXISTS users(
    id UUID PRIMARY KEY,
    acc_type USER_TYPE NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT false,

    name VARCHAR(64) NOT NULL,
    surname VARCHAR(64) NOT NULL,
    fathers_name VARCHAR(64),

    email TEXT NOT NULL UNIQUE,
    phone VARCHAR(16) NOT NULL UNIQUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    password_hash BYTEA NOT NULL,
    password_hash_algorithm TEXT NOT NULL DEFAULT 'bcrypt',
    password_hash_key_id TEXT NOT NULL DEFAULT 'bcrypt_v1'
);

-- Функция, которая привязана к триггеру на обновление временной точки, когда был обновлён пользователь
CREATE OR REPLACE FUNCTION set_updated_at_user()
RETURNS trigger AS $$
BEGIN
NEW.updated_at := now();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_set_updated_at ON users;

-- Триггер на обновление временной точки, когда был обновлён пользователь, который срабатывает при обновлении пользователя
CREATE TRIGGER trg_users_set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_user();

-- Тип данных "Права пользователя в системе"
CREATE TYPE AUTHORITY AS ENUM (
    'ROLE_UNVERIFIED_USER',
    'ROLE_VERIFIED_USER',
    'ROLE_MODERATOR'
);

-- Таблица, в которой хранятся роли пользователя в приложении
CREATE TABLE roles(
    user_id UUID REFERENCES users(id) NOT NULL,
    role AUTHORITY NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Функция, которая привязана к триггеру на добавление роли неподтверждённого пользователя после добавления этого пользователя
CREATE OR REPLACE FUNCTION create_unverified_user_role()
RETURNS trigger AS $$
BEGIN
INSERT INTO roles(user_id, role)
    VALUES (NEW.ID, 'ROLE_UNVERIFIED_USER')
        ON CONFLICT DO NOTHING;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_create_unverified_user_role ON users;

-- Триггер на добавление роли неподтверждённого пользователя после добавления этого пользователя
CREATE TRIGGER trg_users_create_unverified_user_role
    AFTER INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION create_unverified_user_role();

-- Функция, которая привязана к триггеру на добавление роли подтверждённого пользователя после обновления поля verified с false на true
CREATE OR REPLACE FUNCTION add_verified_role_on_verified_true()
RETURNS trigger AS $$
BEGIN
INSERT INTO roles(user_id, role)
    VALUES (NEW.id, 'ROLE_VERIFIED_USER')
        ON CONFLICT DO NOTHING;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_roles_add_verified_on_verified_true ON users;

-- Триггер на добавление роли подтверждённого пользователя после обновления поля verified с false на true
CREATE TRIGGER trg_roles_add_verified_on_verified_true
    AFTER UPDATE OF verified ON users
    FOR EACH ROW
    WHEN (OLD.verified = false AND NEW.verified = true)
    EXECUTE FUNCTION add_verified_role_on_verified_true();
