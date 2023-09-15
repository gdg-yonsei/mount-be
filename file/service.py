import os
from sqlalchemy.orm import Session
from typing import Annotated
from fastapi import Depends , UploadFile
from model.database import SessionLocal
from model.models import Files


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        
db_dependency = Annotated[Session, Depends(get_db)]

def get_uploaded_file(original_name : str, username : str, db : db_dependency):
    first_uploaded_file = db.query(Files).filter(
        Files.original_name == original_name,
        Files.uploader == username).first()
    return first_uploaded_file

def check_existing_file(db : db_dependency, file : UploadFile, username : str):
    existing_file = db.query(Files).filter(
        Files.original_name ==file.filename,
        Files.uploader == username,
        ).first()
    
    return existing_file

def save_file_to_db(db:db_dependency, uploaded_file):
    db.add(uploaded_file)
    db.commit()

def delete_file_from_db(db:db_dependency, uploaded_file):
    original_name = uploaded_file.original_name
    try:
        os.remove(original_name)
    except FileNotFoundError:
        pass
    
    db.delete(uploaded_file)
    db.commit()


