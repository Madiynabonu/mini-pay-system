# MiniPay — To'lovlarni Boshqarish Platformasi

MiniPay — foydalanuvchi balansidan tashqi to'lov-provayder orqali xizmat haqini to'lash tizimi (gateway → orchestrator → provider-plugin → reconciliation arxitekturasiga asoslangan mikroservis).

## Asosiy imkoniyatlar

- **Hold → Capture/Release** pul harakati modeli — bank/karta tizimlaridagi authorization-capture patterniga mos: balans darhol yechiladi, provider javobiga qarab yakunlanadi yoki qaytariladi
- **Idempotency-Key** orqali xavfsiz qayta urinish — bir xil so'rov qayta yuborilsa ham, pul ikki marta yechilmaydi
- **Pessimistic locking** — parallel so'rovlarda bitta foydalanuvchi balansining yaxlitligini ta'minlaydi
- **Strategy pattern** (`provider-plugin`) — bir nechta to'lov-provayderni bir xil kontrakt orqali ulash imkonini beradi
- To'liq `ProblemDetail`/RFC 7807 formatidagi xatolik javoblari, sahifalangan tranzaksiya ro'yxati

## Hujjatlar

Barcha talablar, domain model, API kontraktlari, DB sxemasi va yo'l xaritasi — [SPEC.md](2.SPEC.md) faylida.

MiniPay tugagandan keyin alohida mikroservis sifatida yoziladigan avtospisaniya (auto-debit) xizmati — [AUTODEBIT_SPEC.md](3.AUTODEBIT_SPEC.md) faylida.

## Texnologik stack

Java 17, Spring Boot 3.x, PostgreSQL 16, Liquibase, Maven. Kelajakda rejalashtirilgan: Kafka, Docker Compose, Spring Cloud Gateway, Keycloak, Resilience4j.

## Quickstart

### Talablar

- Java 17, Maven (yoki `./mvnw`)
- PostgreSQL ishga tushirilgan, `mini-pay` nomli baza yaratilgan
- Loyiha ildizida `.env` fayl:
  ```
  DB_URL=jdbc:postgresql://localhost:5432/mini-pay
  DB_USERNAME=postgres
  DB_PASSWORD=<parol>
  ```

### Ishga tushirish

```bash
./mvnw spring-boot:run
```

Ilova `http://localhost:8080` portida ko'tariladi. Liquibase avtomatik schema yaratadi va demo `users`/`providers` seed qiladi (2 ta demo user, `PROVIDER_A`/`PROVIDER_B`).

Demo ma'lumotlar (`07-02-seed-demo-data.xml`):
- User One: `11111111-1111-1111-1111-111111111111` (balans: 500 000 UZS)
- User Two: `11111111-1111-1111-1111-111111111112` (balans: 1 000 UZS)
- Provider A: `aaaaaaaa-0001-0001-0001-aaaaaaaaaaaa`
- Provider B: `aaaaaaaa-0002-0002-0002-aaaaaaaaaaaa`

### Transaction yaratish (success)

```bash
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: <har-safar-yangi-UUID>" \
  -d '{
    "userId": "11111111-1111-1111-1111-111111111111",
    "providerId": "aaaaaaaa-0001-0001-0001-aaaaaaaaaaaa",
    "serviceAccount": "+998901234567",
    "amount": 50000,
    "currency": "UZS"
  }'
```
`201 Created`, `status: SUCCESS` yoki (~10% ehtimol bilan) `status: FAILED` qaytadi.

### Idempotency — bir xil key bilan qayta yuborish

Yuqoridagi so'rovni **xuddi shu** `Idempotency-Key` bilan qayta yuboring — endi `200 OK`, o'sha eski transaction qaytadi, pul qayta yechilmaydi.

### Xatolik holatlari

```bash
# Idempotency-Key header yo'q -> 400 IDEMPOTENCY_KEY_REQUIRED
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"userId":"11111111-1111-1111-1111-111111111111","providerId":"aaaaaaaa-0001-0001-0001-aaaaaaaaaaaa","serviceAccount":"+998901234567","amount":50000,"currency":"UZS"}'

# Balans yetarli emas -> 422 INSUFFICIENT_BALANCE (User Two, balans=1000)
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: <yangi-UUID>" \
  -d '{"userId":"11111111-1111-1111-1111-111111111112","providerId":"aaaaaaaa-0001-0001-0001-aaaaaaaaaaaa","serviceAccount":"+998901234567","amount":5000,"currency":"UZS"}'

# Mavjud bo'lmagan provider -> 400 PROVIDER_NOT_AVAILABLE
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: <yangi-UUID>" \
  -d '{"userId":"11111111-1111-1111-1111-111111111111","providerId":"00000000-0000-0000-0000-000000000000","serviceAccount":"+998901234567","amount":1000,"currency":"UZS"}'
```

### Transaction'larni ko'rish

```bash
# Bitta transaction
curl http://localhost:8080/api/v1/transactions/{transactionId}

# User bo'yicha ro'yxat (sahifalangan)
curl "http://localhost:8080/api/v1/transactions?userId=11111111-1111-1111-1111-111111111111&page=0&size=5"

# Status bo'yicha filtr
curl "http://localhost:8080/api/v1/transactions?userId=11111111-1111-1111-1111-111111111111&status=SUCCESS&page=0&size=5"
```

### Users va Providers

```bash
# Yangi user (201, telefon band bo'lsa 409 USER_ALREADY_EXISTS)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Yangi User","phone":"+998901112233","balance":100000,"currency":"UZS"}'

curl http://localhost:8080/api/v1/users/{userId}

# Yangi provider (201, kod band bo'lsa 409 PROVIDER_ALREADY_EXISTS)
curl -X POST http://localhost:8080/api/v1/providers \
  -H "Content-Type: application/json" \
  -d '{"code":"PROVIDER_C","name":"Provider C","active":true}'

curl http://localhost:8080/api/v1/providers/{providerId}
```