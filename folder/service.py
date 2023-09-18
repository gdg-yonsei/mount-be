from fastapi import HTTPException, Depends
from fastapi.responses import JSONResponse
from sqlalchemy.orm import Session
from folder.model import Folder
from file.model import File
from database import get_db
import json
 
def get_folder_metadata(user_id: str, folder_path: str, folder_name: str, db: Session):
    '''
    path/folder_name에 해당하는 Folder 객체를 반환
    '''
    path_parts = folder_path.split("/") if folder_path else []
    path_folder_id = -1
   
    user_folders = db.query(Folder).filter(Folder.user_id == user_id)
   
   # root 폴더부터 path의 마지막까지 folder_id 검색
   # path 폴더를 parent로 가진 folder_name에 해당하는 folder_id로 path_folder_id를 갱신
    for path in path_parts:
        path_folder_id = user_folders.filter(Folder.parent_id == path_folder_id)\
            .filter(Folder.folder_name == path).first().folder_id
    
    folder = user_folders.filter(Folder.parent_id == path_folder_id)\
        .filter(Folder.folder_name == folder_name).first()
    return folder

def get_parent_folder_id(user_id: str, folder_path: str, db: Session) -> int:
    '''
    parent_folder id 검색. root는 -1반환
    '''
    #find parent_id
    if folder_path == "":
        return -1
    
    path_parts = folder_path.split('/')
    parent_folder = get_folder_metadata(user_id, folder_path="/".join(path_parts[:-1]), folder_name=path_parts[-1], db=db)
    return parent_folder.folder_id
    
def check_folder_exists(user_id:str, parent_id:int, folder_name: str, db: Session):
    folder_exists = db.query(Folder).filter(Folder.user_id == user_id)\
        .filter(Folder.parent_id == parent_id).filter(Folder.folder_name == folder_name).first()
        
    if folder_exists:
        raise HTTPException(status_code=409, detail="folder_already_exists")
      
def check_and_save_folder(user_id:str, folder_path:str, folder_name:str, db: Session):
    '''
    폴더가 존재하면 raise HTTPException
    Folder 객체 생성 후 db에 저장
    '''
    parent_id = get_parent_folder_id(user_id, folder_path, db=db)
    
    check_folder_exists(user_id, parent_id, folder_name, db)
    
    #save folder metadata
    folder = Folder(
        folder_name = folder_name,
        user_id = user_id,
        parent_id = parent_id
    )
    
    db.add(folder)
    db.commit()
    
def check_and_rename_folder(user_id:str, folder_path:str, folder_name:str, new_name:str, db: Session):
    '''
    바꾸고자 하는 폴더 이름이 이미 존재할 경우 에러 반환
    폴더 이름 메타데이터 수정
    '''
    parent_id = get_parent_folder_id(user_id, folder_path, db=db)
    
    check_folder_exists(user_id, parent_id, new_name, db)
    
    db.query(Folder).filter(Folder.user_id == user_id).filter(Folder.parent_id == parent_id)\
        .filter(Folder.folder_name == folder_name).update({Folder.folder_name: new_name})
    db.commit()
    
def check_and_get_items_metadata(user_id:str, folder_path:str, folder_name:str, db:Session):
    '''
    폴더 안의 폴더/파일 메타데이터 검색
    '''
    parent_id = get_parent_folder_id(user_id, folder_path, db)
    
    # 폴더 검색
    folder_id = db.query(Folder).filter(Folder.user_id == user_id)\
        .filter(Folder.parent_id == parent_id).filter(Folder.folder_name == folder_name).first().folder_id
    
    child_folder = db.query(Folder.folder_name).filter(Folder.parent_id == folder_id).all()
    content = {idx: row._data for idx, row in enumerate(child_folder)}
    # 파일 검색
    return JSONResponse(content = content)