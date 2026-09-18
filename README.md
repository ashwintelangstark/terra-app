# Terra Plots — Android Application

A modern, native Android application built with **Kotlin**, **Jetpack Compose**, and **Room Database** for real estate developers, sales teams, and land managers.

Terra Plots enables end-to-end management of plotted land developments: visual site layout inventory, leads CRM pipeline, formal plot bookings with 3-tier sequential approvals, milestone installment tracking, inter-project treasury transfers, and automated Tally Prime XML accounting synchronization.

---

## 🌟 Core Features Ported

### 1. Visual Site Mapper & Plot Inventory
- **Interactive Land Parcel Grid**: View all plots categorized by dimensions (1200, 1500, 1800, 2400 sqft), facing direction (East, North, West, South, Corner, Park Facing), and live availability.
- **Dynamic Pricing Calculator**: Instant calculation of total plot value based on base sqft rate plus premium charges for corner or park-facing plots.
- **Plot Hold & Reservation**: One-tap temporary reservation for interested prospective buyers.
- **Color-Coded Statuses**: Clear visual hierarchy matching original brand specifications:
  - `Available` (Emerald Green)
  - `Reserved` (Cobalt Blue)
  - `Booked` (Vibrant Orange)
  - `Sold` (Dark Crimson)

### 2. Leads Pipeline & CRM
- **6-Stage Sales Funnel**: New → Contacted → Meeting Scheduled → Negotiating → Converted → Dropped.
- **Action Shortcuts**: Direct dial phone integration and instant WhatsApp chat launch for sales executives.
- **Plot Attribution**: Link prospective buyers directly to specific plots of interest with budget tracking.
- **Convert to Booking**: Seamless transition from qualified lead to formal booking, pre-populating purchaser details.

### 3. Bookings & 3-Tier Sequential Approvals
- **Formal Deal Structuring**: Captures Purchaser details, PAN/Tax ID, Agreement Value, Government Guideline Value, Down Payment, and Payment Mode (RTGS/NEFT, Cheque, UPI, Net Banking).
- **Sequential Approval Workflow**:
  1. *CRM Verification*
  2. *Accounts & Payment Clearance*
  3. *Executive Management Signoff*
- **Cancellation & Refunds**: Structured cancellation workflow with reason logging and refund settlement calculation.

### 4. Installment Schedules & Milestone Ledger
- **Milestone Billing**: Automated generation of milestone-based installment schedules (Booking Advance, Agreement Signing, Infrastructure Laying, Final Registration).
- **Receipt Management**: Record collections with UTR/Cheque reference numbers and automated receipt numbering (`RCT-XXXXX`).

### 5. Treasury & Inter-Project Reallocation
- **Project Escrow Accounts**: Track bank accounts, account numbers, IFSC codes, and current balances across multiple developments (e.g. Grand Meadows, Royal Palms, Sierra Vista).
- **Inter-Project Fund Transfers**: Record working capital reallocation between project accounts with purpose narrations and reference tracking.

### 6. Tally Prime Accounting Integration
- **XML Schema Generator**: Generate and view standard Tally Prime XML payloads for Sales Vouchers (customer debit, sales revenue credit) and Journal Vouchers (inter-project transfers).
- **Sync Status**: Track synchronization state and Tally voucher numbers.

### 7. BDO Partners & Sales Team Incentives
- **Commission Calculations**: Automatically calculate percentage-based incentives on booked plots for sales representatives and channel partners.
- **Disbursal Tracking**: Record payout disbursements with banking references.

---

## 🏗️ Technical Architecture

- **Language**: Kotlin 2.1.0
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Design System**: Editorial Terracotta & Sand palette (`#E07A5F`, `#222222`, `#FDFBF7`, `#2E7D32`, `#2B5B84`)
- **State Management**: Android `ViewModel` + `StateFlow`
- **Local Persistence**: Android Room Database (`TerraDatabase`) with Coroutines & Reactive `Flow`
- **Navigation**: Compose Navigation with type-safe routing and edge-to-edge safe drawing insets
- **Launcher Icon**: Custom adaptive launcher icon with terracotta land parcel emblem

---

## 📂 Project Structure

```
app/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/
│   │   ├── MainActivity.kt               # App container & bottom navigation
│   │   ├── data/
│   │   │   ├── model/TerraModels.kt       # Domain enums & data structures
│   │   │   ├── local/TerraEntities.kt     # Room database entities
│   │   │   ├── local/TerraDao.kt          # Room DAO reactive queries
│   │   │   ├── local/TerraDatabase.kt     # Room database with seed data
│   │   │   └── repository/TerraRepository.kt
│   │   ├── ui/
│   │   │   ├── TerraViewModel.kt          # MVVM state holder & business logic
│   │   │   ├── theme/                     # Color, Typography, and M3 Theme
│   │   │   ├── components/                # Badges, StatCards, ApprovalPills
│   │   │   └── screens/
│   │   │       ├── DashboardScreen.kt     # Executive KPI overview
│   │   │       ├── SiteMapperScreen.kt    # Interactive plot inventory
│   │   │       ├── LeadsCrmScreen.kt      # Leads stage funnel & dialer
│   │   │       ├── BookingsScreen.kt      # Approvals & cancellation
│   │   │       ├── InstallmentsScreen.kt  # Milestone collection ledger
│   │   │       ├── TreasuryScreen.kt      # Escrow accounts & Tally sync
│   │   │       ├── IncentivesScreen.kt    # Sales commission disbursals
│   │   │       └── BookingFormScreen.kt   # Plot booking creation form
│   └── res/                               # Vector drawables, themes, mipmaps
├── build.gradle.kts
└── proguard-rules.pro
```
