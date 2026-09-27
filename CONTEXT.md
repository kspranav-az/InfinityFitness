# InfinityFitness — Working Context

Last updated: 2026-09-28
Branch: `v1.5-inf` (off `main`, which received build fixes from `InfinityFitnessFinal` via merge branch `merge-final-fixes`)

## Project Overview

Native Android gym-management app (Kotlin, Room, Firebase, WhatsApp Cloud API billing).
Package: `com.example.infinityfitness`. Single-module app under `app/`.

Core flows:
- Login (`MainActivity`, biometric + GYM/12321 fallback) → `home` (bottom nav: Home / Register / Due / Profile).
- Register customer (`RegisterFragment`) → saves `Customer` + initial `Subscription`, saves contact, sends WhatsApp bill.
- Customer list (`UserDataFragment`), due list (`DueCust`), customer detail / extend (`CustData` → `EditData` creates a new `Subscription` row = a "renewal").
- Renewal log (`RenewalLogActivity`) — paginated list of renewals (subscriptions) with date-range + text search, debounced.
- Bill preview dialog: WebView renders `assets/Bill.html`, PDF via `PdfService`, sent via WhatsApp Cloud API (`WhatsAppReportService`) or Android share sheet.

## Environment / Build

- JDK 17 required (kapt breaks on newer JDKs with `IllegalAccessError: ... jdk.compiler does not export com.sun.tools.javac.main`).
  ```bash
  export JAVA_HOME="$HOME/.sdkman/candidates/java/17.0.18-tem"
  export PATH="$JAVA_HOME/bin:$PATH"
  ./gradlew :app:assembleDebug --warning-mode=none
  ```
- Gradle wrapper pinned to **8.10.2** (8.7 download in `~/.gradle/wrapper/dists` was corrupt → `SocketTimeoutException`; 8.10.2 was already cached). Do not downgrade without re-downloading.
- Android SDK 34, AGP 8.1.0, portrait-locked activities in the manifest.
- Android Studio must use Gradle JDK 17 (`Invalid Gradle JDK configuration` dialog earlier → pick Embedded JDK or the sdkman 17 path).
- WhatsApp Cloud API token/phone-number-id live in `BuildConfig` fields (from `local.properties` via `build.gradle.kts`). Places API key is hardcoded in `RegisterFragment`/`EditData` and `AndroidManifest` (pre-existing; flagged as tech debt).

## Major Feature: New Bill Number Format (INF-YYMM-XXXX) — "Option B"

Decision: keep internal auto-increment `Long billNo` as primary key / FK; add human-readable
`Customer.displayBillNo` (format `INF-YYMM-XXXX`, e.g. `INF-2410-0039`). Serial resets per YYMM prefix
and is padded to 4 digits so lexicographic max works.

