import os
import uuid
from typing import Annotated
from sqlalchemy.orm import Session
import starlette.status as status
from fastapi import APIRouter, Depends, HTTPException, UploadFile
from fastapi.responses import FileResponse
from file.service.database import SessionLocal
from file.service.filemodels import Files
from file.service.utils import is_user , get_uploaded_file


router = APIRouter()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
        
db_dependency = Annotated[Session, Depends(get_db)]


@router.post("/uploadfile")
async def upload_file(db: db_dependency, uploader:str , file : UploadFile) -> None:
    unique_id = uuid.uuid4().hex
    stored_filename = f"{file.filename}_{unique_id}"
    with open(file.filename, "wb") as f:
        f.write(file.file.read())
        
    existing_files = db.query(Files).filter(
        Files.original_filename ==file.filename,
        Files.uploader == uploader
        ).all()
    
    if existing_files:
        raise HTTPException(status_code = 409, detail = "File already exists")
    
    uploaded_file = Files(
        original_filename=file.filename,
        stored_filename=stored_filename,
        file_size=os.path.getsize(file.filename),
        uploader=uploader,
    )
    db.add(uploaded_file)
    db.commit()
    


@router.delete("/deletefile/{original_filename}", status_code = status.HTTP_204_NO_CONTENT)
async def delete_file(db:db_dependency, username : str, original_filename : str) -> None:
    if not is_user(original_filename, username, db ):
        raise HTTPException(status_code = 403, detail = "Permission denied")
    uploaded_file = get_uploaded_file(original_filename, username, db)
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
async def download_file(db:db_dependency, username : str, original_filename : str) -> FileResponse:
    uploaded_file = get_uploaded_file(original_filename, username, db)
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

