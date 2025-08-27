ALTER TABLE users
  ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '$2a$10$dummydummydummydummydummydumMy',
  ADD COLUMN phone VARCHAR(30) NULL,
  ADD COLUMN status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING';

-- Optional: clear the dummy default so future inserts must set password_hash
ALTER TABLE users
  ALTER password_hash DROP DEFAULT;