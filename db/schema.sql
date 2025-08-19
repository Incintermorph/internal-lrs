
-- MySQL 8+
DROP TABLE IF EXISTS lrs_document;
DROP TABLE IF EXISTS lrs_statement;

CREATE TABLE lrs_statement (
  id CHAR(36) NOT NULL PRIMARY KEY,
  stored TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  timestamp_utc TIMESTAMP(6) NULL,
  authority JSON NULL,
  actor JSON NOT NULL,
  verb JSON NOT NULL,
  object JSON NOT NULL,
  result JSON NULL,
  context JSON NULL,
  attachments JSON NULL,
  full_statement JSON NOT NULL,
  voided TINYINT(1) NOT NULL DEFAULT 0,
  verb_id VARCHAR(500) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(verb, '$.id'))) STORED,
  actor_account_name VARCHAR(255) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(actor, '$.account.name'))) STORED,
  activity_id VARCHAR(1000) GENERATED ALWAYS AS (
    JSON_UNQUOTE(
      COALESCE(
        JSON_EXTRACT(object, '$.id'),
        JSON_EXTRACT(object, '$.object.id')
      )
    )
  ) STORED,
  registration CHAR(36) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(context, '$.registration'))) STORED
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_stmt_stored ON lrs_statement (stored);
CREATE INDEX idx_stmt_verb ON lrs_statement (verb_id(191));
CREATE INDEX idx_stmt_actor ON lrs_statement (actor_account_name);
CREATE INDEX idx_stmt_activity ON lrs_statement (activity_id(191));
CREATE INDEX idx_stmt_registration ON lrs_statement (registration);

CREATE TABLE lrs_document (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doc_type ENUM('state','activity_profile','agent_profile') NOT NULL,
  activity_id VARCHAR(1000) NULL,
  agent JSON NULL,
  agent_sha CHAR(64) NULL,
  registration CHAR(36) NULL,
  state_id VARCHAR(200) NULL,
  profile_id VARCHAR(200) NULL,
  content_type VARCHAR(255) NOT NULL,
  content LONGBLOB NOT NULL,
  etag CHAR(64) NOT NULL,
  updated TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uk_state (doc_type, activity_id(191), agent_sha, registration, state_id),
  UNIQUE KEY uk_activity_profile (doc_type, activity_id(191), profile_id),
  UNIQUE KEY uk_agent_profile (doc_type, agent_sha, profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
