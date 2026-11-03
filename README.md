# Delibuddy

## 프로젝트 규칙

### git

브랜치 전략은 GitHub Flow를 따른다.
`main`은 항상 배포 가능한 상태로 유지한다.

1. `main`에서 작업 브랜치를 생성한다.
2. 작업을 완료하면 PR을 생성한다.
3. 코드 리뷰와 필요한 검증을 받고 `main`에 머지한다.
4. 머지한 작업 브랜치는 삭제한다.

커밋 메시지는 Conventional Commits 형식으로 작성한다.
코드 리뷰를 위해 본인이 작성한 코드에 대해 가능한 자세히 커밋 본문을 작성한다.

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `refactor`: 기능 변경 없이 코드 구조 개선
- `chore`: 빌드 설정, 의존성 등 기타 작업
- `docs`: 문서 변경

```text
<type>: <변경 내용 요약>

// 예시
feat: 채팅 화면 추가
refactor: 방 상태 관리 구조 변경
```

브랜치명은 `<type>/<작업 내용>` 형식으로 작성한다.
작업 내용은 케밥케이스를 사용한다.

- `feat/`: 새로운 기능 추가
- `fix/`: 버그 수정
- `refactor/`: 기능 변경 없이 코드 구조 개선
- `chore/`: 빌드 설정, 의존성 등 기타 작업
- `docs/`: 문서 변경

```text
// 예시
feat/signup-ui
fix/system-bar-padding
refactor/auth-repository
chore/update-dependencies
docs/branch-convention
```

### architecture

```text
com.example.delibuddy/
├── MainActivity.kt
├── data/
│   └── <기능>/
│       ├── <기능>Model.kt
│       └── <기능>Repository.kt
└── ui/
    └── <화면>/
        ├── <화면>Fragment.kt
        └── <화면>ViewModel.kt
```

- 데이터 코드는 `data/<기능>` 패키지에 두고 모델과 저장소 한다.
- 화면 코드는 `ui/<화면>` 패키지에 두고 Fragment와 ViewModel을 함께 둔다.
- 차후 변경될 수 있음.
