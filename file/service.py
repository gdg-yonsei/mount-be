import os
import uuid
from model.models import Files
from utils.utils import get_current_time
from folder.service import get_folder, update_children_file


current_time = get_current_time()

def make_file_path(file, username):
    unique_id = uuid.uuid4().hex
    stored_name = f"{file.filename}_{unique_id}"
    
    UPLOAD_DIR = os.path.join("uploads", username)
    if not os.path.exists(UPLOAD_DIR):
        os.makedirs(UPLOAD_DIR)
    file_path = os.path.join(UPLOAD_DIR, stored_name)
    
    return file_path , stored_name


# Save in both local(content) and database(metadata)
def save_file_data(db, file, username, parent_name):
    
    file_path, stored_name = make_file_path(file, username)
    
    with open(file_path, "wb") as f:
        f.write(file.file.read())
    
    parent_folder = get_folder(db, username, parent_name)
    
    uploaded_file = Files(
        original_name=file.filename,
        stored_name=stored_name,
        file_size=os.path.getsize(file_path),
        uploader=username,
        uploaded_time=current_time,
        modified_time=current_time,
        parent_id=parent_folder.id,
    )
    update_children_file(db, parent_name, username, uploaded_file)
    
    db.add(uploaded_file)
    db.commit()
    

def get_file(db , username : str, original_name : str):
    uploaded_file = db.query(Files).filter(
        Files.original_name == original_name,
        Files.uploader == username).first()
    return uploaded_file


# Delete from both local and database
def delete_file_data(db , username, uploaded_file):
    
    delete_file_path = os.path.join("uploads",username,uploaded_file.stored_name )
    
    try:
        os.remove(delete_file_path)
    except FileNotFoundError:
        pass
    
    db.delete(uploaded_file)
    db.commit()


