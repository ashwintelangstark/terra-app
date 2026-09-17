# WhatsApp Automation Documentation & System Memory

This document contains full technical specifications, file mappings, API access credentials, template structures, and workflow triggers for the WhatsApp Automation system integrated into the **Plot Perfect / Terra Site Manager** platform.

---

## 🔑 1. Meta API Access Credentials & Configuration

All Meta WhatsApp Cloud API calls interact with Meta Graph API `v19.0`.

### Environment & Code Keys:
* **Meta Access Token**:
  `EAAONO18mvZBYBSSgXtZCJ9Ya6ZC0ZCZAuZBs1MoeP0zjrKLXLstjaIeeYRrZAtZBkEsiTXyAPQK17sI5bXfKwSlyucnVCu12q9bKvhhAydhEFn9HgpYb2JWtO5EbR9RBPQ1mWHiAgL4ZCNVD5sl90aHzZB7T5hW0kB1mO2EvQQZCxZA2g9a5nJEz0XD3Mp1p8TT5tBzX7xD7UXehIZAutmQYmUGVPVhksxHBaBhQZAZBaiZBQHZCjrsNhafT8ff6CZAzMK7oVbMJNDdzSc7pukDZCikbZBixUGjipl3N8QZDZD`
* **Phone Number ID**: `1126770290524197`
* **Booking Confirmation Template**: `plot_booking_confirmation`
* **EMI Statement Template**: `customer_emi_statement_v2`
* **Default Language**: `en` (with auto-retry fallback to `en_US` if Meta error `#132001` occurs)

---

## 📁 2. File Location Reference & Responsibilities

