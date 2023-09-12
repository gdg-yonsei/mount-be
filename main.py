from fastapi import FastAPI
from file.controller import filecontroller
import file.service.filemodels as filemodels
from file.service.database import engine

import uvicorn
import os


app = FastAPI()


filemodels.Base.metadata.create_all(bind=engine)

app.include_router(filecontroller.router)
#app.include_router(foldercontroller.router)

if __name__ == '__main__':
    uvicorn.run(app,host = '0.0.0.0',port = 8000)