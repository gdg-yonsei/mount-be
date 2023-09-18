from sqlalchemy.orm import Session
from typing import Annotated
from fastapi import Depends , UploadFile
from model.database import SessionLocal
from model.models import Files, Folders, Users
from datetime import datetime


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        

def get_current_time():
    return datetime.now().strftime("%Y-%m-%dT%H:%M:%S")