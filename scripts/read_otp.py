import sqlite3
import sys
from pathlib import Path

email = sys.argv[1] if len(sys.argv) > 1 else None
if not email:
    print('Usage: read_otp.py email@example.com')
    sys.exit(2)

db = Path.home() / '.pennywise' / 'pennywise.db'
if not db.exists():
    print('DB not found:', db)
    sys.exit(1)

conn = sqlite3.connect(str(db))
cur = conn.cursor()
try:
    cur.execute('SELECT email, otp_code, otp_expiry FROM users WHERE email = ?', (email,))
    r = cur.fetchone()
    if not r:
        print('User not found for', email)
    else:
        print('email:', r[0])
        print('otp_code:', r[1])
        print('otp_expiry:', r[2])
finally:
    conn.close()
