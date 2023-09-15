import json
from sqlalchemy.orm import Session
from sqlalchemy.orm.attributes import flag_modified
from typing import Annotated
from fastapi import Depends 
from model.database import SessionLocal
from model.models import Folders


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        
db_dependency = Annotated[Session, Depends(get_db)]

def check_existing_folder(db : db_dependency, folder_name : str, username : str ):
    existing_folder = db.query(Folders).filter(
        Folders.original_name == folder_name,
        Folders.is_folder == True,
        Folders.uploader == username
        ).first()
    
    return existing_folder


def check_if_folder(db: db_dependency, name : str, username : str):
    data = db.query(Folders.uploader == username, Folders.original_name == name).first()
    
    return data.is_folder is True

def update_children_file(db: db_dependency, parent_name : str, username :str , uploaded_file):
    
    parent_folder = db.query(Folders).filter(
            Folders.original_name == parent_name, 
            Folders.uploader == username).first()
    
    if parent_folder.children is None:
        parent_folder.children = []
    
    child_info = {
            "name": uploaded_file.original_name,
            "is_folder": False,
            "file_size" : uploaded_file.file_size,
            "created_time": uploaded_file.uploaded_time,
            }

    child_info_json = json.dumps(child_info, ensure_ascii= False)
    parent_folder.children.append(child_info_json)
    
    parent_folder.modified_time = uploaded_file.uploaded_time
    
    flag_modified(parent_folder, "children")
    flag_modified(parent_folder, "modified_time")
    
def update_children_folder(db: db_dependency, parent_name : int, username :str , new_folder):
    
    parent_folder = db.query(Folders).filter(
            Folders.original_name == parent_name, 
            Folders.uploader == username).first()
    
    if parent_folder.children is None:
            parent_folder.children = []
    child_info = {
        "name": new_folder.original_name,
        "is_folder": True,  
        "created_time": new_folder.uploaded_time,
        }
    
    child_info_json = json.dumps(child_info)
    parent_folder.children.append(child_info_json)
    parent_folder.modified_time = new_folder.uploaded_time
    
    flag_modified(parent_folder, "children")
    flag_modified(parent_folder, "modified_time")
    
def save_folder(db, new_folder):
    db.add(new_folder)
    db.commit()

def get_parent_folder(db, username , folder_name):
    parent_folder = db.query(Folders).filter(
        Folders.original_name == folder_name, 
        Folders.is_folder == True,  
        Folders.uploader == username).first()
    return parent_folder

def get_folder(db, username, folder_name):
    existing_folder = db.query(Folders).filter(
        Folders.uploader == username,
        Folders.original_name == folder_name
        ).first()
    return existing_folder

