from fastapi import APIRouter, UploadFile, Depends, HTTPException
from sqlalchemy.orm import Session
from database import get_db
from file import UPLOAD_DIR
from file.model import File
from file.service import *
import uuid

fileController = APIRouter(prefix='/file')

@fileController.post('/')
async def upload_file(file: UploadFile, user_id: str, db: Session = Depends(get_db), file_path: str | None = ""):
    '''
    파일 존재하는지 확인
    로컬에 파일 저장, 메타데이터 db에 저장
    '''
    file_name = file.filename
    file_extension = "." + file_name.split('.')[-1] if '.' in file_name else ""
    server_filename = UPLOAD_DIR + uuid.uuid4().hex + file_extension
    content = await file.read()
    
    if has_file(file_name, user_id, file_path, db):
        raise HTTPException(status_code=409, detail="file already exists")
    
    save_file(file_name, server_filename, user_id, file_path, content, db)  
     

@fileController.delete('/{file_name}')
async def delete_file(file_name: str, user_id:str, db: Session = Depends(get_db), file_path: str | None = ""):
    ''' 
    존재하지 않는 파일을 삭제할경우 에러반환
    물리적 삭제 포함
    '''
    if not has_file(file_name, user_id, file_path, db=db):
        raise HTTPException(status_code=404, detail="file doesn't exist")
    
    delete_file_service(file_name, user_id, file_path, db=db)
    
@fileController.get('/{file_name}')
async def download_file(file_name:str, user_id:str, db: Session = Depends(get_db), file_path: str | None = ""):
    '''
    파일 존재하지 않으면 에러반환
    권한이 없으면 다운 불가능 (본인만)
    '''
    if not has_file(file_name, user_id, file_path, db=db):
        raise HTTPException(status_code=404, detail="file doesn't exist")
    
    return get_file(file_name, user_id, file_path, db=db)