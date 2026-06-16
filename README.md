# 루틴몬(Routinemon) AI 사물인식 기반 루틴 형성 및 펫 육성 앱

> **"지루한 일상 루틴을 게임처럼 즐겁게!"**  
> Google ML Kit AI 사물인식 기능을 활용하여 일일 미션을 실제로 인증하고, 나만의 펫 '뽀뽀'를 함께 키워나가는 안드로이드 루틴 관리 애플리케이션입니다.

---

## 프로젝트 소개
* **팀원 소개**: 2022145042 설영빈, 2024145060 신재은
* **개발 기간**: 2026.05 ~ 2026.06 (약 1개월)
* **개발 환경**: Android Studio (Java)
* **주요 기술**: Android CameraX, Google ML Kit Object Labeling, 아키텍처 패턴 (UI/데이터 분리)

---
## 실행영상 
https://youtu.be/aK3XMZjJ8gk




## 핵심 기능

### 1. 사용자 맞춤형 루틴 및 투두리스트 관리
* 메인 화면에서 오늘 수행할 나만의 루틴(물 한 잔 마시기, 책 읽기, 스트레칭 등)을 자유롭게 추가하고 관리합니다.

### 2. Google ML Kit 기반 AI 실시간 사물인식 인증
* 단순히 체크박스를 누르는 방식에서 벗어나, **카메라로 실제 미션과 관련된 사물을 촬영하여 인증**해야 루틴이 완료됩니다.
* **실시간 디버깅 필터**: AI가 카메라 화면에서 인식 중인 상위 3개 단어와 확신도(Confidence)를 화면에 실시간으로 중계하여 정밀한 조준과 피드백이 가능합니다.

### 3. 쫀쫀한 미션별 연관어 채점 콘솔 (Custom Matching System)
* AI가 수천 개의 사물 단어를 분석하면, 앱 내부 채점관 시스템이 현재 미션과 연관성이 높은 핵심 키워드를 필터링하여 정확하게 통과 여부를 판단합니다.
  * **물 마시기**: `cup`, `glass`, `mug`, `bottle`, `tableware` 등
  * **책 읽기**: `book`, `paper`, `text`, `publication` 등
  * **스트레칭**: `mat`, `physical`, `joint`, `leg`, `shoe` 등
  * **이불 개기**: `bedroom`, `bed`, `pillow`, `linens`, `quilt`, `sheet` 등
  * **산책/걷기**: `branch`, `forest`, `soil`, `rock` 등
  * **영양제 먹기**: `pill`, `vitamin` 등

### 4. 펫 '뽀뽀' 성장 시스템
* 루틴 미션을 성공적으로 인증할 때마다 미션 완료 신호를 획득하여 캐릭터가 경험치를 얻고 성장합니다.

---

## 서비스 화면 스크린샷

| 메인 화면 & 투두리스트 | AI 카메라 실시간 인증 |
| :---: | :---: |
| <img width="360" height="743" alt="image" src="https://github.com/user-attachments/assets/95609cf3-7ae9-4232-943f-f5712d84b3bd" /> | <img width="363" height="745" alt="image" src="https://github.com/user-attachments/assets/9d5bef68-443e-4174-89d4-4226271769e2" />
| 사용자의 당일 루틴 리스트 확인 | AI 단어 스캔 및 실시간 통과 판독 |

---

## 기술 스택 및 라이브러리

### Environment
* **IDE**: Android Studio Jellyfish / Koala (최신 안드로이드 스튜디오 환경)
* **Language**: Java
* **SDK**: Min SDK 24 / Target SDK 34

### Libraries
* **CameraX**: `androidx.camera:camera-camera2`, `camera-lifecycle`, `camera-view`
* **Google ML Kit**: `com.google.mlkit:image-labeling` (사물 인식 엔진)
* **Guava**: `com.google.common.util.concurrent.ListenableFuture` (비동기 카메라 프로바이더 관리)
