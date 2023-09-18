# Step 2 [파일 및 폴더 (1) 구현]

## 요구 사항 
- 폴더를 생성하는 API
- 폴더 이름을 변경하는 API

### 세부 요구 사항
- 사용자 구분 
- 이미 존재하는 폴더 생성 시 오류 
- 파일의 메타데이터 정보에 위치 폴더 존재도 추가 
- 특정 폴더에 대한 요청 시 폴더에 포함된 파일 / 폴더의 메타데이터 목록 반환 

### 생각해볼 점 
- 파일이 특정 폴더에 있는지 어떻게 알 수 있을까?
- 폴더와 파일은 어떻게 구분할까?


## 구현 

### API Endpoint
- `GET /file/{username}` : Get user files
- `POST /file/{username}/upload` : Upload File for specific user
- `GET /file/{username}/download` : Download File for specific user
- `DELETE /file/{username}/delete` : Delete File for specific user
- `POST /folder/{username}/create` : Create Folder for specific user
- `GET /folder/{username}/{folder_name}` : Get children for input folder name 
- `GET /folder/{username}`: Get user folders
- `PUT /folder/{username}/update` : Update folder name

### Controller / Service 분리
- Controller : API 요청을 받아오고, 요청을 적절하게 가공하여 Service 레이어에 전달 
  - APIRouter 에 대한 로직들은 Controller 에 담았다. 
- Service : I/O 나 DB 요청, 데이터 가공 및 처리 진행 
  - DB에 파일 저장, DB에서 파일 이름 , 유저에 따른 파일 불러오기 등 DB 와 관련된 처리들은 service 레이어에 담았다. Controller 가 반복적으로 수행하는 코드들을 함수로 정리해서 코드가 복잡해지지 않고, 간결하게 표현할 수 있었다. 

### 폴더 생성

- Files 과 Folders Table 

    ```python
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
    ```

- File Storage Sytem 을 Tree 구조로 접근했다. 우선, root node 에 root folder 를 생성하고 (root folder 의 id = 1 , parent_id = 0) 그 후에 생성되는 모든 파일과 폴더들은 트리 구조로 부모 노드가 될 폴더의 parent_id 를 지정한다. 
- 따라서 Folders 의 속성에는 chilren 이 있다. 이것은 폴더와 파일을 구분해주는 차이점이라고 생각했다. 
- children 은 Array 형태이다. 부모 폴더에 더해지는 파일 또는 폴더의 메타데이터 정보를 json 형태로 추가했다. 
- 파일 또는 폴더 생성 시 parent_name , 부모가 될 폴더 이름을 입력받는다. 그 후 부모 폴더의 children 에 생성된 파일 또는 폴더의 정보를 추가한다. 
- root folder 를 구현할 때 고민이 많았는데, parent_name 에 start 를 입력하면 root 폴더가 생성되도록 했다. 만약 이렇게 안 하고 처음 root 폴더를 생성하면, 업데이트 할 parent folder 가 없어서 계속 오류가 났다. 하지만 다른 방법이 더 있을 것 같다 .. 

```json
  {
    "uploader": "soeun",
    "original_name": "myfolder",
    "stored_name": "myfolder",
    "modified_time": "2023-09-15T15:51:45",
    "parent_id": 1,
    "id": 10,
    "file_size": 0,
    "uploaded_time": "2023-09-15T15:16:35",
    "is_folder": true,
    "children": [
      "{\"name\": \"test1.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-15T15:24:06\"}",
      "{\"name\": \"test2.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-15T15:24:06\"}",
      "{\"name\": \"folder_in_myfolder\", \"is_folder\": true, \"created_time\": \"2023-09-15T15:51:45\"}"
    ]
  }
```

### 파일의 메타데이터 정보
- 파일의 메타데이터 정보에는 직속 부모의 id 만 추가했다. 그래서 tree 내에서 계속 tree 를 타고 올라가는 식으로 부모들을 알 수 있다. 

  ```json
  {
      "uploader": "soeun",
      "original_name": "test1.txt",
      "stored_name": "test1.txt_76d1f75371c14fc3a64c59a11e1c6070",
      "modified_time": "2023-09-15T15:24:06",
      "parent_id": 10,
      "id": 8,
      "file_size": 6,
      "uploaded_time": "2023-09-15T15:24:06",
      "is_folder": false
    }
  ```

