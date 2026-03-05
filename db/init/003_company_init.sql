-- Таблица с юридическими лицами
CREATE TABLE IF NOT EXISTS companies(
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) NOT NULL UNIQUE,

    title TEXT NOT NULL,
    inn VARCHAR(10) UNIQUE NOT NULL,
    kpp VARCHAR(9) UNIQUE NOT NULL,
    okved VARCHAR(10),

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    agree_to_the_processing_of_courier_services BOOLEAN NOT NULL DEFAULT true,
    agree_to_the_processing_of_courier_services_since TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Индексы для быстрого поиска компании
CREATE INDEX IF NOT EXISTS idx_company_user_id ON companies(user_id);
CREATE INDEX IF NOT EXISTS idx_company_inn ON companies(inn);

-- Функция, которая привязана к триггеру на обновление временной точки, когда была обновлена компания.
CREATE OR REPLACE FUNCTION set_updated_at_companies()
    RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_companies_set_updated_at ON companies;

-- Триггер на обновление временной точки, когда была обновлена компания, который срабатывает при обновлении компании
CREATE TRIGGER trg_companies_set_updated_at
    BEFORE UPDATE ON companies
    FOR EACH ROW
EXECUTE FUNCTION set_updated_at_companies();