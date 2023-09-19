from typing import Annotated

import starlette.status as status
from sqlalchemy.orm import Session

from fastapi import APIRouter, Depends, HTTPException, UploadFile
from fastapi.responses import FileResponse
from model.models import Files
from utils.utils import get_db
from user.service import is_user, check_first_user
from file.service import (
    get_file,
    save_file_data,
    delete_file_data,
    move_file_data
)

fileController = APIRouter(prefix="/file")

db_dependency = Annotated[Session, Depends(get_db)]

""" 
POST : Upload file and update parent folder's chilren
"""
@fileController.post("/{username}/upload")
async def upload_file(
    db: db_dependency, username: str, file: UploadFile,  parent_name: str = "root", 
) -> None:


    user = check_first_user(db,username)
    if user:
        existing_file = get_file(db, username, file.filename )
        if existing_file:
            raise HTTPException(status_code=409, detail="File already exists")
        else:
            save_file_data(db, file, username, parent_name)
    else:
        raise HTTPException(status_code=403 , detail = "Create Root Folder First")
    


""" 
DELETE : Delete file for specific user
"""
@fileController.delete("/{username}/delete/", status_code=status.HTTP_204_NO_CONTENT)
async def delete_file(db: db_dependency, username: str, file_name: str) -> None:
    if not is_user(file_name, username, db):
        raise HTTPException(status_code=403, detail="Permission denied")

    uploaded_file = get_file(db, username, file_name)
    if not uploaded_file:
        raise HTTPException(status_code=404, detail="File not found")

    delete_file_data(db, username, file_name)

""" 
GET : Download file
"""
@fileController.get("/{username}/download/")
async def download_file(
    db: db_dependency, username: str, file_name: str
) -> FileResponse:
    uploaded_file = get_file(db, username, file_name)
    if not uploaded_file:
        raise HTTPException(status_code=404, detail="File not found")

    if not is_user(file_name, username, db):
        raise HTTPException(status_code=403, detail="Permission denied")

    file_path = f"{uploaded_file.original_name}"
    print("File Path:", file_path)
    return FileResponse(
        file_path,
        headers={
            "Content-Disposition": f"attachment; filename={uploaded_file.original_name}"
        },
    )

""" 
GET : Get all files for specific user
"""
@fileController.get("/{username}/", status_code=status.HTTP_200_OK)
async def get_user_files(db: db_dependency, username: str):
    return (
        db.query(Files)
        .filter(Files.uploader == username)
        .all()
    )

"""
PUT : Move File to another Folder
"""
@fileController.put("/{username}/move")
async def move_file(db: db_dependency, username: str, file_name : str, move_to_folder_name : str):
    move_file_data(db, username, file_name, move_to_folder_name)
    
    return {
        "message": f"{file_name} moved to {move_to_folder_name}"
    }