| File Path | Description / Responsibility |
| :--- | :--- |
| **[`src/lib/whatsappService.ts`](file:///e:/projects/New%20folder/Shree%20Durga/src/lib/whatsappService.ts)** | Central Meta Cloud API integration module. Contains `sendBookingConfirmationWhatsApp`, `sendEMIStatementWhatsApp`, 1-tap `wa.me` deep link fallbacks, and error auto-retry handling. |
| **[`src/lib/emiStatementPDF.ts`](file:///e:/projects/New%20folder/Shree%20Durga/src/lib/emiStatementPDF.ts)** | Binary PDF document generator (`jsPDF`). Renders A4 formal statements and uploads them to Supabase Storage (`project-layouts/statements/`). |
| **[`.env`](file:///e:/projects/New%20folder/Shree%20Durga/.env)** | Stores environment variables (`VITE_WHATSAPP_API_TOKEN`, `VITE_WHATSAPP_PHONE_NUMBER_ID`, `VITE_WHATSAPP_TEMPLATE_NAME`, `VITE_WHATSAPP_EMI_TEMPLATE_NAME`). |
| **[`src/routes/_authenticated/plots.$plotId.book.checkout.tsx`](file:///e:/projects/New%20folder/Shree%20Durga/src/routes/_authenticated/plots.$plotId.book.checkout.tsx)** | Triggers automated WhatsApp confirmation upon completing a plot booking checkout. |
| **[`src/components/site-mapper/RecordPurchaserDialog.tsx`](file:///e:/projects/New%20folder/Shree%20Durga/src/components/site-mapper/RecordPurchaserDialog.tsx)** | Triggers automated WhatsApp confirmation when recording plot purchasers directly from the interactive site map. |
| **[`src/routes/_authenticated/bookings.tsx`](file:///e:/projects/New%20folder/Shree%20Durga/src/routes/_authenticated/bookings.tsx)** | Renders manual **WhatsApp** action buttons on the bookings data table. |
| **[`src/components/analytics/CustomerTallyLedgerModal.tsx`](file:///e:/projects/New%20folder/Shree%20Durga/src/components/analytics/CustomerTallyLedgerModal.tsx)** | Customer ledger modal containing **WhatsApp Text** and **Send WhatsApp PDF** action buttons. |
| **[`src/components/installments/EMIScheduleGeneratorDialog.tsx`](file:///e:/projects/New%20folder/Shree%20Durga/src/components/installments/EMIScheduleGeneratorDialog.tsx)** | Custom EMI schedule planner containing **Send WhatsApp PDF** action buttons. |

---

## 💬 3. WhatsApp Message Templates & Variables

### Template 1: `plot_booking_confirmation`
* **Category**: `Utility`
* **Trigger**: Automatic on plot reservation & purchaser record creation.
* **Variable Mapping**:
  * `{{1}}`: Customer Name (`Allen Uwe S`)
  * `{{2}}`: Project Name & Plot Number (`Royal Villa · Plot #F-12`)
  * `{{3}}`: Advance Paid (`₹7,16,666`)
  * `{{4}}`: Total Plot Price (`₹18,00,000`)

---

### Template 2: `customer_emi_statement_v2`
* **Category**: `Utility`
* **Header**: **`Document`** (Attached binary `.pdf` statement file)
* **Trigger**: **Send WhatsApp PDF** button in Installments & Customer Ledger modals.
* **Variable Mapping**:
  * `{{1}}`: Customer Name (`Allen Uwe S`)
  * `{{2}}`: Property Details (`Royal Villa · Plot #F-12`)
  * `{{3}}`: Total Contract Price (`₹18,00,000`)
  * `{{4}}`: Total Realized Amount (`₹7,16,666`)
  * `{{5}}`: Remaining Balance (`₹10,83,334`)
  * `{{6}}`: Paid Installment Progress (`3 of 12 EMIs`)
  * `{{7}}`: Pending Installments (`9 EMIs remaining`)
  * `{{8}}`: Next Due Date (`29 Aug 2026`)
  * `{{9}}`: Next Due Amount (`₹1,08,333`)

---

## ⚙️ 4. PDF Generation & Single Message Architecture

```
[User Clicks 'Send WhatsApp PDF']
              │
              ▼
  1. Generate Binary PDF (%PDF-1.4) via jsPDF
              │
              ▼
  2. Upload .pdf File to Supabase Storage ('project-layouts/statements/')
              │
              ▼
  3. Get Public HTTPS Document URL
              │
              ▼
  4. Dispatch Single Meta Cloud API Request:
     - Type: 'template'
     - Template Name: 'customer_emi_statement_v2'
     - Component 1: Header -> Document (PDF Public URL)
     - Component 2: Body -> Text Variables ({{1}} to {{9}})
              │
              ▼
[Customer Receives 1 Single WhatsApp Message with PDF Attached at Top]
```

---

## 🔄 5. How to Update Access Tokens in the Future

If Meta returns `(#190) Authentication Error` (token expired), update the token in **two places**:

1. **In [`.env`](file:///e:/projects/New%20folder/Shree%20Durga/.env)**:
   ```env
   VITE_WHATSAPP_API_TOKEN="YOUR_NEW_TOKEN_HERE"
   ```

2. **In [`src/lib/whatsappService.ts`](file:///e:/projects/New%20folder/Shree%20Durga/src/lib/whatsappService.ts)**:
   ```typescript
   const HARDCODED_ACCESS_TOKEN = "YOUR_NEW_TOKEN_HERE";
   ```

3. **In Vercel Production Environment Variables**:
   Update `VITE_WHATSAPP_API_TOKEN` under **Vercel ➔ Project Settings ➔ Environment Variables**.

---

### 🛡️ How to Get a Permanent Access Token (Never Expires):
1. Go to [Meta Business Settings](https://business.facebook.com/settings/).
2. Select **Users** ➔ **System Users** ➔ Click **Add** (Name: `WhatsApp Bot`, Role: `Admin`).
3. Click **Assign Assets** ➔ **WhatsApp Accounts** ➔ Enable **Full Control**.
4. Click **Generate New Token** ➔ App: `My Automation` ➔ Expiration: **Never**.
5. Check permissions: `whatsapp_business_messaging` & `whatsapp_business_management`.
6. Copy the token and paste it into `.env` & `whatsappService.ts`.
