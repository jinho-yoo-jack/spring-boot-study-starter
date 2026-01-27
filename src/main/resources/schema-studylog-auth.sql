-- Add userId column to study_logs table
ALTER TABLE study_logs ADD COLUMN user_id BIGINT;

-- Add foreign key constraint linking study_logs to users table
ALTER TABLE study_logs ADD CONSTRAINT fk_study_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add index on user_id for better query performance
CREATE INDEX idx_study_logs_user_id ON study_logs(user_id);

-- Add composite index for user-specific queries with study_date
CREATE INDEX idx_study_logs_user_date ON study_logs(user_id, study_date DESC);
