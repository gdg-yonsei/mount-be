import json
from datetime import datetime

from sqlalchemy.orm import Session
from sqlalchemy.orm.attributes import flag_modified
from typing import Annotated
from fastapi import Depends 
from model.models import Folders
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
        parent_folder = get_folder(db, username, parent_name)
        
        new_folder = Folders(
            original_name=folder_name,
            stored_name=folder_name,
            uploader=username,
            uploaded_time=current_time,
            modified_time=current_time,
            parent_id= parent_folder.id
        )
        
        update_children_folder(db, parent_name, username, new_folder)
    
        save_folder(db, new_folder)
    return new_folder
    


def get_folder(db, username, folder_name):
    existing_folder = db.query(Folders).filter(
        Folders.uploader == username,
        Folders.original_name == folder_name
        ).first()
    return existing_folder

def add_child_info(db, child_info, username, parent_name, modified_time):
    
    parent_folder = get_folder(db, username, parent_name)
    
    if parent_folder.children is None:
        parent_folder.children = []
        
    child_info_json = json.dumps(child_info, ensure_ascii= False)
    parent_folder.children.append(child_info_json)
    
    parent_folder.modified_time = modified_time
    
    flag_modified(parent_folder, "children")
    flag_modified(parent_folder, "modified_time")

def update_children_file(db: db_dependency, parent_name : str, username :str , uploaded_file):
    
    modified_time = uploaded_file.uploaded_time
    
    child_info = {
            "name": uploaded_file.original_name,
            "is_folder": False,
            "file_size" : uploaded_file.file_size,
            "created_time": uploaded_file.uploaded_time,
            }

    add_child_info(db, child_info, username,  parent_name , modified_time)
    
    
def update_children_folder(db: db_dependency, parent_name : int, username :str , new_folder):
            
    modified_time = new_folder.uploaded_time

    child_info = {
        "name": new_folder.original_name,
        "is_folder": True,  
        "created_time": new_folder.uploaded_time,
        }
    
    add_child_info(db, child_info, username,  parent_name , modified_time)

    
def save_folder(db, new_folder):
    db.add(new_folder)
    db.commit()


def update_folder_info(db, existing_folder, new_folder_name):
    
    existing_folder.original_name = new_folder_name
    existing_folder.stored_name = new_folder_name
    existing_folder.modified_time = current_time

    db.commit()