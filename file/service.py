from fastapi import FastAPI, Depends, HTTPException
from fastapi.responses import FileResponse
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from file.model import File
import os

def has_file(file_name:str, user_id: str, db: Session):
    user_files = db.query(File).filter(File.user_id == user_id).all()
    return any(file.file_name == file_name for file in user_files)

def get_tuple(file_name: str, user_id:str, db: Session):
    target = db.query(File).filter(File.user_id == user_id).filter(File.file_name==file_name).first()
    return target

def save_file(user_file: File, content:bytes, db=Session):
    save_file_in_server(user_file.server_filename, content)   
    db.add(user_file)
    db.commit()
    
def delete_file_service(file_name: str, user_id:str, db: Session):
    target = get_tuple(file_name, user_id, db)
    db.delete(target)
    db.commit()
    delete_file_in_server(target.server_filename)
    
def delete_file_in_server(server_filename: str):
    os.remove(server_filename)
    
def save_file_in_server(server_filename:str, content: bytes):
    with open(server_filename, 'wb') as f:
        f.write(content)
    
def can_access(file_name: str, user_id: str, db: Session):
    #access 파트는 차후step에서 수정 예정
    target = get_tuple(file_name, user_id, db)
    return(target.user_id == user_id or File.access != 0)

def get_file(file_name:str, user_id:str, db:Session):
    target = get_tuple(file_name, user_id, db)
    pysical_file_path = target.server_filename
    return FileResponse(pysical_file_path, filename=target.file_name)