# Ganpati Vargani Management & WhatsApp Communication System

An internal admin application for a Ganpati mandal to record donations (vargani), analyze
collection across buildings/floors/flats, and communicate with contributors over WhatsApp.

## 1. Project overview

Two connected areas:

1. **Vargani / Donation Management & Analytics** — record donations, dynamic building-specific
   forms (floor/flat only where applicable), server-side filtering/sorting/pagination, a
   configurable dashboard.
2. **WhatsApp Communication** — a reusable messaging module (not hardcoded to donation
   receipts): message templates, donation thank-yous, ad-hoc messages, and filterable
   broadcasts, all sent through the official Meta WhatsApp Cloud API.

## 2. Features

- Buildings/categories are ordinary database rows, not hardcoded strings. Each one carries its
  own `requiresFloor` / `requiresFlat` flags, so the donation form, validation, and analytics
  all adapt automatically — adding a 10th category later is a form submission, not a code change.
- Donations: CRUD, combinable filters (building, amount, floor, flat, date range, search,
  WhatsApp status), server-side pagination and sorting.
- Analytics: overall summary, building-wise, floor-wise, flat-wise, top donors, recent
  donations, donation-amount thresholds, WhatsApp send statistics.
- Dashboard: a building selector drives every widget; a settings panel lets the admin
  show/hide individual widgets (persisted per-browser).
- WhatsApp: reusable, admin-editable message templates with `{{variable}}` substitution;
  donation thank-you sends (optional, per-donation); ad-hoc messages to hand-picked
  contributors; filterable broadcasts (by building / donation amount / date, or all
  opted-in contributors); broadcast history with per-recipient delivery status and
  retry-failed.
- Every contributor has a `whatsappOptIn` flag; broadcasts only ever reach opted-in
  contributors, regardless of filter.
- JWT-based admin authentication; every API endpoint except `/api/auth/login` requires it.
- Soft deletes and an audit log on every meaningful write.

## 3. Architecture

```
React (Vite/TS/Tailwind) ──HTTP/JSON──▶ Spring Boot ──JDBC──▶ MySQL
                                             │
                                             ▼
                                    WhatsAppProvider (interface)
                                     ├─ MockWhatsAppProvider (default)
                                     └─ MetaCloudApiWhatsAppProvider
                                             │
                                             ▼
                                  Meta WhatsApp Cloud API (Graph API)
```

The WhatsApp module (`whatsapp/` package) is provider-agnostic: `WhatsAppService` (donation
thank-yous) and `BroadcastService` (announcements) both depend only on the `WhatsAppProvider`
interface, never on Meta's API shape directly. Swapping providers is a one-line config change
(`WHATSAPP_PROVIDER`), not a code change.

## 4. Technology stack

**Frontend:** React, TypeScript, Vite, Tailwind CSS, React Router, Axios, TanStack Table,
Recharts.

**Backend:** Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Validation, Spring
Security (JWT), Flyway, Lombok.

**Database:** MySQL 8.

## 5. Database schema

```
buildings ──< contributors ──< donations
                   │
                   └──< broadcast_recipients >── broadcasts ──> message_templates
admins                                                          (independent)
app_settings                                                    (independent)
audit_logs                                                      (independent)
```

Money is always `BigDecimal`/`DECIMAL(12,2)` — never `float`/`double`. Every table has
`created_at`/`updated_at` and a soft-delete pair (`deleted`, `deleted_at`); nothing with
history (donations, contributors, broadcasts) is ever hard-deleted.

Schema is entirely Flyway-managed (`backend/src/main/resources/db/migration`) — Hibernate's
`ddl-auto` is `validate` only, so the migrations are the single source of truth. Run order:

| Migration | Contents |
|---|---|
| V1 | buildings, contributors, donations, audit_logs |
| V2 | seed the 9 initial building categories |
| V3 | message_templates, broadcasts, broadcast_recipients, app_settings + starter templates |
| V4 | admins table |

## 6. MySQL setup

Local (no Docker):

```bash
mysql -u root -p -e "CREATE DATABASE ganpati_vargani CHARACTER SET utf8mb4;"
```

Flyway creates all tables and seed data automatically on first backend startup — no manual
schema work needed beyond creating the empty database.

## 7. Environment variables

Copy `backend/.env.example` to `backend/.env` (and `frontend/.env.example` to
`frontend/.env`) and fill in:

