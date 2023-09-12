from file.service.database import Base
from sqlalchemy import Column, Integer, String, ForeignKey

class Files(Base):
    __tablename__ = 'files'
    id = Column(Integer, primary_key=True,index=True)
    original_filename = Column(String, nullable=False)
    stored_filename = Column(String, nullable=False)
    file_size = Column(Integer, nullable=False)
    uploader = Column(String, nullable=False)

    
class Folders(Base):
    __tablename__ = 'folders'
    id = Column(Integer, primary_key=True,index=True)
    name = Column(String, nullable=False)
    parent_folder_id = Column(Integer, ForeignKey("folders.id"))
    