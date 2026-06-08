# MLB Analysis

MLB Stats API 기반의 백엔드와 React 프론트엔드를 분리한 모노레포입니다.

## 폴더 구조

```text
.
├── backend/   # Spring Boot API 서버
├── frontend/  # React + Vite + TypeScript + Tailwind UI
├── DESIGN.md  # 프론트/제품 디자인 기준
└── README.md
```

## 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

로그인 기능을 MySQL과 함께 사용할 때는 Docker MySQL을 먼저 띄운 뒤 `login` 프로파일로 실행합니다.

```bash
docker compose -f docker-compose.login.yml up -d
cd backend
SPRING_PROFILES_ACTIVE=login ./gradlew bootRun
```

주요 API:

```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"fan@example.com","displayName":"Fan","password":"password123"}'
curl "http://localhost:8080/api/v1/games?date=2026-06-01"
curl "http://localhost:8080/api/v1/teams"
curl "http://localhost:8080/api/v1/teams/133/players"
curl "http://localhost:8080/api/v1/players/search?name=Mike%20Trout"
curl "http://localhost:8080/api/v1/players/545361"
curl "http://localhost:8080/api/v1/players/545361/stats?season=2025&group=hitting"
```

## 프론트 실행

백엔드를 먼저 실행한 뒤 프론트를 실행합니다.

```bash
cd frontend
npm install
npm run dev
```

프론트 개발 서버는 `/api` 요청을 `http://localhost:8080`으로 proxy합니다.

## 검증

```bash
cd backend
./gradlew test
./gradlew build

cd ../frontend
npm run lint
npm run build
```
