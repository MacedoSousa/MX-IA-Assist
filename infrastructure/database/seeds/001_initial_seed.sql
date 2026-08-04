-- MX Initial Seed
-- Version: 001


INSERT INTO users
(name, email, role)
VALUES

('Pedro', 'pedro@mx.local', 'owner'),

('Kevelin', 'kevelin@mx.local', 'owner'),

('MX Core', 'system@mx.local', 'system');



INSERT INTO workspaces
(name, type)
VALUES

('Pedro Workspace', 'personal'),

('Kevelin Workspace', 'personal'),

('Family Workspace', 'family'),

('MX Core Workspace', 'system');



INSERT INTO agents
(name, purpose, permissions)
VALUES

(
'MX Core',
'Orchestrates conversations and selects specialized agents.',
'{"level":"system"}'
),

(
'MX Developer',
'Software development, architecture and code review.',
'{"read":"projects","write":"suggestions"}'
),

(
'MX Tutor',
'Assists learning and university activities.',
'{"read":"knowledge","write":"notes"}'
),

(
'MX Finance',
'Helps organize financial information.',
'{"read":"financial_data","write":"reports"}'
),

(
'MX Planner',
'Organizes tasks, activities and priorities.',
'{"read":"calendar","write":"planning"}'
),

(
'MX Mail',
'Analyzes emails and prepares responses.',
'{"read":"emails","write":"drafts"}'
);