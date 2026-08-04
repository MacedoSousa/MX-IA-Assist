$migrationsPath = "infrastructure/database/migrations"


# Bootstrap migration control

docker exec mx-postgres `
psql -U mx -d mx `
-c "CREATE TABLE IF NOT EXISTS schema_migrations (
id SERIAL PRIMARY KEY,
filename VARCHAR(255) UNIQUE NOT NULL,
executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);"



$migrations = Get-ChildItem $migrationsPath -Filter *.sql | Sort-Object Name



foreach ($migration in $migrations) {


    $exists = docker exec mx-postgres `
    psql -U mx -d mx -tAc `
    "SELECT COUNT(*) FROM schema_migrations WHERE filename='$($migration.Name)';"



    if ($exists.Trim() -eq "0") {


        Write-Host "Applying $($migration.Name)..."


        Get-Content $migration.FullName |
        docker exec -i mx-postgres psql -U mx -d mx



        docker exec mx-postgres `
        psql -U mx -d mx `
        -c "INSERT INTO schema_migrations(filename) VALUES('$($migration.Name)');"


    }
    else {


        Write-Host "Skipping $($migration.Name)"


    }

}