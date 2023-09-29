from fastapi import FastAPI, Depends, HTTPException
from fastapi.responses import FileResponse
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from file.model import File
from folder.service import get_parent_folder_id
import os


def has_file(file_name:str, user_id: str, file_path:str, db: Session):
    folder_id = get_parent_folder_id(user_id, file_path, db)
    user_files = db.query(File).filter(File.user_id == user_id).all()
    return any(file.file_name == file_name and file.folder_id == folder_id for file in user_files)

def get_metadata(file_name: str, user_id:str, file_path:str, db: Session):
    folder_id = get_parent_folder_id(user_id, file_path, db)
    target = db.query(File).filter(File.user_id == user_id).filter(File.file_name==file_name).first()
    return target

def save_file(file_name:str, server_filename:str, user_id:str, file_path:str , content:bytes, db=Session):
    folder_id = get_parent_folder_id(user_id, file_path, db)
    
    user_file = File(
        file_name = file_name,
        folder_id = folder_id,
        server_filename = server_filename,
        file_size = len(content),
        user_id = user_id
    )
    
    save_file_in_server(user_file.server_filename, content)   
    db.add(user_file)
    db.commit()
    
def delete_file_service(file_name: str, user_id:str, file_path:str, db: Session):
    target = get_metadata(file_name, user_id, file_path, db)
    db.delete(target)
    db.commit()
    delete_file_in_server(target.server_filename)
    
def delete_file_in_server(server_filename: str):
    os.remove(server_filename)
    
def save_file_in_server(server_filename:str, content: bytes):
    with open(server_filename, 'wb') as f:
        f.write(content)

def get_file(file_name:str, user_id:str, file_path:str, db:Session):
    target = get_metadata(file_name, user_id, file_path, db)
    if not(target.user_id == user_id or File.access != 0):
        raise HTTPException(status_code=403, detail="permission denied")
    
    pysical_file_path = target.server_filename
    return FileResponse(pysical_file_path, filename=target.file_name)