| Variable | Where | Notes |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | backend | MySQL connection |
| `JWT_SECRET` | backend | **Required.** Random 32+ char string: `openssl rand -base64 48` |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | backend | Creates the first admin account on first startup only |
| `WHATSAPP_PROVIDER` | backend | `mock` (default, safe, no credentials needed) or `meta-cloud-api` |
| `WHATSAPP_ACCESS_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_BUSINESS_ACCOUNT_ID`, `WHATSAPP_API_VERSION` | backend | Only read when `WHATSAPP_PROVIDER=meta-cloud-api` |
| `CORS_ALLOWED_ORIGINS` | backend | Frontend origin, e.g. `http://localhost:5173` |
| `VITE_API_BASE_URL` | frontend | `/api` (default; works with the Vite dev proxy and the Docker nginx proxy) |

`.env` is gitignored everywhere — never commit real credentials.

## 8. WhatsApp Cloud API setup

1. Create a Meta developer app and a WhatsApp Business Account in
   [Meta Business Manager](https://business.facebook.com).
2. From the app's **WhatsApp → API Setup** page, note:
   - **Phone Number ID** → `WHATSAPP_PHONE_NUMBER_ID`
   - **WhatsApp Business Account ID** → `WHATSAPP_BUSINESS_ACCOUNT_ID`
   - Current **Graph API version** (e.g. `v21.0`) → `WHATSAPP_API_VERSION`
3. Generate a **permanent access token** via Business Settings → System Users (the temporary
   token shown on the API Setup page expires in ~24 hours and is only for quick testing).
   Grant it the `whatsapp_business_messaging` permission.
4. Set `WHATSAPP_PROVIDER=meta-cloud-api` and restart the backend.

### Webhooks

This app exposes `GET/POST /api/whatsapp/webhook` for Meta's webhook configuration
(WhatsApp → Configuration → Webhooks in the app dashboard):

- **Callback URL**: `https://<your-public-domain>/api/whatsapp/webhook`
- **Verify token**: any string you choose, set as `WHATSAPP_WEBHOOK_VERIFY_TOKEN` in `.env`
  (must match exactly what you type into the Meta dashboard)

This endpoint is deliberately excluded from JWT auth (Meta can't send your admin token) and
must be reachable from the public internet — see Section 13a for exposing it during local
development.

### ⚠️ The 24-hour messaging window (read this before going live)

Meta only allows **free-form** text/image messages (what this app sends by default) to a
recipient who has messaged your business number within the last 24 hours. Outside that
window — which is most donation thank-yous and festival announcements, since contributors
haven't necessarily messaged you first — Meta requires a **pre-approved Message Template**
(a Meta concept, created and approved in Business Manager, distinct from this app's own
"Message Template" feature).

Before relying on real WhatsApp sends for broadcasts, either:
- get your mandal's message templates approved in Meta Business Manager and extend
  `MetaCloudApiWhatsAppProvider` to send `type: "template"` requests, or
- accept that sends will only succeed for contributors who recently messaged your number.

This is flagged in code at `backend/.../whatsapp/MetaCloudApiWhatsAppProvider.java`.

### Message templates in this app vs. Meta's templates

This app's **Templates** page (WhatsApp → Templates) is an *application-level* concept: a
reusable message + image, with `{{variable}}` substitution, stored in your own database. It is
**not** the same as a Meta-approved template. Using the mock/default provider, or sending to
someone within the 24-hour window, this distinction doesn't matter. For broadcast campaigns in
production, it does — see above.

### Available message variables

`{{name}}` `{{mobile}}` `{{building}}` `{{floor}}` `{{flat}}` `{{amount}}` `{{date}}`
`{{organizationName}}`. Missing values (e.g. a contributor with no flat) render as `—`, never
the literal text "null".

### Opt-in

Every contributor has a `whatsappOptIn` flag (default: true). Broadcasts and the recipient
preview only ever include opted-in contributors, regardless of what other filters are applied.

## 9. Backend setup

```bash
cd backend
cp .env.example .env   # fill in DB + JWT_SECRET + ADMIN_USERNAME/PASSWORD at minimum
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

## 10. Frontend setup

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Opens on `http://localhost:5173`, proxying `/api` to the backend dev server.

## 11. Running the application

1. Start MySQL, then the backend (`mvn spring-boot:run`) — Flyway migrates the schema and an
   admin account is created from `ADMIN_USERNAME`/`ADMIN_PASSWORD` on first boot.
2. Start the frontend (`npm run dev`).
3. Log in with the admin credentials from step 1, then **change the password immediately**
   from Settings.

## 12. API documentation

Full interactive docs at `/swagger-ui.html` once the backend is running. Key endpoint groups:

- `GET/POST/PUT /api/buildings`, `PATCH /api/buildings/{id}/status`
- `POST/GET/PUT/DELETE /api/donations`, `GET /api/donations` (combinable filters + pagination)
- `GET /api/analytics/summary|buildings|buildings/{id}|buildings/{id}/floors|buildings/{id}/flats|top-donors|recent-donations|thresholds|whatsapp-stats`
- `GET/POST/PUT /api/whatsapp/templates`, `PATCH /api/whatsapp/templates/{id}/status`
- `POST /api/whatsapp/broadcasts/preview`, `POST/GET /api/whatsapp/broadcasts`,
  `GET /api/whatsapp/broadcasts/{id}`, `POST /api/whatsapp/broadcasts/{id}/retry-failed`
- `POST /api/whatsapp/donations/{id}/send`, `POST /api/whatsapp/donations/{id}/retry`
- `GET /api/contributors` (search, used by the recipient picker)
- `GET/PUT /api/settings`
- `POST /api/auth/login`, `POST /api/auth/change-password`

## 13. Docker setup

```bash
cp .env.example .env   # project root - fill in DB_PASSWORD, JWT_SECRET, ADMIN_PASSWORD
docker compose up --build
```

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- MySQL: `localhost:3306`

The frontend container's nginx proxies `/api/*` to the backend container, so no CORS
configuration is needed in this setup.

### 13a. Exposing the webhook publicly (local development)

Meta needs to reach `/api/whatsapp/webhook` from the public internet, which `localhost`
never satisfies. For local development, use a tunnel:

```bash
ngrok http 8080
```

Take the `https://<random>.ngrok-free.app` URL ngrok prints, and set the Meta dashboard's
Callback URL to `https://<random>.ngrok-free.app/api/whatsapp/webhook`. The verify token must
match `WHATSAPP_WEBHOOK_VERIFY_TOKEN` exactly. Note the ngrok URL changes every restart on
the free tier, so you'll re-verify each session — a paid ngrok plan or a real domain avoids
that.

For a persistent/production setup, put the backend behind your own domain with a real nginx
reverse proxy and TLS (e.g. via certbot/Let's Encrypt), then use
`https://your-domain.com/api/whatsapp/webhook` as the permanent Callback URL.

## 14. Troubleshooting

- **Backend fails to start with a JWT_SECRET error** — set `JWT_SECRET` in `.env` to a random
  32+ character string.
- **"No admin account exists" on first login** — set `ADMIN_USERNAME`/`ADMIN_PASSWORD` in
  `.env` before first startup; the account is only created once, when the `admins` table is
  empty.
- **Flyway checksum mismatch after pulling changes** — never edit an already-applied migration
  file; add a new `V{n}__description.sql` instead.
- **WhatsApp sends fail with `meta-cloud-api`** — check you're within the 24-hour messaging
  window, or that a Meta-approved template is in use (see Section 8).
- **CORS errors in local dev** — confirm `CORS_ALLOWED_ORIGINS` in the backend `.env` matches
  the frontend's actual origin (`http://localhost:5173` by default).

## 15. Deployment

- Build both images with the provided Dockerfiles, or `docker compose up --build` for a
  single-host deployment.
- Point `DB_HOST`/etc. at a managed MySQL instance for production; the app itself is stateless
  (JWT auth, no server-side sessions), so it scales horizontally behind a load balancer if
  needed.
- Rotate `JWT_SECRET` and the WhatsApp access token periodically; both are environment
  variables only, never stored in the database.

## 16. Extensibility (deliberately not built yet)

The architecture leaves room for, without requiring rework: UPI/QR payments, PDF/digital
receipts, CSV/Excel exports, monthly/yearly reports, donation targets, multiple
administrators/roles, multiple mandals, email/SMS alongside WhatsApp, an async job queue for
broadcasts at larger scale (the `dispatch()` method in `BroadcastService` is isolated
specifically so it can be swapped for a queue-backed worker later), and receipt numbering.
