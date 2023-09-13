# Simple Cloud Storage

## Descriptions
기본적인 파일 과업들을 수행하는 클라우드 스토리지 RESTful API

현재 작업을 로컬에서 진행하므로, 내 로컬 저장소(하드디스크)가 파일 데이터를 저장하는 저장소의 역할을 하고 있음.

#### 1. 파일 업로드 기능
- Logic : 1) 파일 관련 메타데이터를 DB 에 저장하고, 2) 파일 데이터는 저장소(파일 시스템 또는 클라우드 스토리지)에 저장
- 스프링에서 제공하는 MultipartFile 이라는 인터페이스를 이용해서, HTTP multipart 요청을 처리
- 로그인 구현이 되어 있지 않으므로, 파일을 업로드할 시 유저 정보도 URL 에 함께 전달
- 사용자들이 올리는 파일 이름이 같을 수 있으므로, 파일 이름 충돌 방지를 위해 UUID 활용한 랜덤 이름 사용

#### 2. 파일 삭제 기능
- Logic : 1) DB로부터 파일 메타데이터 삭제하고, 2) 저장소에서 파일을 물리적으로 삭제
- 존재하지 않는 파일을 삭제 시도할 경우 ‘FileNotFoundException’ 발생

#### 3. 파일 다운로드 기능
- Logic : 1) DB 에 있는 파일 정보를 가지고, 3) 저장소에서 알맞은 파일 데이터를 찾아 다운로드
- 존재하지 않는 파일을 다운로드할 경우 ‘FileNotFoundException’ 발셍
- 본인이 만든 파일이 아닐 경우 ‘FileDownloadNotAllowedException’ 발생 

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