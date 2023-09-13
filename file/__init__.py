from database import SessionLocal
UPLOAD_DIR = "cloud/"

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        