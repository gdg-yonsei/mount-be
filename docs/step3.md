# Step 3 [파일 및 폴더 (2) 구현]

## 요구 사항
- 폴더 삭제
  - 폴더 삭제 시 , 하위의 모든 파일 & 폴더 삭제
- 폴더 / 파일 이동
  - 폴더 이동 시 하위의 모든 파일이 전부 이동
- 폴더 / 파일 복사

### 생각해볼 점
- 키워드 : DFS

--------------------------------------------------------

## 구현
### API Endpoint
- `PUT /file/{username}/move` : Move file for specific user
- `PUT /folder/{username}/move` : Move folder for specific user
###  폴더 삭제

#### 1) Folders Table 에 children 정보가 있는 경우

1. 삭제하고자 하는 폴더의 children 정보를 불러온다.
2. children_info 에 (name, is_folder) 여부를 저장한다. 
3. 
   - is_folder = True : 폴더면 `delete_folder_info` 를 다시 반복한다. (recursion)
   - is_folder = False : 파일이면 DB와 로컬에서 파일을 모두 삭제한다. 

- delete 를 file_name , folder_name 으로 하므로 파일이 없을 때 생기는 HTTPException 은 service 단으로 포함시켰다. 
- name 말고 id 를 사용해서 삭제하면 훨씬 빠를까?
- 폴더를 삭제하면 부모 폴더의 children 에서도 그 정보를 삭제해야하는데, 이게 생각보다 연산량이 꽤 많을 것 같다. 
- 만약 children 정보를 없애고, parent_id 만 사용해서 삭제한다면 어떻게 구현해야 할까?

**장점**
- 계속 children 에 정보가 추가 되므로, parent_id 로 찾을 필요 없이 children 정보만 불러오면 된다. 

**단점**
- children 이 끝없이 늘어나면 DB 내에 저장해야할 메타데이터 정보가 매우 늘어난다.
- 폴더 추가, 삭제 요청마다 부모의 children을 계속 업데이트 해주어야 한다. 
- sqlalchemy 는 딕셔너리 형태로 저장하는 것이 아니라, json 형태로 저장해주어야 했는데, 계속 Json <-> dictionary 변환 작업을 해주어야 했다. 굉장히 번거롭기도 하고 코드도 많이 지저분해졌다. 
- child_info 에 name, is_folder, create_time 여부를 저장하는데, 이름으로 검색하는 것이 Id 로 검색하는 것보다 빠르지 않을 것 같았다. 


**[실제 구현 결과]** 
- 삭제 전 
  - 폴더 구조
    |- root
    |----folder1
    |       |----folder1-1
    |       |        |----test3.txt
    |       |----test2.txt
    |----folder2
    |----test1.txt 
  - 
  ```json
  [
    {
      "uploaded_time": "2023-09-19T22:07:35",
      "original_name": "folder2",
      "stored_name": "folder2",
      "parent_id": 43,
      "uploader": "soeun",
      "id": 45,
      "modified_time": "2023-09-19T22:07:35",
      "children": null
    },
    {
      "uploaded_time": "2023-09-19T22:07:35",
      "original_name": "root",
      "stored_name": "root",
      "parent_id": 0,
      "uploader": "soeun",
      "id": 43,
      "modified_time": "2023-09-19T22:07:35",
      "children": [
        "{\"name\": \"folder1\", \"is_folder\": true, \"created_time\": \"2023-09-19T22:07:35\"}",
        "{\"name\": \"folder2\", \"is_folder\": true, \"created_time\": \"2023-09-19T22:07:35\"}",
        "{\"name\": \"test1.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-19T22:07:35\"}"
      ]
    },
    {
      "uploaded_time": "2023-09-19T22:07:35",
      "original_name": "folder1",
      "stored_name": "folder1",
      "parent_id": 43,
      "uploader": "soeun",
      "id": 44,
      "modified_time": "2023-09-19T22:07:35",
      "children": [
        "{\"name\": \"folder1-1\", \"is_folder\": true, \"created_time\": \"2023-09-19T22:07:35\"}",
        "{\"name\": \"test2.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-19T22:07:35\"}"
      ]
    },
    {
      "uploaded_time": "2023-09-19T22:07:35",
      "original_name": "folder1-1",
      "stored_name": "folder1-1",
      "parent_id": 44,
      "uploader": "soeun",
      "id": 46,
      "modified_time": "2023-09-19T22:07:35",
      "children": [
        "{\"name\": \"test3.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-19T22:07:35\"}"
      ]
    }
  ]
  ```

