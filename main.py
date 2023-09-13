'''
main.py
'''
from fastapi import FastAPI
from file.controller import fileController
from database import engine, Base
import uvicorn

app = FastAPI()

Base.metadata.create_all(bind=engine)
app.include_router(fileController)
if __name__ == "__main__":
    uvicorn.run("main:app", host='0.0.0.0', port=8000, reload=True)
