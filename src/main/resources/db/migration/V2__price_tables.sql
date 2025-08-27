-- Schema: hotel_app

-- 1) Owner accounts (each hotel owner account belongs to a user, optional link to users table)
CREATE TABLE IF NOT EXISTS owner_accounts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,                      -- optional; link to users(id) later
  company_name VARCHAR(255),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2) Price tables (groups of hotels that should be compared together)
CREATE TABLE IF NOT EXISTS price_tables (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  city_label VARCHAR(120),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL,
  UNIQUE KEY u_price_table (owner_id, name),
  CONSTRAINT fk_pt_owner FOREIGN KEY (owner_id) REFERENCES owner_accounts(id)
);

-- 3) Join table (which hotels belong to a price table, mark one as the owner's hotel)
CREATE TABLE IF NOT EXISTS price_table_external_hotels (
  price_table_id BIGINT NOT NULL,
  external_hotel_ref BIGINT NOT NULL,   -- FK to external_hotels.id
  is_owner_hotel BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (price_table_id, external_hotel_ref),
  CONSTRAINT fk_pt FOREIGN KEY (price_table_id) REFERENCES price_tables(id),
  CONSTRAINT fk_pt_hotel FOREIGN KEY (external_hotel_ref) REFERENCES external_hotels(id)
);