- folder1 삭제 후 
  ```json
  [
    {
      "uploaded_time": "2023-09-19T22:07:35",
      "original_name": "folder2",
      "stored_name": "folder2",
      "parent_id": 43,
      "uploader": "soeun",
      "id": 45,
      "modified_time": "2023-09-19T22:07:35",
      "children": null
    },
    {
      "uploaded_time": "2023-09-19T22:07:35",
      "original_name": "root",
      "stored_name": "root",
      "parent_id": 0,
      "uploader": "soeun",
      "id": 43,
      "modified_time": "2023-09-19T22:07:35",
      "children": [
        "{\"name\": \"folder2\", \"is_folder\": true, \"created_time\": \"2023-09-19T22:07:35\"}",
        "{\"name\": \"test1.txt\", \"is_folder\": false, \"file_size\": 6, \"created_time\": \"2023-09-19T22:07:35\"}"
      ]
    }
  ]
  ```

**내린 결론**
- 결국 삭제하는 것까지 구현 했으나 코드가 너무 복잡해지는 것 같아 다시 Folders Table 에 children 정보를 없애고, parent_id 로만 종속 관계를 표시하기로 했다. 
- 그러면 file / folder table 을 구분하는 점이 file_size 하나 뿐인데, 하나의 테이블로 관리하는 것이 낫지 않을까? 라는 의문이 들었다. 하지만 테이블을 나누어서 관리한다면, File 을 찾고 싶을 때는 File 테이블에 요청을 보내고, Folder 를 찾고 싶을 때는 Folder 테이블에 요청을 보낼 수 있어 더 빠르게 원하는 객체를 찾을 수 있을 것이라 생각했다. 

#### 2) Folders Table 에 children 정보가 없는 경우

1. 삭제하고자 하는 폴더의 id 를 parent_id 로 가진 모든 파일 / 폴더를 찾는다. 
2. 파일이면, `delete_file_info` 를 통해 파일을 삭제한다.
3. 폴더이면, recursion 을 통해 다시 한번 그 폴더를 parent_id 로 가진 파일 / 폴더를 찾아 위 작업을 반복한다. 

**장점**
- 모든 관계를 parent_id 를 통해서 찾는다. 
- 폴더 삭제 : 부모 폴더를 삭제하면, 부모 폴더를 

**단점**
- 저장하기는 쉬우나, 삭제 요청을 할 때, 모든 파일 / 폴더의 parent_id 를 다 검색해야 하므로 연산량이 많다. 
- 이것을 개선한 방식이 삭제하고자 하는 폴더의 id 이후에 있는 id 부터 찾는 방식이다. 

