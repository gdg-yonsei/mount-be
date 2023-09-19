import json
from datetime import datetime

from sqlalchemy.orm import Session
from sqlalchemy.orm.attributes import flag_modified
from typing import Annotated
from fastapi import Depends 
from model.models import Folders, Files
from utils.utils import get_db, get_current_time
from user.service import check_first_user, add_user


        
db_dependency = Annotated[Session, Depends(get_db)]

current_time = get_current_time()

def create_root_folder(db, username):
    root_folder = Folders(
            original_name= 'root',
            stored_name= 'root',
            uploader=username,
            uploaded_time=current_time,
            modified_time=current_time,
            parent_id= 0
        )
    save_folder(db,root_folder)
    return root_folder

def create_new_folder(db, folder_name, username, parent_name):
    
    user = check_first_user(db, username)
    
    if not user:
        new_folder = create_root_folder(db,username)
        add_user(db,username)
        
    else:
        parent_folder = get_folder_by_name(db, username, parent_name)
        
        new_folder = Folders(
            original_name=folder_name,
            stored_name=folder_name,
            uploader=username,
            uploaded_time=current_time,
            modified_time=current_time,
            parent_id= parent_folder.id
        )
        
    
        save_folder(db, new_folder)
    return new_folder
    


def get_folder_by_name(db, username, folder_name):
    folder = db.query(Folders).filter(
        Folders.uploader == username,
        Folders.original_name == folder_name
        ).first()
    return folder

def get_folder_by_id(db, folder_id):
    folder = db.query(Folders).filter(
        Folders.id == folder_id
        ).first()
    return folder


    
def save_folder(db, new_folder):
    db.add(new_folder)
    db.commit()


def update_folder_info(db, existing_folder, new_folder_name):
    
    existing_folder.original_name = new_folder_name
    existing_folder.stored_name = new_folder_name
    existing_folder.modified_time = current_time

    db.commit()
    
def delete_folder_info(db, username, folder_name):
    from file.service import delete_file_data
    
    parent_folder = get_folder_by_name(db, username, folder_name)
    
    child_files = db.query(Files).filter(
        Files.parent_id == parent_folder.id
    ).all()
    
    for item in child_files:
        delete_file_data(db, username, item.original_name)
    
    child_folders = db.query(Folders).filter(
        Folders.parent_id == parent_folder.id
    ).all()
    for item in child_folders:
        delete_folder_info(db, username, item.original_name)
        
    db.delete(parent_folder)
    db.commit()
    