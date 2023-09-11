from typing import Annotated
from sqlalchemy.orm import Session
from fastapi import APIRouter, Depends, HTTPException, UploadFile
from fastapi.responses import FileResponse
from database import SessionLocal
from models import Files, Folders
from datetime import datetime
import starlette.status as status
import os
import uuid

router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        
db_dependency = Annotated[Session, Depends(get_db)]



@router.post("/uploadfile")
async def upload_file(db: db_dependency, uploader:str , file : UploadFile):
    current_time = datetime.now().strftime("%Y%m%dT%H%M%S")
    file_extension = os.path.splitext(file.filename)[1]
    unique_id = uuid.uuid4().hex
    stored_filename = f"{current_time}_{file.filename}_{unique_id}"
    UPLOAD_DIRECTORY = "./upload"
    with open(file.filename, "wb") as f:
        f.write(file.file.read())
        
    existing_files = db.query(Files).filter(
        Files.original_filename ==file.filename,
        Files.uploader == uploader
        ).all()
    
    count = 1
    if existing_files:
        max_count = max(existing_file.count for existing_file in existing_files)
        count = max_count + 1
    
    uploaded_file = Files(
        original_filename=file.filename,
        stored_filename=stored_filename,
        file_size=os.path.getsize(file.filename),
        uploader=uploader,
        count=count,
    )
    db.add(uploaded_file)
    db.commit()

def is_user(original_filename : str, username : str, db : db_dependency):
    uploaded_files = db.query(Files).filter(Files.original_filename == original_filename).all()
    for uploaded_file in uploaded_files:
        if uploaded_file.uploader == username:
            return True
    return False
    
    
@router.delete("/deletefile/{original_filename}", status_code = status.HTTP_204_NO_CONTENT)
async def delete_file(db:db_dependency, username : str, original_filename : str, count : int = 1):
    if not is_user(original_filename, username, db ):
        raise HTTPException(status_code = 403, detail = "Permission denied")
    uploaded_file = db.query(Files).filter(
        Files.original_filename == original_filename)\
            .filter(Files.uploader == username)\
                .filter(Files.count == count).first()
    if not uploaded_file:
        raise HTTPException(status_code = 404, detail = "File not found")
    
    stored_filename = uploaded_file.stored_filename
    try:
        os.remove(stored_filename)
    except FileNotFoundError:
        pass
    
    db.delete(uploaded_file)
    db.commit()

@router.get("/download/file/{file_id}")
async def download_file(db:db_dependency, username : str, original_filename : str, count: int = 1):
    uploaded_file = db.query(Files).filter(Files.original_filename == original_filename)\
        .filter(Files.uploader == username)\
            .filter(Files.count == count)\
                .first()
    if not uploaded_file:
        raise HTTPException(status_code = 404, detail = "File not found")
    if not is_user(original_filename, username, db ):
        raise HTTPException(status_code = 403, detail = "Permission denied")
    file_path = f"{uploaded_file.original_filename}"
    print("File Path:", file_path)
    return FileResponse(file_path, headers={"Content-Disposition": f"attachment; filename={uploaded_file.original_filename}"})

@router.get("/",status_code = status.HTTP_200_OK)
async def read_all(db:db_dependency, username:str):
    return db.query(Files).filter(Files.uploader == username).all()

