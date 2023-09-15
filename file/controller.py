import os
import uuid
from datetime import datetime
from typing import Annotated

import starlette.status as status
from sqlalchemy.orm import Session

from fastapi import APIRouter, Depends, HTTPException, UploadFile
from fastapi.responses import FileResponse
from model.models import Files
from folder.service import update_children_file , get_parent_folder
from utils.utils import is_user
from file.service import (
    get_db,
    get_uploaded_file,
    check_existing_file,
    save_file_to_db,
    delete_file_from_db,
)



fileController = APIRouter(prefix="/file")

db_dependency = Annotated[Session, Depends(get_db)]

current_time = datetime.now().strftime("%Y-%m-%dT%H:%M:%S")

""" 
POST : Upload file and update parent folder's chilren
"""
@fileController.post("/{username}/upload")
async def upload_file(
    db: db_dependency, username: str, parent_name: str, file: UploadFile
) -> None:
    unique_id = uuid.uuid4().hex
    stored_name = f"{file.filename}_{unique_id}"

    with open(file.filename, "wb") as f:
        f.write(file.file.read())

    existing_file = check_existing_file(db, file, username)

    if existing_file:
        raise HTTPException(status_code=409, detail="File already exists")
    
    parent_folder = get_parent_folder(db, username, parent_name)

    uploaded_file = Files(
        original_name=file.filename,
        stored_name=stored_name,
        file_size=os.path.getsize(file.filename),
        uploader=username,
        uploaded_time=current_time,
        modified_time=current_time,
        is_folder=False,
        parent_id=parent_folder.id,
    )

    update_children_file(db, parent_name, username, uploaded_file)

    save_file_to_db(db, uploaded_file)


""" 
DELETE : Delete file for specific user
"""
@fileController.delete("/{username}/delete/", status_code=status.HTTP_204_NO_CONTENT)
async def delete_file(db: db_dependency, username: str, file_name: str) -> None:
    if not is_user(file_name, username, db):
        raise HTTPException(status_code=403, detail="Permission denied")

    uploaded_file = get_uploaded_file(file_name, username, db)

    if not uploaded_file:
        raise HTTPException(status_code=404, detail="File not found")

    delete_file_from_db(db, uploaded_file)

""" 
GET : Download file
"""
@fileController.get("/{username}/download/")
async def download_file(
    db: db_dependency, username: str, file_name: str
) -> FileResponse:
    uploaded_file = get_uploaded_file(file_name, username, db)
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
