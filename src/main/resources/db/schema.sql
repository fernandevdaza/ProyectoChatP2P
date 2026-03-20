PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS "peers" (
  "id" TEXT PRIMARY KEY,
  "display_name" TEXT NOT NULL,
  "is_self" INTEGER NOT NULL DEFAULT 0, -- 1 para true
  "last_ip_addr" TEXT,
  "last_port" INTEGER,
  "last_seen_at" DATETIME,
  "theme_id" INTEGER NOT NULL DEFAULT 1,
  "created_at" DATETIME NOT NULL,
  "updated_at" DATETIME
);

CREATE TABLE IF NOT EXISTS "conversations" (
  "id" TEXT PRIMARY KEY,
  "type" TEXT NOT NULL CHECK ("type" IN ('DIRECT', 'GROUP')),
  "title" TEXT,
  "created_at" DATETIME NOT NULL,
  "updated_at" DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS "direct_participants" (
  "conversation_id" TEXT PRIMARY KEY,
  "peer_id" TEXT NOT NULL,
  FOREIGN KEY ("conversation_id") REFERENCES "conversations" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("peer_id") REFERENCES "peers" ("id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "group_memberships" (
  "conversation_id" TEXT NOT NULL,
  "peer_id" TEXT NOT NULL,
  "role" TEXT NOT NULL DEFAULT 'MEMBER' CHECK ("role" IN ('MEMBER', 'ADMIN')),
  "joined_at" DATETIME NOT NULL,
  PRIMARY KEY ("conversation_id", "peer_id"),
  FOREIGN KEY ("conversation_id") REFERENCES "conversations" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("peer_id") REFERENCES "peers" ("id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "messages" (
  "id" TEXT PRIMARY KEY,
  "conversation_id" TEXT NOT NULL,
  "sender_peer_id" TEXT NOT NULL,
  "type" TEXT NOT NULL CHECK ("type" IN ('AUDIO', 'TEXT', 'IMAGE', 'SYSTEM')),
  "text_content" TEXT,
  "sent_at" DATETIME NOT NULL,
  "received_at" DATETIME NOT NULL,
  "is_ephemeral" INTEGER NOT NULL DEFAULT 0,
  "is_fixed" INTEGER NOT NULL DEFAULT 0,
  "is_image" INTEGER NOT NULL DEFAULT 0,
  "expires_at" DATETIME,
  "status" TEXT NOT NULL DEFAULT 'RECEIVED' CHECK ("status" IN ('PENDING', 'SENT', 'DELIVERED', 'READ', 'RECEIVED', 'FAILED')),
  "created_at" DATETIME NOT NULL,
  "updated_at" DATETIME NOT NULL,
  FOREIGN KEY ("conversation_id") REFERENCES "conversations" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("sender_peer_id") REFERENCES "peers" ("id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "attachments" (
  "id" TEXT PRIMARY KEY,
  "message_id" TEXT NOT NULL,
  "mime_type" TEXT NOT NULL,
  "file_name" TEXT,
  "size_bytes" INTEGER,
  "sha256" TEXT,
  "storage_type" TEXT NOT NULL CHECK ("storage_type" IN ('BLOB', 'BASE64', 'FILE')),
  "data_blob" BLOB,
  "data_base64" TEXT,
  "file_path" TEXT,
  "created_at" DATETIME NOT NULL,
  "updated_at" DATETIME NOT NULL,
  FOREIGN KEY ("message_id") REFERENCES "messages" ("id") ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS "message_receipts" (
  "message_id" TEXT NOT NULL,
  "peer_id" TEXT NOT NULL,
  "status" TEXT NOT NULL CHECK ("status" IN ('DELIVERED', 'READ')),
  "at_time" DATETIME NOT NULL,
  PRIMARY KEY ("message_id", "peer_id"),
  FOREIGN KEY ("message_id") REFERENCES "messages" ("id") ON DELETE CASCADE,
  FOREIGN KEY ("peer_id") REFERENCES "peers" ("id") ON DELETE CASCADE
);



CREATE INDEX IF NOT EXISTS "idx_peers_last_seen" ON "peers" ("last_seen_at");
CREATE INDEX IF NOT EXISTS "idx_conversations_type" ON "conversations" ("type");
CREATE INDEX IF NOT EXISTS "idx_direct_participants_peer" ON "direct_participants" ("peer_id");
CREATE INDEX IF NOT EXISTS "idx_group_memberships_peer" ON "group_memberships" ("peer_id");
CREATE INDEX IF NOT EXISTS "idx_messages_conversation_time" ON "messages" ("conversation_id", "sent_at");
CREATE INDEX IF NOT EXISTS "idx_messages_sender" ON "messages" ("sender_peer_id");
CREATE INDEX IF NOT EXISTS "idx_messages_expires" ON "messages" ("expires_at");
CREATE INDEX IF NOT EXISTS "idx_attachments_message" ON "attachments" ("message_id");
CREATE INDEX IF NOT EXISTS "idx_receipts_message" ON "message_receipts" ("message_id");

CREATE UNIQUE INDEX IF NOT EXISTS idx_peers_only_one_self ON peers(is_self) WHERE is_self = 1;