### 특정 폴더에 대한 요청 시 폴더에 포함된 파일/폴더의 메타데이터 목록 반환
- 특정 폴더의 이름을 입력받으면 폴더의 children 값을 리턴하도록 했다. 파일 , 폴더가 생성되면서 계속 부모의 children 값을 업데이트 하므로, 이 목록을 반환 하는 것은 단순했다.
- 하지만 폴더 / 파일 생성 마다 부모의 children 값을 업데이트 해줘야 하므로 여기서 연산량이 많이 발생한다. 

  ```json
  [
    "{\"name\": \"test1.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-15T15:24:06\"}",
    "{\"name\": \"test2.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-15T15:24:06\"}",
    "{\"name\": \"folder_in_myfolder\", \"is_folder\": true, \"created_time\": \"2023-09-15T15:51:45\"}"
  ]
  ```

### 데이터베이스를 PostgreSQL 로 이동
- Chilren Column 을 Array 형태로 저장하고자 했는데, sqlite 는 Array 형태의 column 을 지원하지 않았다. 그래서 데이터베이스를 PostgreSQL 을 사용했다. 

-------
## PR 리뷰 반영

### Root 폴더 추가 로직 변경
**[Folder]**

- 회원 등록을 처음 했을 때, 자동으로 루트 폴더를 생성하면 되지 않겠나 ? 라는 피드백이 있었다. 루트 폴더를 사용자의 예약어로 생성하는 방식은 상당히 위험하다. 예약어를 두지 않고 자동으로 생성하는 방식으로 변경했다. 
- `check_first_user` 를 통해 User Table 에 입력한 username 이 있는 유저가 있는지 확인한다. 만약 없으면 (= 새로운 유저이면), 자동으로 이름이 root 인 폴더를 생성한다. 
- 만약 이미 유저가 존재한다면, 사용자가 입력한 대로 폴더를 생성한다. 

**[File]**
- 만약 새로운 유저가 파일을 업로드 한다면, root 폴더를 먼저 만들라고 403 Error 를 리턴한다. 

### File, Folder 테이블 칼럼 수정 & User 테이블 생성
- File, Folder 을 이미 구분하고 있음에도 is_folder 라는 칼럼을 두어 둘을 구분했다. 이것은 굳이 필요 없는 로직이라 생각하여 is_folder 칼럼을 삭제했다. 
- 로그인 기능을 구현하지는 않았지만, 유저는 관리하기 위해 username 만 관리하는 테이블을 만들었다. 
- 최종적인 테이블 칼럼은 아래와 같다. 

  ```python
  class Folders(Base):
      __tablename__ = 'folders'
      
      id = Column(Integer, primary_key=True,index=True)
      original_name = Column(String, nullable=False)
      stored_name = Column(String, nullable=True)
      uploader = Column(String, nullable=False)
      uploaded_time = Column(String, nullable=False)
      modified_time = Column(String, nullable=True)
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
      parent_id = Column(Integer,ForeignKey('folders.id'), default = 1)
      
  class Users(Base):
      __tablename__ = 'users'
      
      id = Column(Integer, primary_key=True,index=True)
      username = Column(String, nullable=False)
  ```

### Controller / Service 분리
- PR 리뷰에서 controller 에서 물리적으로 파일 저장, DB 저장 등 service 의 로직도 일부 포함되어 있다는 피드백을 받았다.
- 그래서 DB 와 관련된 모든 로직은 Service 로 분리했다. 


### 파일 삭제 시 , original_name 으로 삭제해도 괜찮은가 ?
- 여러 유저가 동일한 파일의 이름을 올릴 수 있다. 이때 os.remove(original_name) 을 하게 되면 어느 유저가 올린 파일을 지울지 특정할 수 없다. 
- 그래서 로컬에 저장할 때도 path 를 `uploads/{username}/file_name_{uuid}` 로 저장했다. os.remove 할 때도 저장했던 파일 경로를 이용해 삭제한다. 
- 그렇지만 사실 유저당 동일한 파일 이름은 한 개만 올릴 수 있으므로, 로컬에서 파일 삭제 경로를 {username}/{file_name} 으로 하면 한 개로 특정된다. DB 에서도 (유저, 파일 이름) 쌍은 하나만 존재하므로 original_name 으로 삭제해도 무방하다. 
- 하지만 추후에 같은 유저가 동일한 파일 이름을 여러 개 올릴 수 있다면, 내가 한 방식 처럼 original_name 말고 uuid 를 붙인 stored_name 을 찾아서 삭제하는 방식이 가장 정확할 것이다. 


----------------------------------------------------------------
## 질문 & 더 알아봐야 할 점 
- Service / Controller 의 분리에서 HTTPException Error가 나게 하는 로직은 service 단에서 처리되야 하는가 ? 
- 