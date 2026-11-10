-- =======================================================
-- ESQUEMA INICIAL DE BASE DE DATOS HATO (PostgreSQL / Supabase)
-- =======================================================

-- Habilitar extensión UUID si está disponible
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    phone VARCHAR(50),
    role VARCHAR(30) NOT NULL DEFAULT 'OWNER',
    avatar_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Fincas
CREATE TABLE IF NOT EXISTS farms (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    location VARCHAR(200) NOT NULL,
    total_area_ha REAL NOT NULL DEFAULT 0.0,
    owner_name VARCHAR(150) NOT NULL,
    center_lat DOUBLE PRECISION NOT NULL,
    center_lng DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla de Membresías (Acceso y roles por finca)
CREATE TABLE IF NOT EXISTS memberships (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    farm_id VARCHAR(64) NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    role VARCHAR(30) NOT NULL DEFAULT 'OWNER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_farm UNIQUE (user_id, farm_id)
);

-- 4. Tabla de Dispositivos GPS (Trackers)
CREATE TABLE IF NOT EXISTS trackers (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE, -- IMEI o MAC del dispositivo
    battery_percent INT NOT NULL DEFAULT 100,
    last_seen_at TIMESTAMP WITH TIME ZONE,
    report_interval_seconds INT NOT NULL DEFAULT 30,
    status VARCHAR(30) NOT NULL DEFAULT 'ONLINE'
);

-- 5. Tabla de Zonas / Geocercas
CREATE TABLE IF NOT EXISTS zones (
    id VARCHAR(64) PRIMARY KEY,
    farm_id VARCHAR(64) NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'POLYGON', -- POLYGON o CIRCLE
    points_json TEXT,                            -- Coordenadas [{lat, lng}, ...]
    center_lat DOUBLE PRECISION,
    center_lng DOUBLE PRECISION,
    radius_meters DOUBLE PRECISION,
    tolerance_meters REAL NOT NULL DEFAULT 10.0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    area_ha REAL NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Tabla de Animales (Ganado)
CREATE TABLE IF NOT EXISTS animals (
    id VARCHAR(64) PRIMARY KEY,
    farm_id VARCHAR(64) NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    tagNumber VARCHAR(50) NOT NULL,
    displayNumber VARCHAR(20) NOT NULL,
    breed VARCHAR(100) NOT NULL,
    age_years INT NOT NULL DEFAULT 1,
    weight_kg REAL,
    photo_url VARCHAR(500),
    notes TEXT,
    tracker_id VARCHAR(64),
    current_zone_id VARCHAR(64) REFERENCES zones(id) ON DELETE SET NULL,
    health_status VARCHAR(30) NOT NULL DEFAULT 'HEALTHY',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Tabla de Posiciones GPS (Historial e Ingesta en Tiempo Real)
CREATE TABLE IF NOT EXISTS positions (
    id BIGSERIAL PRIMARY KEY,
    tracker_id VARCHAR(64) NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    speed_kmh REAL DEFAULT 0.0,
    battery INT DEFAULT 100,
    accuracy_meters REAL DEFAULT 5.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tracker_recorded UNIQUE (tracker_id, recorded_at)
);

CREATE INDEX IF NOT EXISTS idx_positions_tracker_recorded ON positions(tracker_id, recorded_at DESC);

-- 8. Tabla de Alertas
CREATE TABLE IF NOT EXISTS alerts (
    id VARCHAR(64) PRIMARY KEY,
    farm_id VARCHAR(64) NOT NULL,
    animal_id VARCHAR(64) NOT NULL,
    animal_name VARCHAR(150),
    zone_id VARCHAR(64),
    zone_name VARCHAR(150),
    type VARCHAR(50) NOT NULL,
    severity VARCHAR(30) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    message TEXT NOT NULL,
    meters_out DOUBLE PRECISION,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by VARCHAR(64),
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_alerts_farm ON alerts(farm_id, created_at DESC);

-- 9. Tabla de Tokens de Dispositivos (FCM / Push)
CREATE TABLE IF NOT EXISTS device_tokens (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    platform VARCHAR(30) NOT NULL DEFAULT 'ANDROID',
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
