# Delibuddy

## git

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

## architecture

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

### Repository 비동기 처리

- Firebase `Task`는 Repository 안에서 `await()`로 기다리고 결과를 반환한다.
- 데이터 변경을 계속 관찰하는 작업은 `Flow`로 제공한다.

```kotlin
// Repository
suspend fun getById(userId: String): UserModel? =
    users.document(userId).get().await().toObject(UserModel::class.java)

// ViewModel
val user = userRepository.getById(userId)
```

### Fragment 코드 스타일

`onViewCreated()`에는 화면 초기화 순서가 보이도록 설정과 상태 관찰 함수를 호출한다.
뷰의 리스너 등록은 `setUpListeners()`처럼 화면이나 기능 단위로 묶는다.
입력값 설정 등 관련 초기화가 함께 필요한 경우에는 해당 기능의 설정 함수에 작성한다.

- 리스너 안의 동작이 짧고 의도가 분명하면 그대로 작성한다.
- 동작이 길거나 여러 곳에서 사용한다면 이름을 붙여 별도 함수로 분리한다.
- 리스너마다 함수를 하나씩 만들기보다 관련된 리스너를 함께 묶는다.

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setUpListeners()
    observeBirthDate()
}

private fun setUpListeners() {
    binding.buttonBirthDate.setOnClickListener { showDatePicker() }
    binding.buttonSkip.setOnClickListener {
        viewModel.setBirthDate(null)
        navigateToAccountInfo()
    }
    binding.buttonNext.setOnClickListener { navigateToAccountInfo() }
}
```

### 스타일링

- 색상, 크기, 여백, 배경, 글꼴 등 스타일에 관련된 내용은 최대한 XML에 작성한다.
- 여러 화면에서 사용하는 스타일은 공용 리소스로 분리하고, 눌림이나 비활성화에 따른 모양은 XML selector로 정의한다.
- Kotlin에서는 화면 상태에 따라 리소스와 표시 여부를 선택하는 코드만 작성한다.

### 코루틴 예외 처리

viewModel 코루틴에서 `Exception`을 처리할 때 `CancellationException`을 먼저 던져 취소가 전달되도록 한다.

```kotlin
try {
    repository.save()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    // 작업 실패 처리
}
```

시간 초과를 오류 화면으로 보여주는 것처럼 취소를 의도적으로 먼저 처리해야 하는 경우는 예외.
