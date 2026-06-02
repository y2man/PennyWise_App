# PennyWise — Personal Finance Manager

Spring Boot backend + JavaFX UI + SQLite database.

## Requirements
- **Java 21+** — https://adoptium.net
- **Maven 3.9+** — https://maven.apache.org

## Quick Start (Windows)
```
run.bat
```
The script will:
1. Check Java and Maven are installed
2. Build both backend and UI
3. Start the backend on port 8080
4. Launch the JavaFX UI automatically

## Manual Start
```bat
# Terminal 1 — Backend
cd backend
mvn spring-boot:run

# Wait for: Started BackendApplication on port 8080
# Then open Terminal 2 — UI

# Terminal 2 — UI
cd ui
mvn javafx:run
```

## First Run
- Database is auto-created at: `C:\Users\YOU\.pennywise\pennywise.db`
- Register a new account on first launch

## Email / OTP Setup (optional)
Create `backend/.env` from `backend/.env.example` and fill in your values there. The backend loads that file automatically when you run it from the `backend` folder.
Get a Gmail App Password: myaccount.google.com → Security → App Passwords

**If not configured:** The OTP code prints to the backend console window instead.

## Troubleshooting
| Problem | Fix |
|---------|-----|
| `java not found` | Install Java 21 from adoptium.net |
| `mvn not found` | Install Maven 3.9+ and add to PATH |
| `Port 8080 in use` | Close other apps using port 8080 |
| UI shows blank / can't connect | Make sure backend started fully first |
| Build fails first time | Check internet connection (Maven downloads deps) |

## Features
- Login / Signup with JWT auth
- Password reset via OTP (email or console)
- Dashboard with balance overview
- Income & Expense tracking
- Account types: Card, Cash, Savings
- Account transfers
- Budget management
- Savings goals
- Debt tracker
- Custom categories
- Analytics & charts
- Monthly filters