**실행결과**
- 삭제 전
  - 폴더 구조
    |- root 
    |----folder1
    |       |----folder1-1
    |       |        |----test1.txt
    |       |----test2.txt
    |----folder2
    |----test3.txt 
  - folder
    ```json
    [
      {
        "uploaded_time": "2023-09-20T00:35:46",
        "original_name": "root",
        "stored_name": "root",
        "parent_id": 0,
        "uploader": "soeun",
        "id": 55,
        "modified_time": "2023-09-20T00:35:46"
      },
      {
        "uploaded_time": "2023-09-20T00:35:46",
        "original_name": "folder1",
        "stored_name": "folder1",
        "parent_id": 55,
        "uploader": "soeun",
        "id": 56,
        "modified_time": "2023-09-20T00:35:46"
      },
      {
        "uploaded_time": "2023-09-20T00:35:46",
        "original_name": "folder1-1",
        "stored_name": "folder1-1",
        "parent_id": 56,
        "uploader": "soeun",
        "id": 57,
        "modified_time": "2023-09-20T00:35:46"
      },
      {
        "uploaded_time": "2023-09-20T00:35:46",
        "original_name": "folder2",
        "stored_name": "folder2",
        "parent_id": 55,
        "uploader": "soeun",
        "id": 58,
        "modified_time": "2023-09-20T00:35:46"
      }
    ]

    ```
  - file
    ```json
    [
      {
        "file_size": 6,
        "id": 42,
        "uploaded_time": "2023-09-20T00:35:46",
        "parent_id": 57,
        "original_name": "test1.txt",
        "stored_name": "test1.txt_005a4e8790fd407186238e9ad0c49fa3",
        "uploader": "soeun",
        "modified_time": "2023-09-20T00:35:46"
      },
      {
        "file_size": 6,
        "id": 43,
        "uploaded_time": "2023-09-20T00:35:46",
        "parent_id": 56,
        "original_name": "test2.txt",
        "stored_name": "test2.txt_05a0e4a58c3f43af8de7c66d962e0b1e",
        "uploader": "soeun",
        "modified_time": "2023-09-20T00:35:46"
      },
      {
        "file_size": 6,
        "id": 44,
        "uploaded_time": "2023-09-20T00:35:46",
        "parent_id": 55,
        "original_name": "test3.txt",
        "stored_name": "test3.txt_34b6b483ef654488b396c75718ffae19",
        "uploader": "soeun",
        "modified_time": "2023-09-20T00:35:46"
      }
    ]
    ```
- folder 1 삭제 후
  - folder
    ```json
    [
      {
        "uploaded_time": "2023-09-20T00:35:46",
        "original_name": "root",
        "stored_name": "root",
        "parent_id": 0,
        "uploader": "soeun",
        "id": 55,
        "modified_time": "2023-09-20T00:35:46"
      },
      {
        "uploaded_time": "2023-09-20T00:35:46",
        "original_name": "folder2",
        "stored_name": "folder2",
        "parent_id": 55,
        "uploader": "soeun",
        "id": 58,
        "modified_time": "2023-09-20T00:35:46"
      }
    ]
    ```
  - file
    ```json
    [
      {
        "file_size": 6,
        "id": 44,
        "uploaded_time": "2023-09-20T00:35:46",
        "parent_id": 55,
        "original_name": "test3.txt",
        "stored_name": "test3.txt_34b6b483ef654488b396c75718ffae19",
        "uploader": "soeun",
        "modified_time": "2023-09-20T00:35:46"
      }
    ]
    ```

**내린 결론**
- 결론적으로 children 없이 parent_id 만으로도 폴더 삭제를 recursion 을 사용해서 구현할 수 있었다. 
- 모든 정보를 모두 저장하기 보다 관계를 가지고 구현할 수 있는지부터 생각하자 ! 훨씬 간결하게 구현할 수 있었다. 
- 하지만 클라우드 스토리지가 확장되면, 전체 파일 / 폴더 테이블에서 parent_id 를 검색하는 것의 연산량이 매우 클 것 같다. 나중에 부하 테스트를 통해 검증해봐야겠다 ! 

### 폴더 / 파일 이동
1. 이동하고자 하는 폴더의 id 를 구한다. 
2. 이동하는 폴더 / 파일의 parent_id 를 이동하고자 하는 폴더의 id 로 업데이트 한다. 
3. 직속 부모 id 만 저장하고 있으므로, 폴더를 다른 폴더 내부로 이동하면 parent_id 만 변경해주면 된다. 

**내린 결론**
- 가장 상위에 있는 폴더의 직속 parent_id 만 바꿔주면, 그 폴더의 하위에 있는 모든 폴더들도 옮겨지므로 아주 간단했다. 
  <img width="642" alt="image" src="https://github.com/ddoddii/ddoddii.github.io/assets/95014836/1dcac11b-edf2-4859-8ed9-49a77c13ef02">

