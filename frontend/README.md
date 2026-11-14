# RideHailing — Frontend

A small, deliberately-educational **React + TypeScript** web app for the
RideHailing backend. It covers three screens:

1. **Register** — create a rider or driver account
2. **Login** — sign in and receive a JWT token
3. **Dashboard** — the page after login, showing your profile

It is completely independent from the backend. You can **delete this entire
`frontend/` folder** and the backend keeps working exactly as before.

---

## Why it's built this way (read this first)

This project is organised to teach you the *shape* of a professional React app.
The guiding rule is **separation of concerns** — each folder has one job:

```
frontend/
├─ index.html                 The single HTML page React mounts into
├─ vite.config.ts             Dev server + proxy to the backend (avoids CORS)
├─ .env                       Configuration (API base URL)
└─ src/
   ├─ main.tsx                Entry point: mounts <App> into index.html
   ├─ App.tsx                 Wires Router + AuthProvider + Routes together
   │
   ├─ api/                    "How we talk to the server" (lowest layer)
   │  ├─ types.ts             TypeScript mirrors of the backend's JSON shapes
   │  └─ httpClient.ts        One axios instance + interceptors (adds JWT, errors)
   │
   ├─ services/               "What we can ask the server to do"
   │  ├─ authService.ts       login / register / logout calls
   │  └─ profileService.ts    fetch the logged-in user's profile
   │
   ├─ auth/                   "Who is logged in" (app-wide state)
   │  ├─ sessionStore.ts      reads/writes the token in localStorage
   │  ├─ AuthContext.ts       the shared state's shape (React Context)
   │  ├─ AuthProvider.tsx     the logic: login/logout, session restore
   │  └─ useAuth.ts           hook so components read auth with one line
   │
   ├─ components/             Reusable, "dumb" UI pieces
   │  ├─ TextField.tsx        labelled input
   │  ├─ Button.tsx           button with a loading state
   │  ├─ Alert.tsx            error/success banner
   │  ├─ Spinner.tsx          loading indicator
   │  ├─ AppLayout.tsx        header + logout, wraps logged-in pages
   │  └─ ProtectedRoute.tsx   redirects to /login if not authenticated
   │
   ├─ pages/                  One component per screen (the "smart" parts)
   │  ├─ LoginPage.tsx
   │  ├─ RegisterPage.tsx
   │  └─ DashboardPage.tsx
   │
   ├─ routes/
   │  └─ AppRoutes.tsx        The URL → page map, with guarded routes
   │
   └─ styles/
      └─ global.css           All styling (kept simple on purpose)
```

### The layers, top to bottom

- A **page** (e.g. `LoginPage`) handles user interaction and local form state.
- It calls a **service** (`authService.login`) instead of talking to the network itself.
- The service uses the shared **httpClient**, which automatically attaches the
  JWT token and cleans up errors.
- **Auth state** (`AuthProvider` + `useAuth`) remembers who is logged in and is
  readable from anywhere via the `useAuth()` hook.

This top-to-bottom flow is the single most important idea to take away: UI never
touches the network directly; it goes through clear, testable layers.

---

## Running it

> Requires [Node.js](https://nodejs.org/) 18+ (includes `npm`).

From inside the `frontend/` folder:

```bash
npm install     # install dependencies (first time only)
npm run dev     # start the dev server at http://localhost:5173
```

Then make sure the **backend is running on port 8080** (e.g. `docker compose up`
from the project root), open <http://localhost:5173>, and register an account.

Other scripts:

```bash
npm run build       # type-check + production build into dist/
npm run preview     # preview the production build locally
npm run typecheck   # type-check only, no output
```

---

## API routing during development

The shared Axios client uses /api as its base URL. Services use paths such as
/api/auth/login, /riders/me, and /drivers/me. The browser sends all these requests
to the frontend origin, http://localhost:5173.

Vite has one proxy rule for /api, targeting http://localhost:8080:

| Browser path | Backend path |
| --- | --- |
| /api/api/auth/login | /api/auth/login |
| /api/api/auth/register/rider | /api/auth/register/rider |
| /api/api/auth/register/driver | /api/auth/register/driver |
| /api/api/auth/logout | /api/auth/logout |
| /api/riders/me | /riders/me |
| /api/drivers/me | /drivers/me |
| /api/rides/... | /rides/... |
| /api/prices | /prices |
| /api/locations/... | /locations/... |

The proxy strips exactly one leading /api from every request. Service paths
match the backend mappings, including /api/auth for authentication. The doubled
/api/api/auth browser prefix is intentional: the first /api selects the proxy,
and the second belongs to the backend endpoint.

Leave VITE_API_BASE_URL empty to use the default /api, or set it explicitly
to /api as shown in .env.example. Restart Vite after changing its environment.
The current backend has mixed path prefixes, so pointing this variable directly
at port 8080 would bypass the required rewrite.

These browser requests are same-origin and need no backend CORS configuration.
The proxy runs only in development. A production reverse proxy such as Nginx
will need equivalent routing; it has not been configured yet.

---

## How authentication works here

1. You log in → backend returns a **JWT token** + your role (`RIDER`/`DRIVER`).
2. The token is saved in `localStorage` (`sessionStore.ts`).
3. On every request, `httpClient` adds the header `Authorization: Bearer <token>`.
4. `ProtectedRoute` keeps you out of `/dashboard` unless you're logged in.
5. If the backend ever rejects the token (401), the app logs you out automatically.

> Note: storing a JWT in `localStorage` is simple and fine for learning, but is
> vulnerable to XSS. Production apps often use httpOnly cookies instead. This is
> called out in `sessionStore.ts` too.

---

## Scope & next steps

This frontend intentionally stays small. Natural things to add as you learn:

- An **edit profile** form (the backend already supports `PUT /riders/me` and
  `PUT /drivers/me`).
- A **rides** page listing your rides (`GET /rides/rider/{id}` or `/driver/{id}`).
- A **price estimate** form (`POST /prices`).
- Live driver location via the backend's WebSocket (`/ws` + STOMP).
