# MLB Analysis Frontend

React + Vite + TypeScript + Tailwind CSS 기반의 한국어 MLB Analysis MVP 프론트엔드입니다.

## 실행

백엔드 서버를 먼저 실행합니다.

```bash
cd ../backend
./gradlew bootRun
```

프론트 개발 서버를 실행합니다.

```bash
cd ../frontend
npm install
npm run dev
```

Vite dev server는 `/api` 요청을 `http://localhost:8080`으로 proxy합니다.

## 검증

```bash
npm run lint
npm run build
```
