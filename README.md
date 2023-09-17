# Simple Cloud Storage

## Descriptions
기본적인 파일 과업들을 수행하는 클라우드 스토리지 RESTful API

현재 작업을 로컬에서 진행하므로, 내 로컬 저장소(하드디스크)가 파일 데이터를 저장하는 저장소의 역할을 하고 있음.

#### 1. 파일 업로드 기능
- Logic : 1) 저장소(파일 시스템 또는 클라우드 스토리지)에 파일을 물리적으로 저장하고, 2) 데이터베이스에 파일의 메타데이터를 추가
- 스프링에서 제공하는 MultipartFile 이라는 인터페이스를 이용해서, HTTP multipart 요청을 처리
- 로그인 구현이 되어 있지 않으므로, 파일을 업로드할 시 유저 정보도 URL 에 함께 전달
- 사용자들이 올리는 파일 이름이 같을 수 있으므로, 파일 이름 충돌 방지를 위해 UUID 활용한 랜덤 이름 사용

#### 2. 파일 삭제 기능
- Logic : 1) 데이터베이스에 파일의 메타데이터를 삭제하고, 2) 저장소에서 파일을 물리적으로 삭제
- 존재하지 않는 파일을 삭제 시도할 경우 ‘FileNotFoundException’ 발생

#### 3. 파일 다운로드 기능
- Logic : 1) DB 에 있는 파일 정보를 가지고, 3) 저장소에서 알맞은 파일 데이터를 찾아 다운로드
- 존재하지 않는 파일을 다운로드할 경우 ‘FileNotFoundException’ 발셍
- 본인이 만든 파일이 아닐 경우 ‘FileDownloadNotAllowedException’ 발생 

#### 4. 폴더 생성 기능
- Logic : 파일 업로드 기능과 동일
- 최초 폴더 생성 시, 랜덤한 이름으로 지정 (파일 이름 충돌 방지)

#### 5. 폴더 이름 변경 기능
- 이미 존재하는 폴더를 만들경우 ‘FolderNameDuplicateException’ 발생
- 폴더 이름 변경 시, 폴더 내 파일들의 경로도 함께 변경

#### 6. 폴더 정보 조회 기능
- 특정 폴더에 대한 요청 시 폴더에 포함된 파일/폴더의 메타데이터 목록을 반환


## API
#### 파일 
  1. **파일 업로드 API**
     - `POST` /api/v1/files/upload?file=example&user=example&parentId=1
  2. **파일 삭제 API**
     - `DELETE` /api/v1/files/{fileId}?user=example
  3. **파일 다운로드 API**
     - `GET` /api/v1/files/{fileId}/download?user=example
#### 폴더
  1. **폴더 생성 API**
     - `POST` /api/v1/folders?user=example&parentId=1
  2. **폴더 이름 변경 API**
     - `PUT` /api/v1/folders/{folderId}?user=example&new=newfoldername
  3. **특정 폴더에 대한 정보 조회 API**
     - `GET` /api/v1/folders/{folderId}?user=example
     - 설명: 특정 폴더 내의 하위 폴더와 파일 목록을 가져올 수 있다.



## Test Case

### Repository 테스트 (JPA CRUD)
* File Repository save 테스트
* File Repository findById 테스트
* File Repository deleteById 테스트

### Service 테스트
* File upload 테스트
* File delete 테스트 (TODO)
* File download 테스트


## TODO
* 실행 중인 service name 감지 및 적용
    - 현재는 http://localhost:8080 으로 고정
* Error Handling 고도화
  - 목적 : 예외 처리에 관한 자세한 정보를 제공하여 클라이언트 코드에서 문제 발생 시 빠르게 파악하기 위함
  - 파일 처리 시 발생하는 IOException, 파일 메타데이터 삭제 시 DB 오류 등
* 테스트 코드 작성

## Dependencies


| Dependency        | Version       |
| ----------------- | ------------- |
| Spring Boot       |   3.1.3       |
| Lombok            |               |
| Spring Boot Data JPA |               |
| MySQL Connector/J | 8.0.33        |
| Spring Boot Test  | testImplementation |