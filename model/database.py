from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base

SQLALCEMY_DATABASE_URL = 'postgresql://postgres:test@localhost/FileSystemDatabase'

engine = create_engine(SQLALCEMY_DATABASE_URL)

SessionLocal = sessionmaker(autocommit=False,autoflush=False, bind=engine)

Base = declarative_base()