-- ============================================================
-- V4: Admin authentication.
-- Deliberately NO seed row here - a default admin account is created at
-- application startup (see DefaultAdminInitializer) from the
-- ADMIN_USERNAME / ADMIN_PASSWORD environment variables, so no password
-- hash - not even a placeholder one - ever ends up committed to source
-- control.
-- ============================================================

CREATE TABLE admins (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(100)    NOT NULL,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    deleted         BOOLEAN         NOT NULL DEFAULT FALSE,
    deleted_at      DATETIME        NULL,
    CONSTRAINT uq_admin_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
