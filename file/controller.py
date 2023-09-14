from fastapi import APIRouter, UploadFile, Depends, HTTPException
from sqlalchemy.orm import Session
from database import SessionLocal
from file import get_db, UPLOAD_DIR
from file.model import File
from file.service import *
import uuid

fileController = APIRouter(prefix='/file')

@fileController.post('/')
async def upload_file(file: UploadFile, user_id: str, db: Session = Depends(get_db)):
    '''
    파일 존재하는지 확인
    로컬에 파일 저장, 메타데이터 db에 저장
    '''
    file_name = file.filename
    file_extension = file_name.split('.')[-1] if '.' in file_name else ""
    server_filename = UPLOAD_DIR + uuid.uuid4().hex + '.' + file_extension
    content = await file.read()
    
    if has_file(file_name, user_id, db=db):
        raise HTTPException(status_code=409, detail="file already exists")
    
    user_file = File(
        file_name = file_name,
        server_filename = server_filename,
        file_size = len(content),
        user_id = user_id
    )
    
    save_file(user_file, content, db=db)   

@fileController.delete('/{file_name}')
async def delete_file(file_name: str, user_id:str, db: Session = Depends(get_db)):
    ''' 
    존재하지 않는 파일을 삭제할경우 에러반환
    물리적 삭제 포함
    '''
    if not has_file(file_name, user_id, db=db):
        raise HTTPException(status_code=404, detail="file doesn't exist")
    
    delete_file_service(file_name, user_id, db=db)
    
@fileController.get('/{file_name}')
async def download_file(file_name:str, user_id:str, db: Session = Depends(get_db)):
    '''
    파일 존재하지 않으면 에러반환
    권한이 없으면 다운 불가능 (본인만)
    '''
    if not has_file(file_name, user_id, db=db):
        raise HTTPException(status_code=404, detail="file doesn't exist")
    
    allowed_file = get_file(file_name, user_id, db=db)
    if allowed_file is None:
        raise HTTPException(status_code=403, detail="permission denied")
    return allowed_file