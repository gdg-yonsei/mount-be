from fastapi import FastAPI, Depends, HTTPException
from fastapi.responses import FileResponse
from sqlalchemy import create_engine
from sqlalchemy.orm import Session
from file.model import File
import os

def check_file_exists(file_name:str, user_id: str, db: Session):
    user_files = db.query(File).filter(File.user_id == user_id).all()
    return any(file.file_name == file_name for file in user_files)

def get_tuple(file_name: str, user_id:str, db: Session):
    target = db.query(File).filter(File.user_id == user_id).filter(File.file_name==file_name).first()
    return target

def save_file(user_file: File, user_id: str, db=Session):
    db.add(user_file)
    db.commit()
    
def delete_file_service(file_name: str, user_id:str, db: Session):
    target = get_tuple(file_name, user_id, db)
    os.remove(target.server_filename)
    db.delete(target)
    db.commit()
    
def can_access(file_name: str, user_id: str, db: Session):
    #access 파트는 차후step에서 수정 예정
    target = get_tuple(file_name, user_id, db)
    if target.user_id == user_id or File.access != 0:
        return True
    return False

def get_file(file_name:str, user_id:str, db:Session):
    target = get_tuple(file_name, user_id, db)
    pysical_file_path = target.server_filename
    return FileResponse(pysical_file_path, filename=target.file_name)