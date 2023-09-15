from typing import Annotated
from sqlalchemy.orm import Session
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from model.models import Folders
from utils.utils import get_db
from folder.service import (
    update_children_folder,
    save_folder,
    get_folder,
)

folderController = APIRouter(prefix="/folder")

db_dependency = Annotated[Session, Depends(get_db)]

current_time = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

""" 
POST : Create folder for specific user
"""
@folderController.post("/{username}/create")
async def create_folder(
    db: db_dependency,
    folder_name: str,
    username: str,
    parent_name: str = "root",
):
    existing_folder = get_folder(db, username, folder_name)
    if existing_folder:
        raise HTTPException(status_code=400, detail="Folders already exist")
    
    # 시작 root 폴더 생성
    if parent_name == 'start':
        new_folder = Folders(
        original_name= 'root',
        stored_name= 'root',
        file_size=0,
        uploader=username,
        uploaded_time=current_time,
        modified_time=current_time,
        is_folder=True,
        parent_id= 0
        )
    
    else:
        parent_folder = get_folder(db, username, parent_name)
        
        new_folder = Folders(
        original_name=folder_name,
        stored_name=folder_name,
        file_size=0,
        uploader=username,
        uploaded_time=current_time,
        modified_time=current_time,
        is_folder=True,
        parent_id= parent_folder.id
        )
        
        update_children_folder(db, parent_name, username, new_folder)
        

    save_folder(db, new_folder)

    return new_folder


""" 
GET : Get all children(files and folders) for specific parent folder 
"""
@folderController.get("/{username}/{folder_name}")
async def get_children(
    db: db_dependency,
    username: str,
    folder_name: str,
) -> list:
    parent_folder = get_folder(db, username, folder_name)

    if parent_folder:
        return parent_folder.children
    else:
        raise HTTPException(status_code=404, detail="Folder not found")


"""
GET : Get all folders in Database
"""
@folderController.get("/{username}/")
async def get_user_folders(db: db_dependency, username: str):
    return db.query(Folders).filter(Folders.uploader == username).all()


"""
PUT : Update Folder name and update modified time
"""
@folderController.put("/{username}/update")
async def update_folder_name(
    db: db_dependency, username: str, existing_folder_name: str, new_folder_name: str
):
    existing_folder = get_folder(db, username, existing_folder_name)
    
    if not existing_folder:
        raise HTTPException(status_code=404, detail="Folder Not Found")

    existing_folder.original_name = new_folder_name
    existing_folder.stored_name = new_folder_name
    existing_folder.modified_time = current_time

    db.commit()

    return {
        "message": f"Folder name updated from {existing_folder_name} to {new_folder_name}"
    }

