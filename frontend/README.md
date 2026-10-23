# quotelab — frontend

Angular 22 (standalone, signals, `httpResource`, новый control flow), графики — Lightweight Charts и Chart.js.

```bash
npm install
npm start          # http://localhost:4200, /api проксируется на localhost:8080
npm run build      # статика в dist/frontend
```

Angular 22 требует Node 22.22+ или 24.15+. Нужный Node 24 стоит в devDependencies (пакет `node`),
поэтому `npm start` / `npm run build` работают с ним при любом системном Node.

Структура `src/app`: `core/api` — типизированные клиенты и DTO, `core/sse` — поток событий,
`features/*` — страницы, `shared/ui` — бейджи, состояния, обёртки графиков.
