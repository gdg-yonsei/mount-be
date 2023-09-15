from sqlalchemy.orm import Session
from typing import Annotated
from fastapi import Depends , UploadFile
from model.database import SessionLocal
from model.models import Files, Folders


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        
db_dependency = Annotated[Session, Depends(get_db)]

def is_user(original_name : str, username : str, db : db_dependency) -> bool:
    uploaded_files = db.query(Files).filter(Files.original_name == original_name).all()
    return any(uploaded_file.uploader == username for uploaded_file in uploaded_files)
