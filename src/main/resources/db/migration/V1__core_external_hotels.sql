-- Schema: hotel_app  (remove USE if your connection already selects this DB)
USE hotel_app;

-- Core hotels table (one row per external hotel id/provider)
CREATE TABLE IF NOT EXISTS external_hotels (
  id                BIGINT PRIMARY KEY AUTO_INCREMENT,
  provider          VARCHAR(64) NOT NULL,           -- e.g. RAPIDAPI_BOOKING
  external_hotel_id VARCHAR(64) NOT NULL,           -- e.g. 1046167 (string to be safe)
  name_cached       VARCHAR(255),
  url_cached        VARCHAR(512),
  city_cached       VARCHAR(120),
  state_cached      VARCHAR(120),
  country_cached    VARCHAR(2),
  is_active         BOOLEAN NOT NULL DEFAULT TRUE,
  last_seen_at      TIMESTAMP NULL,
  UNIQUE KEY u_provider_hotel (provider, external_hotel_id)
);

-- Price snapshots (one row per check-in/out quote you fetch)
CREATE TABLE IF NOT EXISTS price_snapshots (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  provider           VARCHAR(64) NOT NULL,
  external_hotel_id  VARCHAR(64) NOT NULL,
  checkin_date       DATE NOT NULL,
  checkout_date      DATE NOT NULL,
  currency           CHAR(3) NOT NULL,
  price_total        DECIMAL(12,2) NOT NULL,
  price_per_night    DECIMAL(12,2) NULL,
  availability       VARCHAR(24) NOT NULL,          -- AVAILABLE | LIMITED | SOLD_OUT | NO_DATA
  source             VARCHAR(64) NULL,              -- e.g. RAPIDAPI
  fetched_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY k_lookup (provider, external_hotel_id, checkin_date, checkout_date),
  KEY k_fetched (fetched_at)
);