$sqlFile = "infrastructure/database/migrations/001_initial_schema.sql"

Get-Content $sqlFile | docker exec -i mx-postgres psql -U mx -d mx