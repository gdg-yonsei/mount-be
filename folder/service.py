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

def get_folder_path(db, username, folder_name):
    folder = get_folder_by_name(db, username, folder_name)
    if folder.original_name == "root":
        return "root"
    parent_folder = get_folder_by_id(db, folder.parent_id)
    path = ""

    while parent_folder.original_name != 'root':
        path = f"{parent_folder.original_name}/{path}"
        parent_folder = get_folder_by_id(db, parent_folder.parent_id)
        
    path = f"root/{path}{folder_name}/"
    
    return path

def get_child_files(db, folder):
    
    child_files = db.query(Files).filter(
        Files.parent_id == folder.id
    ).all()
    
    return child_files

def get_child_folders(db, folder):
    
    child_folders = db.query(Folders).filter(
        Folders.parent_id == folder.id
    ).all()
    
    return child_folders
    

def get_folder_size(db, username, folder_name):
    folder = get_folder_by_name(db, username, folder_name)
    
    

def save_folder(db, new_folder):
    db.add(new_folder)
    db.commit()

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
        parent_folder.modified_time = current_time
        flag_modified(parent_folder, "modified_time")
        save_folder(db, new_folder)
    return new_folder
    

def update_folder_data(db, existing_folder, new_folder_name):
    
    existing_folder.original_name = new_folder_name
    existing_folder.stored_name = new_folder_name
    existing_folder.modified_time = current_time

    db.commit()
    
def delete_folder_data(db, username, folder_name):
    from file.service import delete_file_data
    
    parent_folder = get_folder_by_name(db, username, folder_name)
    
    child_files = get_child_files(db, parent_folder)
    for item in child_files:
        delete_file_data(db, username, item.original_name)
    
    child_folders = get_child_folders(db, parent_folder)
    for item in child_folders:
        delete_folder_data(db, username, item.original_name)
        
    parent_folder.modified_time = current_time
    flag_modified(parent_folder, "modified_time")
    
    db.delete(parent_folder)
    db.commit()
    
def move_folder_data(db, username, folder_name, move_to_folder_name):
    parent_folder = get_folder_by_name(db, username, move_to_folder_name)
    folder = get_folder_by_name(db, username, folder_name)
    
    folder.parent_id = parent_folder.id
    parent_folder.modified_time = current_time
    
    flag_modified(folder, "parent_id")
    flag_modified(parent_folder, "modified_time")
    
    db.commit()
    
def get_folder_data(db, username, folder_name):
    folder = get_folder_by_name(db, username, folder_name)
    return_data = {}
    # type
    return_data['type'] = 'folder'
    
    # path
    path = get_folder_path(db, username, folder_name)
    return_data['path'] = path
    
    # size
    size = 0
    child_files = get_child_files(db, folder)
    
    for item in child_files:
        size += item.file_size
    return_data['size'] = size
        
    # child data
    child_file_number = len(child_files)
    
    child_folders = get_child_folders(db, folder)
    child_folder_number = len(child_folders)
    
    return_data['child data'] = f"files : {child_file_number}, folders : {child_folder_number}"
    
    # uploaded_time
    return_data['upload_time'] = folder.uploaded_time
    
    # modified_time
    return_data['modified_time'] = folder.modified_time
    
    return return_data