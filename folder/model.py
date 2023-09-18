from sqlalchemy import Column, Integer, String, DateTime, func
from database import Base

class Folder(Base):
    '''
    table for folder metadata
    '''
    __tablename__ = "Folders"
    folder_id = Column(Integer, primary_key=True, autoincrement=True)
    folder_name = Column(String, nullable=False)
    user_id = Column(String, index=True, nullable=False)
    parent_id = Column(Integer, nullable=False)
    created_date = Column(DateTime, default=func.current_timestamp())
    modified_date = Column(DateTime, default=func.current_timestamp(), onupdate=func.current_timestamp())