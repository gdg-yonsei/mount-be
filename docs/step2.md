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
- `GET /folder/{username}`: Get ser folders
- `PUT /folder/{username}/update` : Update folder name

### 구현 방식
