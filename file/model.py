from sqlalchemy import Column, Integer, String, DateTime, func
from database import Base

class File(Base):
    '''
    table for file metadata
    '''
    __tablename__ = "Files"
    file_id = Column(Integer, primary_key=True, autoincrement=True)
    folder_id = Column(Integer, nullable=False)
    file_name = Column(String, nullable=False)
    server_filename = Column(String,unique=True, nullable=False)
    file_size = Column(Integer, nullable=False)
    user_id = Column(String, index=True, nullable=False)
    upload_date = Column(DateTime, default=func.current_timestamp())
    access = Column(Integer, default=0)
    