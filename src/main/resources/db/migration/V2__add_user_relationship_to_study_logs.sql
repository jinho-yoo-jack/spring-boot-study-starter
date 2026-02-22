-- ============================================
-- V2: Add User Relationship to Study Logs
-- ============================================
-- Description: Add user_id column to study_logs table to establish relationship with users table
-- Author: Spring Boot Study Diary
-- Date: 2024
-- ============================================

-- Step 1: Add user_id column to study_logs table
-- Note: Initially nullable to allow migration of existing data
ALTER TABLE study_logs
ADD COLUMN user_id BIGINT;

-- Step 2: Create index on user_id for better query performance
CREATE INDEX idx_study_logs_user_id ON study_logs(user_id);

-- Step 3: Update existing records to set a default user_id (optional)
-- This assumes user with id=1 exists as default/admin user
-- You should adjust this based on your actual data
UPDATE study_logs
SET user_id = 1
WHERE user_id IS NULL;

-- Step 4: Make user_id NOT NULL after data migration
ALTER TABLE study_logs
MODIFY COLUMN user_id BIGINT NOT NULL;

-- Step 5: Add foreign key constraint to users table
ALTER TABLE study_logs
ADD CONSTRAINT fk_study_logs_user
FOREIGN KEY (user_id) REFERENCES users(id)
ON DELETE CASCADE;

-- Step 6: Add additional indexes for common queries
CREATE INDEX idx_study_logs_user_id_category ON study_logs(user_id, category);
CREATE INDEX idx_study_logs_user_id_study_date ON study_logs(user_id, study_date);

-- ============================================
-- Rollback Script (if needed):
-- ============================================
-- DROP INDEX idx_study_logs_user_id_study_date ON study_logs;
-- DROP INDEX idx_study_logs_user_id_category ON study_logs;
-- ALTER TABLE study_logs DROP FOREIGN KEY fk_study_logs_user;
-- DROP INDEX idx_study_logs_user_id ON study_logs;
-- ALTER TABLE study_logs DROP COLUMN user_id;
-- ============================================