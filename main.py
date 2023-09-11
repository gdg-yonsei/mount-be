from fastapi import FastAPI
import models
from database import engine
from routers import file, folder
import uvicorn

app = FastAPI()

models.Base.metadata.create_all(bind=engine)

app.include_router(file.router)
app.include_router(folder.router)

if __name__ == '__main__':
    uvicorn.run(app,host = '0.0.0.0',port = 8000)