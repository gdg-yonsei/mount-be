from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from folder.model import Folder
from folder.service import *
from database import get_db

folderController = APIRouter(prefix='/folder')

@folderController.post('/{folder_name}')
async def create_folder(user_id: str,  folder_name: str, folder_path: str | None = ""):
    '''
    폴더 생성 API
    '''
    check_and_save_folder(user_id, folder_path, folder_name)
    
@folderController.patch('/{folder_name}')
async def rename_folder(user_id:str, folder_name:str, new_name: str, folder_path: str | None = ""):
    '''
    폴더 이름 변경 API
    '''
    check_and_rename_folder(user_id, folder_path, folder_name, new_name)

@folderController.get('/{folder_name}')
async def get_items_in_folder(user_id:str, folder_name:str, folder_path:str | None = ""):
    '''
    요청된 폴더의 하위 아이템(폴더와 파일)의 메타데이터 반환 API
    '''
    return check_and_get_items_metadata(user_id, folder_path, folder_name)