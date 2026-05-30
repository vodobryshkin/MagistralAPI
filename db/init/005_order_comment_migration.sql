-- Миграция для уже существующих баз: заменяем признак "секретный груз" на произвольный комментарий к заказу.
-- Скрипт идемпотентен и безопасен для повторного запуска, а также для свежих баз, где таблица orders уже создана с колонкой comment.

ALTER TABLE orders ADD COLUMN IF NOT EXISTS comment VARCHAR(1024);

ALTER TABLE orders DROP COLUMN IF EXISTS secret_cargo;
