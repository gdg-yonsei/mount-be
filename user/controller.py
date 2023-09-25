from typing import Annotated
import starlette.status as status
from sqlalchemy.orm import Session

from fastapi import APIRouter, Depends, HTTPException
from model.models import Users
from utils.utils import get_db 


userController = APIRouter(prefix="/user")

db_dependency = Annotated[Session, Depends(get_db)]


""" 
GET : Get all usernames
"""
@userController.get("/{username}/", status_code=status.HTTP_200_OK)
async def get_all_users(db: db_dependency):
    return (
        db.query(Users).all()
    )