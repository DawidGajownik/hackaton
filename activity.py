import os
import time
import sqlite3
import threading
import json
import firebase_admin
from firebase_admin import credentials, messaging
from flask import Flask, request, jsonify
from functools import wraps 

# --- 1. INITIALIZATION and DATABASE SETUP ---

app = Flask(__name__)

SECRET_API_KEY = "abcde12345"

# Initialize Firebase Admin SDK
cred_path = os.path.join(os.path.dirname(__file__), "service-account-key.json")
cred = credentials.Certificate(cred_path)
firebase_admin.initialize_app(cred)

DATABASE_NAME = 'activity.db'

def require_api_key(f):
    """Decorator to ensure a valid API key is present in the request header."""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Check if 'X-API-Key' header is in the request
        if 'X-API-Key' in request.headers and request.headers['X-API-Key'] == SECRET_API_KEY:
            # If the key is valid, proceed with the original function
            return f(*args, **kwargs)
        else:
            # If the key is missing or invalid, return a 403 Forbidden error
            return jsonify({"error": "Unauthorized access"}), 403
    return decorated_function

def get_db_connection():
    """Establishes a connection to the SQLite database."""
    conn = sqlite3.connect(DATABASE_NAME)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    """Initializes the database schema if it doesn't exist."""
    with get_db_connection() as conn:
        cursor = conn.cursor()
        # Users table: stores the last seen time and notification status
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS users (
                fcm_token TEXT PRIMARY KEY,
                last_seen_timestamp INTEGER NOT NULL,
                notified_for_inactivity BOOLEAN NOT NULL DEFAULT 0
            )
        ''')
        # Events table: a log of every action received
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_token TEXT NOT NULL,
                event_type TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                metadata TEXT,
                FOREIGN KEY (user_token) REFERENCES users (fcm_token)
            )
        ''')
        conn.commit()
        print("Database initialized.")

# --- 2. THE EVENT LOGGING ENDPOINT ---

@app.route('/log-event', methods=['POST'])
@require_api_key
def log_event():
    """Receives event data from the Kotlin app and logs it to the database."""
    try:
        data = request.get_json()
        token = data.get('token')
        event_type = data.get('eventType')
        metadata = data.get('metadata') # This can be None

        if not token or not event_type:
            return jsonify({"error": "Missing 'token' or 'eventType'"}), 400

        current_timestamp = int(time.time())

        with get_db_connection() as conn:
            cursor = conn.cursor()
            
            # --- Logic to update the user's status ---
            # INSERT OR REPLACE (UPSERT) is perfect here. It creates a new user
            # or updates an existing one. We reset their inactivity notification flag
            # because they are now active.
            cursor.execute(
                "INSERT OR REPLACE INTO users (fcm_token, last_seen_timestamp, notified_for_inactivity) VALUES (?, ?, ?)",
                (token, current_timestamp, 0) # 0 means FALSE
            )

            # --- Logic to log the specific event ---
            cursor.execute(
                "INSERT INTO events (user_token, event_type, timestamp, metadata) VALUES (?, ?, ?, ?)",
                (token, event_type, current_timestamp, json.dumps(metadata) if metadata else None)
            )
            conn.commit()
            
        print(f"Logged event '{event_type}' for token: {token[:15]}...")
        return jsonify({"status": "success"}), 200

    except Exception as e:
        print(f"Error in /log-event: {e}")
        return jsonify({"error": str(e)}), 500


# --- 3. BACKGROUND TASK AND NOTIFICATION LOGIC ---

def inactivity_checker_task():
    """The background thread's main function."""
    print("Starting inactivity checker background task...")
    while True:
        # Check every 15 minutes
        time.sleep(1)
        
        print("BACKGROUND: Running inactivity check...")
        
        # 8 hours in seconds
        INACTIVITY_THRESHOLD = 60
        # For testing, use a shorter time, e.g., 180 seconds (3 minutes)
        # INACTIVITY_THRESHOLD = 180

        now = int(time.time())
        
        try:
            with get_db_connection() as conn:
                cursor = conn.cursor()
                
                # --- The Core Logic ---
                # Find users who haven't been seen in over 8 hours
                # AND whom we haven't already notified.
                cursor.execute(
                    "SELECT fcm_token FROM users WHERE (? - last_seen_timestamp) > ? AND notified_for_inactivity = 0",
                    (now, INACTIVITY_THRESHOLD)
                )
                inactive_users = cursor.fetchall()

                if inactive_users:
                    print(f"BACKGROUND: Found {len(inactive_users)} inactive user(s). Preparing to notify.")
                
                for user_row in inactive_users:
                    token = user_row['fcm_token']
                    if send_inactivity_notification(token):
                        # If sending was successful, mark the user as notified
                        # to avoid spamming them.
                        cursor.execute(
                            "UPDATE users SET notified_for_inactivity = 1 WHERE fcm_token = ?",
                            (token,)
                        )
                        conn.commit()
        except Exception as e:
            print(f"BACKGROUND: Error during inactivity check: {e}")


def send_inactivity_notification(token):
    """Sends the actual FCM message."""
    print(f"Attempting to send inactivity notification to {token[:15]}...")
    try:
        message = messaging.Message(
            notification=messaging.Notification(
                title='Inactivity!',
                body='User is inactive for 8 hours!',
            ),
            data={"destination": "special"},
            token=token,
        )
        messaging.send(message)
        print(f"Successfully sent notification to {token[:15]}")
        return True
    except Exception as e:
        # This can happen if the token is no longer valid.
        print(f"Failed to send notification to {token[:15]}. Error: {e}")
        return False


# --- 4. MAIN EXECUTION BLOCK ---

if __name__ == '__main__':
    init_db() # Ensure database tables are created on start

    # Start the background thread
    checker_thread = threading.Thread(target=inactivity_checker_task, daemon=True)
    checker_thread.start()

    # Start the Flask web server
    app.run(host='0.0.0.0', port=5000)