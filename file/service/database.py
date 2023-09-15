from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base
import yaml

with open('config.yml') as f:
    conf = yaml.safe_load(f)

env = "dev"

if env == "dev":
    DATABASE_URL = conf['DevelopmentDatabaseURL']


engine = create_engine(DATABASE_URL,connect_args={'check_same_thread':False})

SessionLocal = sessionmaker(autocommit=False,autoflush=False, bind=engine)

Base = declarative_base()
