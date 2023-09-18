from fastapi import FastAPI
import uvicorn

from file.controller import fileController
from folder.controller import folderController
from user.controller import userController

import model.models as models
from model.database import engine


app = FastAPI(title= "File Storage System")

models.Base.metadata.create_all(bind=engine)

app.include_router(fileController)
app.include_router(folderController)
app.include_router(userController)


if __name__ == '__main__':
    uvicorn.run(app,host = '0.0.0.0',port = 8000)