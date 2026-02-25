#!/bin/bash

# Configuration
CONTAINER_NAME="postgres_demo"
POSTGRES_USER="postgres"
POSTGRES_DB="postgres"
POSTGRES_PASSWORD="mysecretpassword"
DB_URL="https://edu.postgrespro.com/demo-medium-en.zip"
ZIP_FILE="demo-medium-en.zip"
SQL_FILE="demo-medium-en-20170815.sql"

echo "🚀 Starting PostgreSQL deployment with Podman..."

# 1. Pull and Start the PostgreSQL Container
podman run --name $CONTAINER_NAME \
  -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
  -p 5432:5432 \
  -d postgres:latest

echo "⏳ Waiting for database to initialize..."
sleep 10

# 2. Download the Demo Database
echo "📥 Downloading the 'Bookings' sample database..."
curl -O $DB_URL

# 3. Extract the SQL file
echo "📦 Extracting database files..."
unzip -o $ZIP_FILE

# 4. Copy the SQL file into the container
echo "🚛 Transferring SQL to container..."
podman cp $SQL_FILE $CONTAINER_NAME:/tmp/demo.sql

# 5. Restore the database
echo "🛠️ Restoring database (this may take a moment)..."
podman exec -it $CONTAINER_NAME psql -U $POSTGRES_USER -f /tmp/demo.sql

echo "✅ Success! You can now connect to the 'demo' database."
echo "Command: podman exec -it $CONTAINER_NAME psql -U $POSTGRES_USER -d demo"

# Clean up local files
rm $ZIP_FILE $SQL_FILE
