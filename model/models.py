from model.database import Base
from sqlalchemy import Column, Integer, String, Boolean, ForeignKey , ARRAY



class Folders(Base):
    __tablename__ = 'folders'
    id = Column(Integer, primary_key=True,index=True)
    original_name = Column(String, nullable=False)
    stored_name = Column(String, nullable=True)
    file_size = Column(Integer, nullable=True)
    uploader = Column(String, nullable=False)
    uploaded_time = Column(String, nullable=False)
    modified_time = Column(String, nullable=True)
    is_folder = Column(Boolean, default = True)
    parent_id = Column(Integer,ForeignKey('folders.id'), default = 1)
    children = Column(ARRAY(String))

class Files(Base):
    __tablename__ = 'files'
    
    id = Column(Integer, primary_key=True,index=True)
    original_name = Column(String, nullable=False)
    stored_name = Column(String, nullable=True)
    file_size = Column(Integer, nullable=True)
    uploader = Column(String, nullable=False)
    uploaded_time = Column(String, nullable=False)
    modified_time = Column(String, nullable=True)
    is_folder = Column(Boolean, default = False)
    parent_id = Column(Integer,ForeignKey('folders.id'), default = 1)
    
    