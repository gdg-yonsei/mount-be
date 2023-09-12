from sqlalchemy.orm import Session
from typing import Annotated
from fastapi import Depends
from file.model.database import SessionLocal
from file.model.filemodels import Files


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        
db_dependency = Annotated[Session, Depends(get_db)]

def is_user(original_filename : str, username : str, db : db_dependency) -> bool:
    uploaded_files = db.query(Files).filter(Files.original_filename == original_filename).all()
    return any(uploaded_file.uploader == username for uploaded_file in uploaded_files)

def get_uploaded_file(original_filename : str, username : str, db : db_dependency):
    first_uploaded_file = db.query(Files).filter(Files.original_filename == original_filename).filter(Files.uploader == username).first()
    return first_uploaded_file