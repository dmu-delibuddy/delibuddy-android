[33mcommit 5676d2ac49a233b449203e7648b1e74093947bd4[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mMap[m[33m, [m[1;31morigin/main[m[33m, [m[1;31morigin/Map[m[33m, [m[1;31morigin/HEAD[m[33m)[m
Merge: 18a7f87 8430242
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Tue Nov 17 22:09:00 2026 +0900

    Merge pull request #45 from dmu-delibuddy/feat/room-detail-profile-tab
    
    Feat/room detail profile tab

[33mcommit 84302428a26745402ceb035ef990f16beb0c5fdb[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 22:05:38 2026 +0900

    feat: 참여자 카드에 방장과 본인 표시

[33mcommit 0cf8b63b2f51b346a837db461ece55e2e30a4147[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 21:59:43 2026 +0900

    feat: 방 상세 참여자 프로필 다이얼로그 연결
    
    참여자 카드를 누르면 방 모드에 맞는 프로필을 표시한다.
    
    다른 사용자의 비공개 사용자 문서 대신 프로필 문서를 조회하고, 다이얼로그의 중복 표시를 방지한다.

[33mcommit 7f4f7946dbdd2c6337ea7ac4ba6a58f04eb7b354[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 21:43:32 2026 +0900

    fix: 참여자 기본 아이콘의 원형 배경 제거

[33mcommit 18a7f873f19ad4ed250d0b26f95c7f65b46895bf[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 21:17:15 2026 +0900

    refactor: 프로필 모달 코드를 common/profile 패키지로 이동

[33mcommit 272bab2f2dd67709febc85919446e33e1a5ea07c[m
Merge: 9678b3e 31a2ce8
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Tue Nov 17 21:03:48 2026 +0900

    Merge pull request #44 from dmu-delibuddy/feat/profile-modal-v2
    
    feat: 프로필 모달 추가

[33mcommit 31a2ce82b3bb9dcada2e82ae003aafb5ee6791de[m
Author: kdahey <132222550+kdahey@users.noreply.github.com>
Date:   Tue Nov 17 20:37:54 2026 +0900

    feat: 프로필 모달 화면과 ViewModel 추가
    
    - ProfileDialogFragment: 다른 사용자 프로필 모달. 불러오는 동안 GlobalLoadingDialog를 띄우고, 사진은 loadImage로 표시합니다.
    - ProfileViewModel: UserRepository로 사용자를 확인한 뒤 ProfileRepository에서 모드별 프로필을 읽습니다. README의 suspend 방식과 CancellationException 처리 규칙을 따랐습니다.
    - 프로필 문서가 없으면 마이페이지처럼 빈 프로필로 보여줍니다.
    - onViewCreated는 setUpListeners, observeProfile, loadProfile 순서로 나눴습니다.

[33mcommit bc1f066810b05b43a750d5b0d38bc21dd47df01f[m
Author: kdahey <132222550+kdahey@users.noreply.github.com>
Date:   Tue Nov 17 20:36:35 2026 +0900

    feat: 프로필 모달 레이아웃 추가

[33mcommit 63f470a827db191584d7cfe91162bcb024f98ba9[m
Author: kdahey <132222550+kdahey@users.noreply.github.com>
Date:   Tue Nov 17 20:33:18 2026 +0900

    feat: 프로필 모달 문구 추가

[33mcommit 9678b3eaad845966f6fc77c7687a3df8b114f7cc[m[33m ([m[1;32mmain[m[33m)[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 20:11:57 2026 +0900

    feat: Repository 함수 규칙 작성

[33mcommit 55da622005b4162c98d7f3ba7781fa1370ec16de[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 20:09:55 2026 +0900

    refactor: Repository 비동기 처리를 suspend함수로 통일

[33mcommit 6c012690e68ce310378a4141e82fa3b0b039e121[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 19:39:40 2026 +0900

    feat: 배달비 상태를 readonly타입으로 변경

[33mcommit 3207503286131beaef386e1f3e763d79fbf62eca[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 19:36:23 2026 +0900

    docs: viewModel에서 코루틴 사용에 대한 규칙 작성

[33mcommit 7cfe1ecb82f78c2f84b2bd40da6470ece63e124e[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 19:25:11 2026 +0900

    refactor: 프로젝트 코드 스타일 정리

[33mcommit 502cb20277ea6f3f30e067b202f3617751ab6dc0[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 19:19:09 2026 +0900

    fix: 코루틴 취소 예외 전달

[33mcommit f6397fa6cc0f3248e7cf62fb3e56882ef2a5d655[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 17:57:57 2026 +0900

    docs: Fragment 코드 스타일에 대한 규칙 작성

[33mcommit 2ca2d8f2456f6810d6f03f721a755de8a7ca38e8[m
Merge: 83d0d76 1ae390b
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Tue Nov 17 17:46:08 2026 +0900

    Merge pull request #43 from dmu-delibuddy/refactor/fragment-listener-setup
    
    refactor: Fragment 코드 스타일 통일

[33mcommit 1ae390b741247adb22f686bb82a2db644a49eb5a[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 17 17:43:17 2026 +0900

    refactor: Fragment 코드 스타일 통일

[33mcommit 83d0d766c8ae5c69bf3e4c676a55c30515bf3771[m
Merge: b311cb8 5dbf0dd
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Tue Nov 17 13:39:34 2026 +0900

    Merge pull request #42 from dmu-delibuddy/fix/order-price-padding
    
    fix: 음식값 입력칸 글자가 박스 그림자에 겹치는 문제 수정

[33mcommit 5dbf0dd89374657cc5a809d753fe6579811c3664[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Tue Nov 17 10:47:42 2026 +0900

    fix: 음식값 입력칸 글자가 박스 그림자에 겹치는 문제 수정

[33mcommit b311cb87fefe90231bebfe626b325c97aadcfc4f[m
Merge: 56c6144 90cd1d6
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Mon Nov 16 23:57:54 2026 +0900

    Merge pull request #41 from dmu-delibuddy/feat/global-ui
    
    Feat/global UI

[33mcommit 56c6144e9158fc4c01fd3b8df8e17375ee496901[m
Merge: 8f971f4 efcb1a5
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Mon Nov 16 23:30:02 2026 +0900

    Merge pull request #40 from dmu-delibuddy/feat/chat-management-tab
    
    Feat/chat management tab

[33mcommit 90cd1d6a254dfb65d1c4dc84891467c4b9b8c6e0[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:52:27 2026 +0900

    feat: 마이페이지 아이콘 + 제목 추가

[33mcommit efcb1a5a8596595ed70fef698a0f89a8c3138aba[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:27:37 2026 +0900

    feat: 채팅방 상단 정보 표시

[33mcommit fd6e0f7231769be8499780ea50ca341722341770[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:47:55 2026 +0900

    feat: 앱에서 액션바 제거 & 홈 프래그먼트에 액션바 디자인 그대로 채용해서 사용

[33mcommit 0330b2b9b1e8c7582a6a7fc2989a883a2b85c493[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:25:47 2026 +0900

    feat: 채팅 카메라 사진 전송 추가

[33mcommit d89635f21691b3221046a87f8ae1e80afb5c8c8c[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:42:33 2026 +0900

    feat: 마이페이지에서 마이페이지 제목 텍스트뷰 제거

[33mcommit 051fbdacad8976170deaa07c9e850b35f1da5938[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:05:17 2026 +0900

    feat: 채팅 메뉴와 참여자 패널 추가

[33mcommit 5076bd965bd3a04061efa18f3385bc7360cd3aa1[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:42:09 2026 +0900

    feat: 앱 전역 폰트 변경

[33mcommit 12ee53566c54905e343fde8eaca701dfdf60c9e5[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 23:05:10 2026 +0900

    fix: 주문 화면의 사진 아이콘 참조 수정

[33mcommit 416f1d332b962174dc10e344f2cdf679eb2e1e7f[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 22:08:11 2026 +0900

    feat: 메뉴 탭에서 사용할 아이콘 추가

[33mcommit 76b04a486a96dd11810a72b8d1634ec318546972[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Nov 16 21:07:36 2026 +0900

    feat: 채팅방 이미지 버튼 텍스트만 +로 변경

[33mcommit 8f971f47ff33d3787f9c5e768b73c4de43b99761[m
Merge: 7972a80 62d8a4f
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Mon Nov 16 09:44:39 2026 +0900

    Merge pull request #38 from dmu-delibuddy/feat/delivery-app
    
    feat: 방 만들기에 사용할 배달 앱 선택 추가

[33mcommit 7972a8086c87388bb5c08b14b4df4a66294a9c3a[m
Merge: 259b990 cde9cd7
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Mon Nov 16 09:44:22 2026 +0900

    Merge pull request #37 from dmu-delibuddy/refactor/remove-room-deadline
    
    refactor: 방 마감 시간 제거

[33mcommit 62d8a4fc4643806a2296131d60de1dd692e41bc7[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Mon Nov 16 02:06:33 2026 +0900

    chore: 배달 앱 아이콘 임시 이미지 추가
    
    - 공식 로고를 넣기 전까지 빌드가 되도록 앱 이름 글자만 적은 임시 이미지
    - 공식 로고로 교체할 때는 같은 파일 이름으로 덮어쓰기

[33mcommit c72e945ef70337d4bff461115ce9929ba6060eaf[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Mon Nov 16 02:01:43 2026 +0900

    feat: 방 만들기에 사용할 배달 앱 선택 추가
    
    - 3단계에 배달 앱 선택 : 배민 / 쿠팡이츠 / 요기요 는 아이콘 버튼, 그 외는 직접 입력
    - RoomModel 에 deliveryApp(enum), deliveryAppName(직접 입력 이름) 추가, 예전 방은 '기타' 로 표시
    - 방 카드 태그를 '배달 앱 · 음식점' 으로, 방 상세에 배달 앱 줄 추가
    - 미선택 / 직접 입력 이름 누락 검사

[33mcommit cde9cd7fb3fc625d872d41c8483271a46a50087d[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sun Nov 15 23:53:02 2026 +0900

    refactor: 방 마감 시간 제거
    
    - 방 만들기 3단계에서 마감 시간, 시간 제한 없음 입력 제거 (3단계는 장소만, 배달 앱 선택 추가 예정)
    - RoomModel 의 deadline, isDeadLinePassed 제거, 참여 / 주문 가능 여부는 방 상태로만 판단
    - 방 목록 정렬을 마감 순에서 생성 순으로 변경
    - 방 카드, 방 상세의 마감 표시 제거
    - firestore.rules : orders 규칙의 deadline 조건 제거 (콘솔 게시 필요)

[33mcommit 259b9902df13dbfe34fd5e021514b96c0a22b526[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sun Nov 15 12:02:57 2026 +0900

    fix: image document 안에 불필요한 id 저장 방지

[33mcommit 3af27110ea0367c2c99ca3205fa7d80e24521b4e[m
Merge: b11b654 2c62d3e
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sun Nov 15 11:50:36 2026 +0900

    Merge pull request #36 from dmu-delibuddy/refactor/chat-image-id
    
    Refactor/chat image

[33mcommit 2c62d3ea0e55ee72e76129a01e9104d0e49a5409[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sun Nov 15 11:47:50 2026 +0900

    fix: ImageRepository 메타데이터 삭제 코드 추가

[33mcommit a0883c227cfdb8160728b63cd675c8fc0301ab96[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sun Nov 15 11:45:08 2026 +0900

    feat: ChatData구조 변경에 따른 이미지 렌더 방식 및 저장, 호출 방법 번경

[33mcommit 59b94b4f8f4fb5d558a70cb72e1d28fb986c93dc[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sun Nov 15 11:43:07 2026 +0900

    feat: ChatRepository에서 ImageRepository 의존성 삭제 및 이미지ID를 저장하는 구조로 변경

[33mcommit b11b654e5988b551a993237c14d2f212eca46562[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 22:52:00 2026 +0900

    feat: 프로필 수정 중에 로딩 화면 렌더

[33mcommit 1593444ffb6fb3424f9d2384e14d1d1dc972df13[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 22:46:13 2026 +0900

    style: 버튼 스타일 통일

[33mcommit 77125a3064a11d8831cf74020970c3dd6fc1629e[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 22:36:41 2026 +0900

    feat: 회원가입에서 은행명 선택을 드롭다운 메뉴로 변경

[33mcommit 43a568477c6de6b8858d6906e49c3d1ceebbd66d[m
Merge: a68a556 0d5fac1
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 22:35:46 2026 +0900

    Merge pull request #35 from dmu-delibuddy/fix/bottom-nav-padding
    
    fix: 하단 네비게이션 바 글자가 바닥에 붙어 보이는 문제 수정

[33mcommit 0d5fac18c690edc86279c3b4f33ce30f10dfe1ec[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Nov 7 22:33:19 2026 +0900

    fix: 하단 네비게이션 바 글자가 바닥에 붙어 보이는 문제 수정
    
    - 아이콘을 35dp 로 키웠는데 바 높이는 기본값이라 글자가 아래로 밀려 바닥에 붙어 보이는 거였음
    - 바 높이를 76dp 로 고정, itemPaddingTop 8dp, itemPaddingBottom 10dp 추가
    - 아이콘 크기, 색, 배경을 변경사항 없음

[33mcommit a68a5569c3da3401eed35641574b042b1df82d71[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Nov 7 22:19:56 2026 +0900

    fix: 모드 이름을 픽업만 / 밥친구로 통일

[33mcommit 3b6924ac3cd9b2c141b4395797065ce3c8848759[m
Merge: 6490553 5f15f54
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 21:32:25 2026 +0900

    Merge pull request #34 from dmu-delibuddy/feat/join-requires-account
    
    feat: 송금 계좌 미등록 시 방 참여 제한

[33mcommit 6490553d5ebf0603c3083bd5b9e868309e312201[m
Merge: 9782554 ba71bd0
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 21:32:02 2026 +0900

    Merge pull request #33 from dmu-delibuddy/feat/mypage
    
    feat: 마이페이지 모드별 프로필·송금 계좌 수정 구현

[33mcommit 5f15f5465e36ba192f3c245c8622783e9fa32fa5[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Nov 7 21:15:23 2026 +0900

    feat: 송금 계좌 미등록 시 방 참여 제한
    
    - 참여하기 직전에 users 문서의 은행, 계좌번호를 확인
    - 없으면 RoomException.ACCOUNT_REQUIRED 로 막고 기존 실패 토스트로 안내
    - 회원가입에서 계좌 입력을 건너뛸 수 있어서 필요

[33mcommit ba71bd0ff21a7f9fece9cbb97c9ff1101ad04c34[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Nov 7 16:36:40 2026 +0900

    feat: 마이페이지 모드별 프로필·송금 계좌 수정 구현
    
    - feat/profile(김다혜) 작업을 main 최신 기준으로 옮기고 도메인별(profile / account) 패키지로 정리
    - 모드 버튼으로 픽업만 / 밥친구 프로필을 골라 보고, 고른 모드의 프로필만 수정
    - 프로필 수정 창 : 사진 변경 / 기본 사진으로, 닉네임, 한 줄 소개, 생년월일 달력
    - 송금 계좌 변경 창 : 은행 선택, 숫자만 입력 검사
    - 수정 창을 DialogFragment 로 바꾸고 마이페이지와 도메인 ViewModel 공유
    - 레이아웃을 layout_mypage_profile / layout_mypage_account 로 나눠 include
    - id snake_case, 문구 strings.xml(mypage_), cake_icon 2MB -> 5KB

[33mcommit 3ca0a4fa5e63ac2e3eeb91a36f411b7a2eaba7d5[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Nov 7 16:35:51 2026 +0900

    feat: 모드별 프로필 구독·저장 및 유저 부분 수정 함수 추가
    
    - ProfileRepository : observe(uid, type) 실시간 구독, save(uid, profile) 추가 (기존 함수 변경 없음)
      save 는 #28 이전 가입 계정처럼 프로필 문서가 없어도 저장되도록 set 사용
    - UserRepository : updateBirthDate, updateAccount 추가
      기존 update 는 4개 필드를 모두 덮어써서 화면별로 들고 있는 예전 값으로 다른 필드를 덮는 문제를 막기 위함

[33mcommit 9782554fa90f45139181789cfa344457cb2c4ab1[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 19:43:47 2026 +0900

    feat: 참여자 빈자리 및 모집 안 함 아이콘 적용

[33mcommit b444a36aa727d2ecb65870cae9e3a9edc24b0568[m
Merge: 1318211 5016800
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 19:20:14 2026 +0900

    Merge pull request #32 from dmu-delibuddy/feature/chat-sender-profile
    
    feat: 채팅 메시지에 상대방 프로필 표시

[33mcommit 501680010ac17b84421e8988f98e6ce9e8513e4f[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 19:18:17 2026 +0900

    feat: 채팅 메시지에 상대방 프로필 표시

[33mcommit 1318211bee8e28777703a020501ac0a543302b18[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 19:06:36 2026 +0900

    style: 로딩 UI 애니메이션 픽셀 느낌 살리기. 5단계로 나눠서 렌더

[33mcommit b8363592f4c4295fb416bb6e1b04842752be6c8f[m
Merge: 4c81a4c 422aff3
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 19:04:00 2026 +0900

    Merge pull request #31 from dmu-delibuddy/feat/room-participant-profiles
    
    Feat/room participant profiles

[33mcommit 422aff3500267905198af239ab303acc8dd84e47[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 18:45:41 2026 +0900

    refactor: 방 목록 화면 패키지 분리

[33mcommit 5d4e51eef809df7534ba80e5edf753423f916f12[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 18:43:15 2026 +0900

    refactor: 방 상세 참여자 표시와 주문 처리 분리

[33mcommit c1e3a1ede23b739f04cbe5aa2edc01c404023823[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 18:39:33 2026 +0900

    style: 참여자 카드 상태별 테두리 구분

[33mcommit cbc3e3e532c3f767029e8809b04cf4c39d571d61[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 18:39:33 2026 +0900

    feat: 방 모드에 맞는 참여자 프로필 표시

[33mcommit 4c81a4cf205040c519d3b245d17ff291f139788d[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 18:04:34 2026 +0900

    fix: 이미지 크기만 확인하는 디코딩에서 null을 반환하면 파일 읽기 실패로 판단하는 에러 수정

[33mcommit 9b75a61929cec60c3815b9b0115c3d09d4e14af6[m
Merge: 885ad23 f5be5a5
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 17:40:14 2026 +0900

    Merge pull request #30 from dmu-delibuddy/feat/auth-ui
    
    Feat/auth UI

[33mcommit f5be5a5d41db5acbe4886b23d54236c22fefad47[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 17:21:19 2026 +0900

    feat: auth_entry와 startup 로고 똑같이 만듦

[33mcommit 62aa1226ef0c6f1e0cb949298ad674c89f3e0622[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 17:20:40 2026 +0900

    feat: 로그인 중에 로딩 UI가 나오도록 설정

[33mcommit 885ad23b4bafd5b39a73e1fb2c31f3e82819b4e3[m
Merge: 5c95bcc 25bb13c
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 16:03:07 2026 +0900

    Merge pull request #29 from dmu-delibuddy/refactor/user-profile-subcollection
    
    refactor: 프로필을 사용자 문서 하위로 이동

[33mcommit 25bb13c3c5d8fe76d69215c4925c87aabc67cc09[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 16:01:18 2026 +0900

    refactor: 프로필을 사용자 문서 하위로 이동

[33mcommit 5c95bcc859232abb9734309cc083b5d9a8f364f2[m
Merge: 6bc872f 71f4ace
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 15:34:45 2026 +0900

    Merge pull request #28 from dmu-delibuddy/feat/create-profile-on-signup
    
    Feat/create profile on signup

[33mcommit 71f4acef96f6b3381d79a980fdb69b86bdd8077a[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 15:30:24 2026 +0900

    feat: 회원가입 시 프로필 생성하도록 코드 추가

[33mcommit da0553389154611b5eafa02fe2f7ebdefa60bd3e[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 15:29:49 2026 +0900

    feat: firestore profile 저장 규칙 업데이트

[33mcommit 94375ce35f3147b24a0cd79c2a4e23e1cb08cc76[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 15:26:20 2026 +0900

    feat: 프로필 모델 & 레포지토리 정의

[33mcommit 6bc872fe9235efbd08703ef76f0118aa814cf32e[m
Merge: aff85ad d7ee741
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 14:03:45 2026 +0900

    Merge pull request #27 from dmu-delibuddy/feat/order
    
    Feat/order

[33mcommit d7ee74141f969b1c3d12b5eeb1eb7d274d91a054[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 12:05:45 2026 +0900

    feat: RoomDetailPage 하단에 나타나는 버튼은 '방 참가', '채팅방으로 이동' 두개만 남김

[33mcommit 5f36c49db9e7e6e1ab47851639badaf46703f4a0[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 11:53:31 2026 +0900

    feat: 참여자 카드 레이아웃 스타일 변경

[33mcommit 92b84046ec6e609580b40b97d59752bd6c6c52a9[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Sat Nov 7 11:35:53 2026 +0900

    feat: 이벤트 리스너 코드 레이아웃 분리에 따라 변경

[33mcommit 85b973da4d5df514a69a32c4d5b5818e79943773[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 20:37:36 2026 +0900

    feat: room detail fragment 레이아웃 두개 분리

[33mcommit eac33235a3d499e727a8a387e375d3758dbac64c[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 20:35:21 2026 +0900

    feat: order 레이아웃 추가

[33mcommit 9019132744bc4e174ac0669fe9c99ae9eb8feb20[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 20:16:10 2026 +0900

    feat: 방 상세화면 메뉴 입력 컴포넌트에 들어갈 아이콘 추가

[33mcommit 3d2d4519d04c1185d2a74a97278fa1119da58368[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 20:08:06 2026 +0900

    feat: order 컬렉션 저장을 위한 규칙 추가

[33mcommit 9cd8b110100ca0c232c4fc9b88745282bf23fed5[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 20:07:48 2026 +0900

    feat: order 모델, 레포지토리 추가

[33mcommit aff85ad63140aa4c9497a0fc30e5a4cbbd1d23df[m
Merge: a2d99c6 eacd5a5
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 11:07:14 2026 +0900

    Merge pull request #26 from dmu-delibuddy/feat/chat
    
    feat: 채팅 기능 구현 (실시간 송수신, 사진 전송, 시스템 메시지, 내 채팅방 목록)

[33mcommit a2d99c6264a174951a444df37fbaef0307e30cc7[m
Merge: ac2ce4a c8d1d36
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Nov 7 11:05:50 2026 +0900

    Merge pull request #25 from dmu-delibuddy/fix/closed-room-visible
    
    fix: 모집 마감된 방이 참여자 목록에서 사라지는 문제 수정

[33mcommit eacd5a5d9451b3a738f4fe3d517cb132d84ad048[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Nov 7 03:56:42 2026 +0900

    feat: 채팅 사진 전송 및 크게 보기, 키보드 입력창 가림 수정
    
    - 사진 선택기로 고른 사진을 Storage 에 올리고 attachments 로 저장
    - storagePath 를 다운로드 URL 로 바꿔 말풍선에 표시, 누르면 전체 화면
    - 업로드 / 저장 실패 시 재전송 (업로드가 끝났으면 저장만 다시)
    - MainActivity : 키보드 높이만큼 아래 여백, 채팅방에서 하단 탭 숨김, 액션바 ← 동작
    - AndroidManifest : adjustResize

[33mcommit d5bf936798a5ddece41aa4df3c9216a87e705128[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Nov 7 03:56:35 2026 +0900

    feat: 방 생성·참여·취소·마감·완료 시 채팅 시스템 메시지 전송

[33mcommit 4d766324bf0645c0b62eb52a13e5e0c3343519aa[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Fri Nov 6 18:46:10 2026 +0000

    feat: 하단 탭 채팅을 내 채팅방 목록으로 변경
    
    - 하단 탭 navigation_chat : 내가 방장 / 참여자인 방 목록 (ChatListFragment)
    - 채팅방은 navigation_chat_room 으로 분리, roomId 필수
    - 방 상세 채팅방 버튼 목적지를 navigation_chat_room 으로 변경
    - RoomRepository.observeMyRooms 추가 (완료된 방 제외, 최근 만든 순)

[33mcommit 4d3fdeac9c9417fc0b8c5d7a757b9f6bef4dddbd[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Fri Nov 6 12:36:38 2026 +0000

    feat: 채팅방 화면 및 메시지 실시간 송수신 구현
    
    - 방 상세에서 넘어온 roomId 로 메시지 실시간 구독
    - 내 / 상대 / 시스템 말풍선 3종류 (ChatAdapter, ChatItem)
    - 보내는 중 흐리게 표시, 전송 실패 시 같은 id 로 재전송
    - 맨 아래를 보고 있을 때만 새 메시지 자동 스크롤
    - 하단 탭으로 들어오면 안내 문구, 참여자가 아니면 불러오기 실패 안내

[33mcommit ed871945d79195ee5a11150886e0588fe9cba4a0[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Fri Nov 6 11:20:22 2026 +0000

    feat: 채팅 메시지 모델, Repository 및 Firestore 규칙 추가
    
    - 보고서 DB 설계에 맞춘 ChatModel (senderId, type NORMAL/SYSTEM, content, attachments, createdAt, deletedAt)
    - ChatRepository : 방 메시지 실시간 구독, 텍스트 / 시스템 메시지 전송
    - 재전송 시 중복 방지를 위해 메시지 id 를 미리 생성
    - firestore.rules : rooms/{roomId}/messages 는 같은 방 참여자만 읽기 / 쓰기

[33mcommit c8d1d36e2af4ae223793de9d6186d8a04f203979[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Fri Nov 6 19:57:19 2026 +0900

    fix: 모집 마감된 방이 참여자 목록에서 사라지는 문제 수정
    
    - 방 목록 구독에 모집 마감(ORDER_CONFIRMED) 상태 포함
    - 모집 마감 / 마감 시간 지난 방은 방장, 참여자에게만 표시
    - 마감 시간 필터를 Repository 에서 RoomListViewModel 로 이동

[33mcommit ac2ce4a3af75b654f09d22f332bbf4fe5c8fc7de[m
Merge: 73d9ef8 75caa4a
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Fri Nov 6 19:44:18 2026 +0900

    Merge pull request #24 from dmu-delibuddy/feat/room-detail
    
    feat: 방 상세 화면 및 참여·취소·모집 마감·채팅방 이동 구현

[33mcommit 75caa4ad93a0770b70bf68f409a1b620f9caf063[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Fri Nov 6 18:50:43 2026 +0900

    feat: 방 상세에 채팅방 이동 버튼 추가
    
    - 방장 / 참여자에게만 채팅방 버튼 표시
    - roomId 를 넘기며 채팅 화면으로 이동
    - navigation_chat 에 roomId 인자 추가 (하단 탭으로 오면 null)

[33mcommit 2734c3a6b80396e54ef9ffc818fdfff36a7f6ff9[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Fri Nov 6 18:50:08 2026 +0900

    docs: 방 생성·목록 관련 함수 설명 주석 추가

[33mcommit c113bf8dfea43c3ad96aedcfc19247cfe9f1c264[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Fri Nov 6 01:41:17 2026 +0900

    feat: 방 상세 화면 및 참여·취소·모집 마감 버튼 구현
    
    - 방 카드 클릭 시 roomId 를 넘겨 방 상세로 이동
    - 방 문서 실시간 구독, 로딩 / 방 없음 / 실패 안내
    - 방 정보, 참여자 목록(방장 먼저, 나 표시) 표시
    - 내 역할 + 방 상태에 따라 하단 버튼 변경
      (참여하기 / 참여 취소 / 모집 마감 / 주문 완료)
    - 모집 마감, 주문 완료는 확인 창 후 실행
    - 실패 시 RoomException 사유 토스트

[33mcommit 73d9ef8fb10d8279984203123c699a26cf4fa8d5[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 18:38:53 2026 +0900

    init: firebase 규칙을 프로젝트 내에서 변경할 수 있도록 설정

[33mcommit 9d7fd940b01797efbab2ea0cbcf01d2fd2cf57af[m
Merge: deec045 87c4c7b
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Fri Nov 6 18:20:31 2026 +0900

    Merge pull request #23 from dmu-delibuddy/feat/image
    
    data/image 패키지 정의, 이미지 뷰 확장함수 정의

[33mcommit 87c4c7b7863479b33ee44cecbff29ea2657bc1ae[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 18:17:00 2026 +0900

    feat: ImageRepository 정의

[33mcommit c076fa9a45d868d9cf72fd2dadedc249139a94ab[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 17:48:24 2026 +0900

    feat: Glide 의존성 추가 & ImageView 확장함수 정의.

[33mcommit c1e388bd1fabf89f633f1151d32f453ae0dad9db[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 17:18:43 2026 +0900

    feat: ImageModel 정의

[33mcommit 1b9540ab15fc872c178e6ca1df3e7fce72251a91[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Nov 6 17:11:20 2026 +0900

    feat: firestore 의존성 추가

[33mcommit deec045d0b3fccb01c7434befb97bdbc1e68277c[m
Merge: 5b1917c 565681b
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Fri Nov 6 16:43:06 2026 +0900

    Merge pull request #22 from dmu-delibuddy/feat/auth-ui
    
    - 앱 인증 정보를 확인하는 동안 보여줄 엔트리 화면 생성
    - 회원가입 UI에 계좌, 생일을 받을 수 있는 뷰 추가
    - 애니메이션 수정에 용이하도록 구조 일부분 변경

[33mcommit 565681b8faefba0e10ecc32d41dca05813446bd5[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 19:10:16 2026 +0900

    feat: 애니메이션 코드 common으로 이동 & startup 화면 생성

[33mcommit bd92a1961f17618a119ba680c13ecc9484f70374[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 19:04:26 2026 +0900

    feat: 개발용 로그아웃 버튼 임시 생성

[33mcommit a6ab8c7c0b23c8a51bdb90dcd62039b3b734bc58[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 18:49:22 2026 +0900

    feat: 계좌정보 입력 및 회원가입 완료 처리

[33mcommit d31c0c808869f8e99732f234d98b4e2316cd3f7e[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 18:49:09 2026 +0900

    feat: 회원가입 생년월일 입력 단계 추가

[33mcommit a7180a8e921128b3f1437c63813f0ea5ed636596[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 18:48:51 2026 +0900

    feat: 이메일 인증 확인 중 전역 로딩 표시

[33mcommit 4a85f910aed1f84292b4ef4487ee40912b03e8b1[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 18:48:07 2026 +0900

    fix: 회원가입 정보 저장 전 인증 토큰 갱신

[33mcommit 5f83618f0d7d03ec239c5b28f37867841db60efd[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 17:54:15 2026 +0900

    feat: PasswordFragment 완성
    
    - 인증 성공 시 홈화면으로 이동되던 개발용 흐름 제거
    - 인증 메일 보내기 버튼 터치시 전역 로딩 UI

[33mcommit 567126cd12b1536cca4a91c58786536772e32d46[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 17:51:02 2026 +0900

    fix: 사용되지 않던 버튼 제거

[33mcommit 5b1917ce61d20b00e63e886a980d5d3806794b49[m
Merge: 42c6b7c bef5f86
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Thu Nov 5 19:15:52 2026 +0900

    Merge pull request #21 from dmu-delibuddy/feat/create-room-submit
    
    feat: 방 만들기 입력 검사 및 Firestore 개설 연결

[33mcommit bef5f86caea15fb023fe3c7a08c22fa56d8d85fb[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Thu Nov 5 19:06:58 2026 +0900

    fix: 방 만들기 자정 넘는 마감 시간 처리 및 개설 실패 안내 개선
    
    - 고른 마감 시각이 오늘 이미 지났으면 다음 날로 처리 (23:30 에 00:30 선택 가능)
    - '마감 시간이 이미 지났어요' 분기와 문자열 제거
    - 개설 실패 시 Log.w 로 원인 기록
    - 로그인 풀림(NOT_LOGGED_IN)은 별도 안내 문구 표시

[33mcommit d8288ba1c7aaeecacf8a508314d8e2d15c694b25[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Thu Nov 5 18:54:47 2026 +0900

    feat: 방 만들기 입력 검사 및 Firestore 개설 연결
    
    - 단계별 필수값 검사(validateStep), 다음 버튼에서 검사 후 이동
    - 개설 시 1~4단계 전체 검사, 문제 있는 단계로 이동 + 안내 토스트
    - 마감 시각이 이미 지났으면 3단계로 이동

[33mcommit 42c6b7c26af665fd59f684d05e6f071f08c122f6[m
Merge: 77ca833 e19088b
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Thu Nov 5 17:23:11 2026 +0900

    Merge pull request #20 from dmu-delibuddy/feat/global-ui
    
    feat: 전역 로딩 UI 생성

[33mcommit e19088bf9c3fbd61186a45a3413bca28596136c5[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Thu Nov 5 17:18:19 2026 +0900

    feat: 전역 로딩 UI 생성

[33mcommit 77ca833895df279235dbf965779732630d758614[m
Merge: 98ef8a8 5089a24
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Thu Nov 5 16:45:16 2026 +0900

    Merge pull request #19 from dmu-delibuddy/refactor/pixel-shadow-dimens
    
    refactor: 픽셀 그림자 값을 dimens로 통일

[33mcommit 5089a24c948dca6ad0b5cc10b4df9a9080515f3e[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Thu Nov 5 14:59:36 2026 +0900

    refactor: 픽셀 그림자 값을 dimens로 통일
    
    drawable 4개에 흩어져 있던 그림자 오프셋과 테두리 두께를 dimens.xml의 pixel_shadow/pixel_stroke로 모음

[33mcommit 98ef8a89a8000fb4050a7351fb50207d6edb79c9[m
Merge: 5934dce ead88fd
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Wed Nov 4 19:20:08 2026 +0900

    Merge pull request #18 from dmu-delibuddy/refactor/room-fragments
    
    refactor: 방 목록·방 만들기·홈 Fragment 이벤트 등록 코드를 역할별 함수로 분리

[33mcommit ead88fd973556a12be335ff66c60a21354dedea5[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Wed Nov 4 02:36:19 2026 +0900

    refactor: 방 목록·방 만들기·홈 Fragment 이벤트 등록 코드를 역할별 함수로 분리
    
    - viewModel 을 클래스 필드(by viewModels())로 변경 (SignInFragment 참고)
       - onViewCreated 에 몰려 있던 리스너·observe 코드를 setUp…/observe…/render…/show… 함수로 분리
       - 동작 변화 없음 확인

[33mcommit 5934dceea1d9cd4677cacb75ca45c98b2a73318f[m
Merge: 63a38bc 004a40e
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Wed Nov 4 01:24:24 2026 +0900

    Merge pull request #17 from dmu-delibuddy/refactor/auth-ui
    
    나는 왜
    내 아이디어도 아닌 앱을
    나 혼자서 이렇게
    열심히 만들고 있는 것일까.
    
    누군가 가볍게 꺼낸 아이디어가
    내 책상 위의 새벽이 되고
    새벽의 쉘에는
    내 얼굴만 비친다.
    
    할 수 있다는 이유로
    해야만 하는 사람이 되는 것.
    
    책임감이라 부르면
    조금은 자랑스럽고
    포기하지 못 할 일이라 부르면
    조금은 서글프다.
    
    나는 오늘도 그 두 이름 사이에서
    혼자 PR을 올리고 혼자 Merge를 한다.
    
    누군가 알아주기를 바라는지,
    아니면 지저분한 코드가
    내 눈에 거슬리는 것을
    그냥 두지 못하는지.
    
    커서는 내 질문에 대답없이
    같은 자리에서 깜빡인다.
    
    사랑했던 컴퓨터 공학마저
    미운 마음에 컴퓨터를 끄면
    검은 모니터엔 나만 남는다.
    
    내가 끄고 싶었던건
    화면이었을까,
    끝내 내 몫이 되어버리는
    아픈 마음이었을까
    
    -팀프로젝트, 채해중-

[33mcommit 004a40eb93a429b45580853dae468f73423d4d76[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Wed Nov 4 01:05:17 2026 +0900

    refactor: 라이프사이클 함수 추상화

[33mcommit 3981e2b0709a47330958cdb4e0e377765e9b96d7[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Wed Nov 4 01:04:54 2026 +0900

    refactor: viewModel 필드 변수로 관리

[33mcommit 5505e91347fe79add01d028458c35f77eb5f8c3a[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Wed Nov 4 01:03:52 2026 +0900

    refactor: 앱 로고가 LiveData로 관리되고 있던거 string resorce로 변경

[33mcommit 63a38bc3cb940b45d3a924bac38f2104a8e5b176[m
Merge: 2b04606 5a3fabc
Author: MICcall2004 <128366404+MICcall2004@users.noreply.github.com>
Date:   Wed Nov 4 01:00:09 2026 +0900

    Merge pull request #16 from dmu-delibuddy/feat/create-room
    
    feat: 방 만들기 화면

[33mcommit cc2d7c67289f74df4df0a71dd0f3fc1f3c58bde8[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Wed Nov 4 00:52:40 2026 +0900

    refactor: Auth에서 AuthEntry로 좀 더 의미에 부합한 명칭으로 변경

[33mcommit 5a3fabcf20448f24d4289f030ac086f15563f53e[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Tue Nov 3 15:49:20 2026 +0000

    refactor: 방 만들기 화면의 중복 시스템 바 여백 코드 제거
    
    main(#12)의 MainActivity.applySystemBarInsets() 가 상태바·제스처 바 여백을 앱 전체 루트에 적용하고
    insets 를 CONSUMED 로 넘기기 때문에, CreateRoomFragment 에서 하단 버튼에 따로 더하던 여백 코드는
    더 이상 값이 들어오지 않아 의미가 없어짐.
    - CreateRoomFragment: layoutBottom 의 WindowInsets 리스너와 관련 import 3개 삭제

[33mcommit 2fc1afc1f8f640d1fe354476e489e13189bbfdd6[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Wed Nov 4 00:40:04 2026 +0900

    feat(room): 방 만들기 2~4단계 입력 (음식·마감·금액·정산)
    
    - 2단계: 카테고리 드롭다운(Spinner), 음식점 이름 30자
    - 3단계: 마감 시간 선택(TimePicker, 24시간), 시간 제한 없음 체크
    - 4단계: 최소 주문 금액·예상 배달비 입력, 음식값/배달비 정산 방법 선택
      1인당 예상 배달비 안내(배달비 / 최대 참가자 수)
    - SplitChip 스타일 추가, 입력값은 모두 CreateRoomViewModel 에 저장

[33mcommit ee084a6da3569f20229d25a7d1d73461ce0d7ae4[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Tue Nov 3 01:36:33 2026 +0900

    feat(room): 방 만들기 1단계 입력 (제목·인원·모드)
    
    - 방 제목 30자 제한 + 글자 수 표시
    - 최대 참가자 수 −/+ (2~6명, 방장 포함), 범위 끝에서 버튼 비활성
    - 픽업 / 밥 친구 모드 선택
    - 입력값은 CreateRoomViewModel 에 저장 (이전으로 돌아가도 유지)

[33mcommit a95849acd387aec43566818a2a9bc6b551bf35c8[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Tue Nov 3 00:54:54 2026 +0900

    feat(room): 방 만들기 화면 틀과 단계 이동
    
    - 상단 바(뒤로가기·제목·단계), 진행 막대 4칸, 이전/다음 버튼
    - CreateRoomViewModel 로 단계 관리, 시스템 뒤로가기는 이전 단계
    - 단계별 레이아웃(layout_create_step1~4) 제목·설명만 우선 추가

[33mcommit b30b769cdabcd3f3a81978d4412ad25d3026be52[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Mon Nov 2 21:06:14 2026 +0900

    feat(room): 방 만들기 화면 이동 연결
    
    - CreateRoomFragment, fragment_create_room 추가 (빈 화면으로 놔두고 후에 작업 예정)
    - 홈 + 방 만들기 버튼 -> navigation_create_room 으로 이동
    - 방 만들기 화면에서는 하단 탭 / 액션 바 숨김 (MainActivity)

[33mcommit 2b0460613c2c925708b8eddc93053ee52e3f6fc4[m
Merge: 77383c5 0016b19
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Wed Nov 4 00:46:20 2026 +0900

    Merge pull request #15 from dmu-delibuddy/refactor/signup
    
    ui/signup 패키지 라이프 사이클 함수 추상화

[33mcommit 0016b1939e1c4c87f75508d4ef0a21ce89afcd59[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Wed Nov 4 00:45:09 2026 +0900

    refactor: savedStatedHandle 관련 코드 제거

[33mcommit 75458d5a12e1e94e1fdedd223f283a695394cfdd[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Wed Nov 4 00:44:40 2026 +0900

    refactor: Abstract PasswordFragment lifecycle function

[33mcommit ffd18a5414070bc7647e8e4ee653640e42bb887a[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Wed Nov 4 00:44:01 2026 +0900

    refactor: Abstract EmailFragment LifeCycle Function

[33mcommit 77383c57e3eea05079c151405d4456639888e9ad[m
Merge: 1f92053 064930f
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Tue Nov 3 20:57:42 2026 +0900

    Merge pull request #14 from dmu-delibuddy/refactor/signin
    
    생명주기 함수 추상화

[33mcommit 064930fbd49a0c879632b997a4e834099cc9ca08[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 20:54:51 2026 +0900

    refactor: SignInViewModel 추상화

[33mcommit cc8d32327545d09a349567e5a159193a2861a855[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 20:25:13 2026 +0900

    refactor: 이벤트 등록 코드들을 역할 기준으로 함수로 나눠 호출

[33mcommit 0a18f7f2ddebd969ebeb96361583eefc4e3d3475[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 20:18:14 2026 +0900

    refactor: viewModel 변수를 클래스 필드 변수로 변경

[33mcommit 1f920536a00b5f423bb072eeb840911f7c8ec28e[m
Merge: 920e060 85a1f50
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Tue Nov 3 20:14:23 2026 +0900

    Merge pull request #13 from dmu-delibuddy/feat/signin-ui
    
    feat: 로그인 화면 추가

[33mcommit 85a1f504e255ecf94e6e6fb9a413a98e25ef7c00[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 19:41:41 2026 +0900

    feat: 로그인 화면 추가

[33mcommit 920e060f6f2464ef2e060e7d7b89f82a02fe8750[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 19:26:59 2026 +0900

    docs: 작업 규칙 작성

[33mcommit 75613d4550e15f2ef5cdf77d7bf186625b38e75d[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 19:23:51 2026 +0900

    docs: 브랜치명 작명 규칙 추가

[33mcommit df375ee569a5117a6a3ac833a19e5a5026458a95[m
Merge: e171190 5dea871
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Tue Nov 3 19:18:24 2026 +0900

    Merge pull request #12 from dmu-delibuddy/feat/signup-ui
    
    - 회원가입 화면 일부 완성.
    - 개발 흐름 상 로그인 화면을 우선 구성하기 위해서 머지함.

[33mcommit 5dea871c5d9675e4a29d835a970bf105e331a094[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 19:10:57 2026 +0900

    feat: PasswordFragment에서 인증완료 눌렀을 때 로그인 정보가 있다면 홈 프래그먼트로 이동되도록 변경
    
    - 이메일, 비밀번호 이후 3, 4페이지에서 추가 입력 정보를 받고 넘어가도록
    추후 수정 필요

[33mcommit 40e086979e71b0f54dab1d96fc53958856493f2d[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 18:57:56 2026 +0900

    fix: SystemBar 패딩 중복 적용 코드 제거

[33mcommit 0a2f48b135a26f1faf0196e241f62d639305613a[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Tue Nov 3 15:12:54 2026 +0900

    feat: 로그인 여부 확인 로직 변경

[33mcommit 5ea3a6be077a0cb22fb12f829430e10d83459cb0[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Mon Nov 2 20:25:55 2026 +0900

    feat: PasswordFragment UI 생성

[33mcommit e116ed981f9de01e69d93500e882e32b79ae6c15[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Mon Nov 2 19:26:50 2026 +0900

    feat: EmailFragment UI 생성

[33mcommit a8fb1dc7520506db68ce2bc8aa03dd9781d4fe50[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Mon Nov 2 19:25:52 2026 +0900

    feat: system bar에 요소 가림 문제 해결을 위한 패딩 추가

[33mcommit e17119019204db29cfdcd57c7103f9fb97b5388a[m
Merge: 5db5b21 7941e8b
Author: MICcall2004 <128366404+MICcall2004@users.noreply.github.com>
Date:   Tue Nov 3 01:44:03 2026 +0900

    Merge pull request #11 from dmu-delibuddy/feat/room-list
    
    feat(room): 홈 방 목록 / 필터 / 카드 디자인 및 RoomListFragment 분리

[33mcommit 7941e8bc0fcfb80b9fd7abe49db20479e2343083[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Mon Nov 2 20:26:10 2026 +0900

    refactor(room): 방 목록·필터를 RoomListFragment 로 분리
    
    - HomeFragment 는 FragmentContainerView 로 RoomListFragment 를 담고 방 만들기 버튼만 가짐
    - RoomListFragment: 기존 HomeFragment 의 목록·필터 코드 이동, onDestroyView 에서 어댑터 해제

[33mcommit 409792d34de4bc60b91b1b4adcee690ff5d2682f[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Mon Nov 2 19:05:03 2026 +0900

    fix(main): 로고 이미지 이름 변경(delibuddy_pizza_icon) 반영

[33mcommit 83ab23426230f1410f738b02912deb5c18f79577[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Mon Nov 2 18:50:28 2026 +0900

    feat(home): 방 카드 디자인 시안 반영

[33mcommit a4cf6e912944083041db85d57547307589bc21d9[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Mon Nov 2 13:43:03 2026 +0900

    refator: RoomRepository에서 RoomException 분리 & MainActivity 코드 포맷

[33mcommit 611ba3bd25ad5d365a9d39e619bc922d45e252bd[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Mon Nov 2 01:41:25 2026 +0900

    feat(home): 모드·카테고리·참여 가능·검색 필터 추가
    
    - HomeViewModel: 필터 상태(MutableStateFlow 4개)와 Firestore 목록을 combine 해서 걸러진 rooms 제공
    - HomeFragment: 모드/카테고리 RadioGroup, 참여 가능 스위치, 검색창 리스너 연결
    - HomeFragment: onDestroyView 에서 RecyclerView 어댑터 해제

[33mcommit d205f03fe16053fd64a18ee7ae41460325d7f3f9[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sun Nov 1 14:26:43 2026 +0900

    feat(home): 방 목록 실시간 표시
    
    - RoomAdapter: ListAdapter + DiffUtil, enum 한글 표시, 카드 클릭 시 roomId 전달
    - HomeViewModel: 테스트 코드 제거, rooms를 LiveData로 제공
    - HomeFragment: 어댑터 연결, 빈 목록 안내 표시
    - RoomRepository: 잘못된 문서가 있어도 앱이 죽지 않도록 문서별 변환 후 건너뜀

[33mcommit 8c3b70b8d21c8bf17267518f3a891d23af4ea26f[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sun Nov 1 14:26:30 2026 +0900

    fix(main): 액션바에 로고 표시, 화면 위아래 잘림 수정
    
    - 액션바 제목 대신 DELI-BUDDY 로고(actionbar_logo) 표시
    - targetSdk 36 edge-to-edge 대응: 상태바 높이만큼 위쪽 여백을 코드로 지정
    - nav_host_fragment 크기를 0dp로 변경해 하단 탭에 가려지던 문제 수정

[33mcommit a0c64f4b45e25b0b7a1581630dcefe1a5e23f8ec[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sun Nov 1 14:26:10 2026 +0900

    feat(home): 홈 화면 레이아웃과 픽셀 스타일 리소스 추가
    
    - fragment_home: 모드 선택, 참여 가능 스위치, 검색창, 카테고리, 방 목록(RecyclerView), 빈 목록 안내, 방 만들기 버튼
    - item_room: 방 카드 레이아웃
    - drawable: 픽셀 테두리 배경 3종, 픽셀 아이콘 3종(검색, 방장, 시계)
    - colors/strings/themes: 색상, 문구, CategoryChip 스타일 추가
    - recyclerview 1.3.2 의존성 추가

[33mcommit 5db5b21ddc1934b2a46846128ab1360abb225815[m
Merge: 30b6eb2 8acc9bc
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Mon Nov 2 18:45:09 2026 +0900

    Merge pull request #10 from dmu-delibuddy/feat/auth-ui
    
    - activity_main에 있는 전역 액션바 패딩 삭제
    - 앱 진입점을 AuthFragment로 설정
    - AuthFragment에서 앱 내부로 분기되도록 설정

[33mcommit 8acc9bc9fefeeeb0fdce95e2cb1fd3e4af291372[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Mon Nov 2 18:39:54 2026 +0900

    feat: AuthFragment UI & AuthFragment를 앱 진입점으로 지정 및 로그인, 회원가입 화면으로 이동 가능하도록 네비게이션 추가

[33mcommit 7ce5c0454a3bd61423710e7d7eab5e73f016c5e5[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Mon Nov 2 18:37:26 2026 +0900

    ui: 전역 action-bar padding 삭제

[33mcommit 3e416ea5dbad48973c2dabb44060f306256da458[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sun Nov 1 23:45:08 2026 +0900

    wip: 앱 시작점 화면 AuthFragment 만드는 중

[33mcommit 30b6eb287128f9743f7f1f8f6e717f9aaf3268d0[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Mon Nov 2 09:49:33 2026 +0900

    chore: ./kotlin ignore에 추가

[33mcommit 3df1c5a4140e325337b6b71ab99e69341d3756fd[m
Merge: d8e8fed 28693dc
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Oct 31 20:00:30 2026 +0900

    Merge pull request #8 from dmu-delibuddy/feat/global-ui
    
    - 폰트 설정
    - 보일러플레이터 프래그먼트 코드 삭제 & 앱에서 사용할 프래그먼트 추가
    - 앱 배경, 네비게이션 배경
    - 앱 아이콘 설정

[33mcommit d8e8fedcc83adf4d608d258bd72daaede65ae68f[m
Merge: ca4047d f3f47e8
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Oct 31 19:56:54 2026 +0900

    Merge pull request #7 from dmu-delibuddy/feat/room
    
    feat/room: room data패키지 정의

[33mcommit 28693dc363980c4fec948de5b1925e1e1f3bf93a[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 19:37:12 2026 +0900

    feat: 앱 아이콘 생성 및 앱 이름 변경

[33mcommit f3f47e81c360e84079f5de532505ac60e5c4ddbb[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Oct 31 19:33:53 2026 +0900

    fix: firebase-auth 중복 의존성 제거

[33mcommit 99e9cf5e4467cee30d999170d62e5ed02bf60002[m
Author: MICcall2004 <sseony88@gmail.com>
Date:   Sat Oct 31 18:02:14 2026 +0900

    feat: Room 모델 및 RoomRepository 구현
    
    - RoomModel 데이터 클래스와 상태/모드/카테고리/정산 enum 추가
    - 방 생성, 목록·상세 실시간 구독, 참여/취소, 모집 마감/완료 함수 구현
    - 참여 시 정원 검사 순서 수정, 방장 상태 변경 시 현재 상태 검증
    - 잘못된 문서가 있어도 목록 조회가 멈추지 않도록 예외 처리
    - firebase-auth, kotlinx-coroutines-play-services 의존성 추가

[33mcommit 753abdaca69ab62f388102e023388a8cee42d602[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 18:46:42 2026 +0900

    feat: 아이콘 크기 변경

[33mcommit 0f62b8f82a616fe6c9ed0471432983957eb838dd[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 18:43:09 2026 +0900

    feat: 네비게이션 순서 변경

[33mcommit 03cfc297a4a897dbb8e502b97768cdc930aaa30d[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 18:42:58 2026 +0900

    feat: 네비게이션 선택 표시

[33mcommit d3056dc10f6e5a71693afd38eccc2a07e8e352ed[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 18:36:00 2026 +0900

    feat: 보일러플레이트 네비게이션 요소 삭제 및 앱에서 사용될 요소들 추가

[33mcommit 05d6774458b906267b9f4d7cf76ac6fa1e85fbc6[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 18:11:32 2026 +0900

    feat: 백그라운드 격자 무늬, 네비게이션바, 액션바 배경색 설정

[33mcommit ae0d6348fd60ae54dbc3bb19bba10d500cdbaa56[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 17:44:50 2026 +0900

    feat: gugi.ttf 를 전역 폰트로 설정

[33mcommit ca4047dfd8fa1a89066d0e97f2fb5548f7d52d48[m
Merge: cb46628 29084a9
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Oct 31 16:34:41 2026 +0900

    Merge pull request #5 from dmu-delibuddy/feat/auth
    
    Feat/auth: AuthRepository에 로그인, 회원가입 함수 생성

[33mcommit 29084a9f9ccdfab48977eff717b8275d20d72b9a[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 16:33:00 2026 +0900

    feat: 회원가입을 위한 repository 함수 추가

[33mcommit c08e036ba67488a200bd3a1633248c7c6cb45392[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 16:19:07 2026 +0900

    feat: AuthRepository에 signin 함수 추가

[33mcommit cb46628561855c17050fc2074e473a9b768b67e2[m
Merge: 1085c29 5cffbb1
Author: ChaeHaejoong <146797747+ChaeHaejoong@users.noreply.github.com>
Date:   Sat Oct 31 16:21:40 2026 +0900

    Merge pull request #4 from dmu-delibuddy/feat/user
    
    feat: user 데이터 클래스 정의 및 CRUD 함수 추가

[33mcommit 5cffbb16d8fc609e62e4f571de73a24b6560af8b[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 15:52:31 2026 +0900

    feat: user 데이터 클래스 정의 및 CRUD 함수 추가

[33mcommit 1085c298014aeb742e6e774e3478086ea2c12aeb[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 15:59:38 2026 +0900

    init: fireauth 의존성 추가

[33mcommit e85b8088f6da92ed0f09a674a7143392c6dde8bc[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 12:35:07 2026 +0900

    init: firestore 의존성 추가

[33mcommit 6d6afd67a8a0389bc3b43a88a484186afcf1478c[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Sat Oct 31 12:20:47 2026 +0900

    chore: 학교 안드로이드 스튜디오 환경에 맞춰 의존성 버전 조정

[33mcommit 451eeb0fe58d1c66693dd01747c87179a6fbf855[m
Author: ChaeHaejoong <svneh0329@naver.com>
Date:   Fri Oct 30 20:49:15 2026 +0900

    init: firebase firestore dev, prod 환경 분리

[33mcommit 1c49dba4efc8c00fe20f5cfb50f5c5202587a0c9[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Thu Oct 29 23:41:56 2026 +0900

    init: firebase init에 따른 의존성 변경 및 google-service.json파일 추가

[33mcommit 3724bb7db8696e9bb5df21cb9bb18f3bf6e3e61c[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Thu Oct 29 23:40:49 2026 +0900

    chore: .idea 폴더 gitignore에 추가

[33mcommit 96bf0c81a5fdc4d664c4253f9fc03200ec26e161[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Oct 26 22:19:54 2026 +0900

    docs: 규칙 작성

[33mcommit f99ae3e4e8d8ea2e00615d2ad158b5869921683e[m
Author: chaehaejoong <svneh0329@naver.com>
Date:   Mon Oct 26 22:10:18 2026 +0900

    init: 프로젝트 생성 및 초기 구조 정의