- `database/entity/Customer.kt`: new `displayBillNo` column + **unique** index `index_Customer_displayBillNo`; `billNo` remains `@PrimaryKey(autoGenerate = true)`.
- `database/GymDatabase.kt`: version **2**, `MIGRATION_1_2` — adds column, backfills `INF-<yyMM of JoiningDate>-<serial>` ordered by `JoiningDate, billNo`, then creates the unique index (index is created **after** backfill; creating it before backfill fails on duplicate empty strings).
- `database/dao/CustomerDao.kt`: `getCustomerByDisplayBillNo`, `getMaxDisplayBillNoForPrefix(prefix)` (drives serial generation), search queries (`getActiveCustomers`, `getActiveCustomersPaged`, `queryCustomers`) match `name / displayBillNo / phoneNumber / numeric billNo`.
- `database/dao/SubscriptionDao.kt`: `RenewalDetail` gains `displayBillNo`; both paged renewal queries select + search it.
- Generation: `RegisterFragment.generateNextDisplayBillNo()` — prefix from current date `yyMM`, next = max serial + 1, `%04d`. Bill no field in register/edit layouts is read-only (`inputType="none"`, `focusable="false"`).
- `EditData` preserves `displayBillNo` on customer update (extend flow reuses the same bill number — renewals share the customer's bill no; serial changes only per new registration month).
- Displayed everywhere instead of raw id: customer cards (`CustomerCardAdapter` binds `displayBillNo`; internal `customerId: Long` kept for navigation), `CustData` (`vbillno`), `EditData` (`edbillno`), renewal log (`RenewalLogAdapter`), bill preview / PDF filename / WhatsApp caption / saved contact name.
- `CustomerCard` data class: `customerId: Long` + `displayBillNo: String` + `payType`.

### Gotchas learned the hard way
- **Always check `layout-large/` variants** — the test emulator picks them; `layout-large/custcard.xml` had a 76dp `customerId` (truncated bill numbers) and `layout-large/cust_data.xml` was missing `vbillno` (caused the `findViewById(...) must not be null` crash). Both were fixed; any future layout change must be applied to both `layout/` and `layout-large/`.
- Serials in the migrated data don't always match `billNo` order within the same month (backfill ordered by `JoiningDate`); acceptable.
- Bill no fields made non-editable; if manual override is ever wanted, re-enable focus and validate uniqueness.

## Renewal Log

- `RenewalLogActivity` + `RenewalLogAdapter` (`renewal_log.xml`, `renewal_log_item.xml`).
- Paginated (15/page, `LIMIT/OFFSET`), debounced search (500 ms, `Job` cancellation), date range via `DatePickerDialog` fields (auto-loads on date pick).
- **Defaults to today** on open (From = To = today); **Show All** clears date + search fields and loads everything.
- Cards show photo (tap to enlarge, `dialog_image.xml`), name, amount, `displayBillNo`, phone, pack, date. All card text is white (`text_box` background is translucent grey).

## Customer Cards

- Redesigned `custcard.xml` / `layout-large/custcard.xml`: photo left (centerCrop), name + bill no + payType column, join/due dates column, info button right. No overlaps.
- `payType` is **bound to real data**: `UserDataFragment`/`DueCust` fetch latest subscription per customer (`SubscriptionDao.getSubscriptionById`) and pass `paymentMethod.name`. It was previously hardcoded "UPI" in the layout — don't regress this.

## Bill Share

- `bill_preview_dialog.xml` now has Cancel / **Share** / Send via WhatsApp.
- Share generates the PDF and fires `Intent.ACTION_SEND` (`application/pdf`, `EXTRA_STREAM` via `FileProvider`, `EXTRA_TEXT`/`EXTRA_SUBJECT`, `FLAG_GRANT_READ_URI_PERMISSION`) → system chooser (WhatsApp/Telegram/Signal/Gmail...). Implemented in `RegisterFragment.sharePdf` and `EditData.sharePdf`.
- WhatsApp Cloud API send (`sendPdfToWhatsApp`) unchanged; needs valid `BuildConfig.WHATSAPP_TOKEN` / `PHONE_NUMBER_ID`.

## Codebase Health Notes

- Heavy use of deprecated APIs (`startActivityForResult`, `onBackPressed`, `getBitmap`, `canAuthenticate()`) — only warnings so far.
- `UserDataFragment.loadCustomers` does per-customer DAO calls for payType (N+1, fine at page size 8).
- Duplicated bill-preview logic between `RegisterFragment` and `EditData` (candidate for extraction).
- Places API key + WhatsApp credentials handling should move to secure config before release.
- Commit history on this branch: build/JDK fixes → renewal log screen → login visibility/orientation fixes → renewal log detail/date/search → bill-number migration + UI redesign + share (this batch).

## Key Files

| Area | Files |
|---|---|
| DB schema/migration | `database/GymDatabase.kt`, `database/entity/Customer.kt`, `database/Converters.kt` |
| DAOs | `database/dao/CustomerDao.kt`, `SubscriptionDao.kt`, `PackDao.kt`, `UserDao.kt` |
| Screens | `MainActivity`, `home`, `fragments/RegisterFragment`, `fragments/UserDataFragment`, `DueCust`, `CustData`, `EditData`, `RenewalLogActivity` |
| Adapters | `Adpater/CustomerCardAdapter.kt`, `Adpater/RenewalLogAdapter.kt` |
| Billing | `services/WhatsAppReportService.kt`, `services/PdfService.kt`, `assets/Bill.html`, `layout/bill_preview_dialog.xml` |
| Layouts (remember layout-large twins) | `custcard.xml`, `cust_data.xml`, `register.xml`, `edit_data.xml`, `renewal_log.xml`, `renewal_log_item.xml` |
