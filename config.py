'''
config.py
'''
import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    DATABASE_URL = os.getenv('DATABASE_URL')
    HOST = os.getenv('HOST')
    PORT = int(os.getenv('PORT'))
    
    