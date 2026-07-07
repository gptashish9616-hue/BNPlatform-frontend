# BNPlatform — Backend (Spring Boot)

REST API for the BNPlatform professional networking platform.

## Stack
- Java 17, Spring Boot 3.3.5 (Web, Data JPA, Security, Validation)
- MySQL 8 (Laragon) — database `hibernate` (auto-created on first run)
- JWT auth (jjwt), BCrypt passwords
- Swagger UI (springdoc)

## Run
```bash
cd backend
mvn spring-boot:run
```
App starts on **http://localhost:8080**. Swagger UI: **http://localhost:8080/swagger-ui.html**

DB credentials live in `src/main/resources/application.properties` (`root` / `boot`).
On first start it seeds a **super admin** and **3 plans**.

### Seeded super admin
- Email: `admin@bnplatform.com`
- Password: `Admin@123`

## Auth
1. `POST /api/auth/register` or `/api/auth/login` → returns a JWT.
2. Send it on protected calls: `Authorization: Bearer <token>`.

## Roles
`SUPER_ADMIN`, `SUB_ADMIN`, `PREMIUM_USER`, `FREE_USER`.
`/api/admin/**` requires an admin role. Subscribing upgrades a `FREE_USER` to `PREMIUM_USER`.

## Modules & key endpoints
| Module | Base path | Notes |
|--------|-----------|-------|
| Auth | `/api/auth` | register, login, forgot-password, reset-password |
| Users | `/api/users` | `me`, update profile, search, `nearby` (premium) |
| Profile | `/api/profile` | addresses, company profile, authenticity documents |
| Invites (App Referral) | `/api/invites` | send invite, history — invitee registers ⇒ inviter gets 1 free month + points |
| Referrals (Deal Referral) | `/api/referrals` | given / received / status; completion awards points |
| Requirements | `/api/requirements` | post, respond, accept response, fulfill (awards points) |
| Subscription | `/api/plans`, `/api/subscriptions`, `/api/payments` | plans are public; subscribe = mock payment |
| Chat | `/api/chat` | premium-only conversations + messages |
| Notifications | `/api/notifications` | in-app list, unread count, mark read |
| Points & Leaderboard | `/api/points/history`, `/api/leaderboard?city=&state=` | credibility only (no redemption) |
| Reviews | `/api/reviews` | rate a professional, recomputes avg rating |
| Post Feed | `/api/feed` | posts, like, comment (modular/removable) |
| Analytics | `/api/dashboard/user`, `/api/admin/dashboard` | per-role summaries |
| Admin | `/api/admin/users` | list users, change status / role |

## Notes
- Email / SMS / WhatsApp dispatch and a real payment gateway are **Phase 4** (currently in-app/mock).
- Points are reputation only and are never redeemable.
