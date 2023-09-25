# Step 4 [폴더 요약 구현 사항]

## 요구 사항

- 폴더의 정보를 확인할 수 있는 기능 구현

<img width="401" alt="image" src="https://github.com/ddoddii/ddoddii.github.io/assets/95014836/809bef78-a5bd-433d-ad4b-28f24d619dd4">

- 만든 날짜는 최초 1회에 한 해 설정되며, 절대 변경되지 않는다. 수정한 날짜는 다음과 같은 과업이 수행될 때 변경된다.
	- 파일, 폴더가 생성된 경우
	- 파일, 폴더의 이름이 변경되거나, 위치가 변경된 경우
	- (폴더의 경우) 해당 폴더 내에서 파일 과업 (업로드, 삭제, 이름 변경, 복사, 이동)이 발생한 경우

## 구현 

### API Endpoint
- `/folder/{username}/{folder_name}` : Get folder data for specific folder

### 폴더 정보 
#### 종류
Folder 이므로 'folder' 리턴
 
#### 경로
1. parent.original_name 이 "root" 가 될 때까지 거슬러 올라간다. 
2. 그 경로를 다 더해서 리턴한다.
3. ex. folder1-1 -> folder1 -> root 면 root/folder1/folder1-1/ 리턴 

#### 크기
안에 있는 파일들의 file_size 를 모두 더해서 리턴한다. 

#### 내용 
1. 바로 직속 자식의 폴더의 개수를 센다.
2. 바로 직속 자식의 파일 개수를 센다.

#### 만든 날짜
uploaded_time 을 리턴한다. 

#### 수정한 날짜
uploaded_time 을 리턴한다. 

### 최종 형태

만약 folder1-1 에 대한 정보를 구한다면, 아래와 같은 형태로 출력된다. 

```json
{
  "type": "folder",
  "path": "root/folder1/folder1-1/",
  "size": 6,
  "inside": "files : 1, folders : 2",
  "upload_time": "2023-09-20T01:13:34",
  "modified_time": "2023-09-25T16:43:25"
}
```


