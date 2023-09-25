from sqlalchemy.orm import Session
from typing import Annotated
from fastapi import Depends , UploadFile
from model.database import SessionLocal
from model.models import Files, Folders, Users
from utils.utils import get_db


db_dependency = Annotated[Session, Depends(get_db)]

def is_user(original_name : str, username : str, db : db_dependency) -> bool:
    uploaded_files = db.query(Files).filter(Files.original_name == original_name).all()
    return any(uploaded_file.uploader == username for uploaded_file in uploaded_files)

def check_first_user(db : db_dependency, username : str) -> bool :
    user = db.query(Users).filter(Users.username == username).first()
    return user

def add_user (db : db_dependency, username : str):
    new_user = Users(username = username)
    db.add(new_user)
    db.commit()