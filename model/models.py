from model.database import Base
from sqlalchemy import Column, Integer, String, ForeignKey , ARRAY


class Folders(Base):
    __tablename__ = 'folders'
    
    id = Column(Integer, primary_key=True,index=True)
    original_name = Column(String, nullable=False)
    stored_name = Column(String, nullable=True)
    uploader = Column(String, nullable=False)
    uploaded_time = Column(String, nullable=False)
    modified_time = Column(String, nullable=True)
    parent_id = Column(Integer,ForeignKey('folders.id'), default = 1)


class Files(Base):
    __tablename__ = 'files'
    
    id = Column(Integer, primary_key=True,index=True)
    original_name = Column(String, nullable=False)
    stored_name = Column(String, nullable=True)
    file_size = Column(Integer, nullable=True)
    uploader = Column(String, nullable=False)
    uploaded_time = Column(String, nullable=False)
    modified_time = Column(String, nullable=True)
    parent_id = Column(Integer,ForeignKey('folders.id'), default = 1)
    
class Users(Base):
    __tablename__ = 'users'
    
    id = Column(Integer, primary_key=True,index=True)
    username = Column(String, nullable=False)