# Design

## Source of truth
- Status: Active
- Last refreshed: 2026-06-02
- Primary product surfaces: MLB Analysis 웹 MVP, 오늘의 경기 대시보드, 팀 목록, 팀별 선수 목록, 선수 이름 검색, 선수 상세
- Evidence reviewed:
  - 저장소에는 기존 프론트 소스 없음 (`package.json`, Vite/Next 설정, TSX/JSX 파일 없음)
  - 백엔드 API: `GET /api/v1/teams`, `GET /api/v1/games?date=YYYY-MM-DD`, `GET /api/v1/players/{playerId}`, `GET /api/v1/teams/{teamId}/players`
  - 사용자 결정: 실제 서비스 MVP, 한국어 UI, ESPN 스타일, React + Vite + TypeScript, Tailwind CSS, 백엔드 API만 사용, 데스크톱 우선, 시각화 필요, 선수 이름 검색 필요

## Brand
- Personality: 스포츠 미디어처럼 빠르고 생동감 있으면서, 데이터 대시보드처럼 신뢰감 있는 톤
- Trust signals: 명확한 경기 상태, 점수, 팀/선수 정보, API 오류 상태의 투명한 표시
- Avoid: 과도한 애니메이션, 베팅/도박 느낌, 복잡한 엔터프라이즈 관리자 UI, 실제 데이터 없이 꾸민 mock 화면

## Product goals
- Goals:
  - 오늘의 MLB 경기와 핵심 정보를 한 화면에서 빠르게 확인한다.
  - 팀 목록에서 팀별 선수 명단을 탐색한다.
  - 선수 이름 검색으로 선수 상세 정보를 찾는다.
  - 간단한 시각화로 경기/팀/선수 데이터를 더 쉽게 이해한다.
- Non-goals:
  - 로그인/회원/관리자 기능
  - DB 저장 기반 히스토리/개인화
  - 베팅, 예측, 결제 기능
- Success signals:
  - 백엔드 API만으로 주요 화면이 동작한다.
  - 데스크톱에서 ESPN 스타일의 스포츠 대시보드 인상을 준다.
  - 로딩/빈값/오류 상태가 명확하다.

## Personas and jobs
- Primary personas:
  - MLB 경기와 선수 정보를 빠르게 확인하려는 야구 팬
  - 포트폴리오/서비스 MVP를 평가하는 개발자 또는 리뷰어
- User jobs:
  - 오늘 경기 일정과 결과를 본다.
  - 팀 목록을 보고 특정 팀 선수들을 확인한다.
  - 선수 이름으로 검색해 상세 정보를 본다.
- Key contexts of use: 데스크톱 브라우저, 로컬 백엔드 서버와 함께 실행되는 MVP 데모

## Information architecture
- Primary navigation: 상단 네비게이션 `오늘의 경기`, `팀`, `선수 검색`
- Core routes/screens:
  - 단일 페이지 대시보드 MVP: 섹션 앵커 기반 이동
  - 오늘의 경기: 날짜 선택, 경기 카드/스코어보드, 경기 상태 분포 시각화
  - 팀: 팀 목록, 선택 팀의 선수 목록
  - 선수 검색: 이름 검색 결과, 선택 선수 상세
- Content hierarchy:
  1. 히어로/오늘의 경기 요약
  2. 경기 스코어보드와 시각화
  3. 팀 탐색 및 로스터
  4. 선수 이름 검색과 상세

## Design principles
- Principle 1: 스포츠 미디어처럼 핵심 수치와 상태를 먼저 보여준다.
- Principle 2: MVP답게 화면 수를 늘리기보다 한 페이지에서 탐색 흐름을 완성한다.
- Tradeoffs: 정교한 라우팅/디자인 시스템보다 API 연동, 명확한 상태 처리, 데이터 가독성을 우선한다.

## Visual language
- Color: ESPN에서 연상되는 딥 네이비/차콜 배경, 레드 액센트, 화이트 카드, 그린/옐로우 상태 보조색
- Typography: 시스템 산세리프, 한국어 본문 14px 이상, 제목은 굵고 짧게
- Spacing/layout rhythm: 데스크톱 12컬럼 느낌의 넓은 컨테이너, 카드 간 16~24px 간격
- Shape/radius/elevation: 카드 radius 12~16px, 절제된 shadow, 스코어보드에는 명확한 구분선
- Motion: 로딩 스피너/hover 정도만 사용, reduced-motion 고려
- Imagery/iconography: 외부 이미지 의존 없이 텍스트/배지/간단 차트 중심

## Components
- Existing components to reuse: 없음
- New/changed components:
  - Header/Nav
  - SectionCard
  - GameScoreCard
  - StatusDistributionChart
  - TeamList
  - RosterTable
  - PlayerSearch
  - PlayerDetailCard
  - Loading/Error/Empty 상태 컴포넌트
- Variants and states: loading, empty, error, selected, hover, disabled
- Token/component ownership: Tailwind 유틸리티 기반, 과한 디자인 시스템 추상화 금지

## Accessibility
- Target standard: MVP 수준 WCAG 2.1 AA 지향
- Keyboard/focus behavior: 버튼/입력/팀 선택 요소 focus-visible 제공
- Contrast/readability: 어두운 배경 위 텍스트 대비 확보, 한국어 본문 14px 이상
- Screen-reader semantics: 섹션 heading, table header, form label 사용
- Reduced motion and sensory considerations: 필수 정보는 색만으로 전달하지 않고 텍스트 배지 병행

## Responsive behavior
- Supported breakpoints/devices: 데스크톱 우선, 1024px 이상 최적화; 모바일은 깨지지 않는 수준의 단일 컬럼 대응
- Layout adaptations: 데스크톱 2~3컬럼, 좁은 화면 단일 컬럼
- Touch/hover differences: hover 정보는 보조만, 클릭/탭으로 주요 동작 가능

## Interaction states
- Loading: 섹션별 로딩 메시지/스켈레톤 최소 적용
- Empty: 데이터 없음 메시지와 다음 행동 안내
- Error: 백엔드/API 오류 메시지와 재시도 버튼
- Success: 검색/선택 결과 즉시 표시
- Disabled: 검색어 미입력 시 버튼 비활성화
- Offline/slow network: 섹션별 오류 처리, 전체 앱 중단 금지

## Content voice
- Tone: 짧고 명확한 한국어, 스포츠 중계 느낌의 활기 있는 제목
- Terminology: `오늘의 경기`, `경기 상태`, `팀`, `선수 명단`, `선수 검색`, `타격`, `투구`
- Microcopy rules: 오류는 원인보다 사용자가 할 다음 행동을 먼저 안내

## Implementation constraints
- Framework/styling system: React + Vite + TypeScript + Tailwind CSS
- Design-token constraints: Tailwind 설정/유틸리티 중심, 별도 디자인 시스템 패키지 금지
- Performance constraints: 클라이언트 MVP, 필요한 API만 호출, 과도한 라이브러리 추가 금지
- Compatibility constraints: 백엔드 API는 Vite dev proxy를 통해 상대 경로 `/api/v1/...` 호출
- Test/screenshot expectations: frontend build 성공, 핵심 유틸/컴포넌트 테스트 가능하면 Vitest 사용

## Open questions
- [x] 선수 이름 검색은 백엔드 API 추가로 처리 / owner: user confirmed / impact: frontend real search 가능
- [ ] 실제 배포 환경의 API base URL / owner: future / impact: 현재는 Vite proxy와 상대 URL 사용
