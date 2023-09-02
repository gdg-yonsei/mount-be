# mount-be

- 프로젝트 설명은 대화방 내 pdf 파일을 참고 바랍니다.

## Branch Guide

- gdsc-ys org에 존재하는 레포에 본인의 대표 브랜치를 생성합니다.
- 이후, 각 Step 마다 해당 브랜치를 베이스로 하는 새로운 브랜치를 생성합니다.
  - 이 경우, 각 브랜치의 이름은 `feature/{대표 브랜치 이름}_step0` 과 같은 방식으로 생성합니다.
  - PR은 대표 브랜치 <- Step 브랜치로 진행합니다.

## Git Convention

### 포맷

```jsx
type: subject

body
```

#### type

- 하나의 커밋에 여러 타입이 존재하는 경우 상위 우선순위의 타입을 사용한다.
- fix: 버스 픽스
- feat: 새로운 기능 추가
- refactor: 리팩토링 (버그픽스나 기능추가없는 코드변화)
- docs: 문서만 변경
- style: 코드의 의미가 변경 안 되는 경우 (띄어쓰기, 포맷팅, 줄바꿈 등)
- test: 테스트코드 추가/수정
- chore: 빌드 테스트 업데이트, 패키지 매니저를 설정하는 경우 (프로덕션 코드 변경 X)

#### subject

- 제목은 50글자를 넘지 않도록 한다.
- 개조식 구문 사용
  - 중요하고 핵심적인 요소만 간추려서 (항목별로 나열하듯이) 표현
- 마지막에 특수문자를 넣지 않는다. (마침표, 느낌표, 물음표 등)

#### body (optional)

- 각 라인별로 balled list로 표시한다.
  - 예시) - AA
- 가능하면 한줄당 72자를 넘지 않도록 한다.
- 본문의 양에 구애받지 않고 최대한 상세히 작성
- “어떻게” 보다는 “무엇을" “왜” 변경했는지 설명한다.

## Additional Requirement

반드시 수행할 필요는 없지만, 더 나은 BE 개발자가 될 수 있도록 도입을 검토해보면 좋습니다.

- 매 Step 마다 테스트 코드를 작성해보세요.
- Code Smell 및 Test Coverage 검증을 위한 다양한 도구를 검토해보세요.
  - Ex) Sonarqube, Qodana
- Code Convention 을 정의해서 코드를 작성해주세요.
  - Java
    - Naver: https://naver.github.io/hackday-conventions-java/
    - Google: https://google.github.io/styleguide/javaguide.html
  - Python: https://peps.python.org/pep-0008/
  - JavaScript
    - Airbnb: https://github.com/airbnb/javascript
    - Google: https://google.github.io/styleguide/jsguide.html

