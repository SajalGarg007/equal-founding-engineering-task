# Configuration Setup Guide

## 🔒 Security Best Practices

This project uses environment variables and local configuration files to protect sensitive credentials.

## Setup Instructions

### Option 1: Environment Variables (Recommended for Production)

Set these environment variables:
```bash
export DB_URL=jdbc:postgresql://localhost:5432/task_db
export DB_USERNAME=postgres
export DB_PASSWORD=your_password_here
```

On Windows (PowerShell):
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/task_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password_here"
```

### Option 2: Local Configuration File (Recommended for Development)

1. Copy the example file:
   ```bash
   cp application.properties.example application-local.properties
   ```

2. Edit `application-local.properties` and add your credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/task_db
   spring.datasource.username=postgres
   spring.datasource.password=your_actual_password
   ```

3. The `application-local.properties` file is automatically gitignored and will not be committed to version control.

## File Structure

- `application.properties` - Main config (committed to git, uses environment variables)
- `application.properties.example` - Template file (committed to git, no credentials)
- `application-local.properties` - Local overrides (gitignored, contains your credentials)

## Spring Profile Priority

Spring Boot loads properties in this order (later files override earlier ones):
1. `application.properties`
2. `application-{profile}.properties` (e.g., `application-local.properties`)

## Important Notes

⚠️ **Never commit files with real credentials to git!**
- `application-local.properties` is automatically gitignored
- Always use environment variables in production
- Use secrets management tools (AWS Secrets Manager, HashiCorp Vault) for production deployments

