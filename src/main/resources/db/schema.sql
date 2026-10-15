-- TaskTier Database Schema for SQLite
-- Auto-created on startup by AppInitializer

CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL UNIQUE COLLATE NOCASE,
  display_name TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL DEFAULT 'USER',
  is_active INTEGER NOT NULL DEFAULT 1,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  last_login_at TEXT
);

CREATE TABLE IF NOT EXISTS categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL UNIQUE,
  description TEXT,
  color TEXT,
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS tasks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  title TEXT NOT NULL,
  description TEXT,
  category_id INTEGER,
  estimated_hours REAL,
  deadline TEXT,
  importance TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING',
  priority_score REAL,
  created_at TEXT NOT NULL DEFAULT (datetime('now')),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE IF NOT EXISTS time_entries (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  task_id INTEGER NOT NULL,
  start_time TEXT NOT NULL,
  end_time TEXT,
  duration_minutes INTEGER,
  notes TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (task_id) REFERENCES tasks(id)
);

-- Pre-seed Daily-Use Categories (INSERT OR IGNORE ensures idempotency)
INSERT OR IGNORE INTO categories (name, description, color) VALUES
  ('Personal', 'Personal errands & household tasks', '#10b981'),
  ('Work', 'Work-related tasks & meetings', '#3b82f6'),
  ('Study', 'Academic study & coursework', '#8b5cf6'),
  ('Health', 'Exercise, medical, wellbeing', '#ef4444'),
  ('Finance', 'Bills, budgets, investments', '#f59e0b'),
  ('Shopping', 'Groceries & purchases', '#ec4899'),
  ('Home', 'Cleaning, repairs, maintenance', '#14b8a6'),
  ('Projects', 'Long-term personal or professional projects', '#6366f1'),
  ('Meetings', 'Scheduled calls and syncs', '#06b6d4'),
  ('Errands', 'Quick outside tasks', '#84cc16');

-- Performance Indexes for Scalable Querying
CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_category_id ON tasks(category_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_deadline ON tasks(deadline);
CREATE INDEX IF NOT EXISTS idx_time_entries_user_id ON time_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_time_entries_task_id ON time_entries(task_id);

