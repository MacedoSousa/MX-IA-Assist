$sqlFile = "infrastructure/database/seeds/001_initial_seed.sql"

Get-Content $sqlFile | docker exec -i mx-postgres psql -U mx -d mx