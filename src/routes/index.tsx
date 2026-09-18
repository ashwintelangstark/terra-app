import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import {
  LayoutDashboard,
  Map,
  ClipboardList,
  WalletCards,
  Contact2,
  Landmark,
  Cloud,
  Send,
  CheckCircle2,
  Clock,
  AlertCircle,
  FileCode,
  Search,
  Phone,
  MessageSquare,
  Sparkles,
  RefreshCw,
  Smartphone,
  Maximize2,
  SlidersHorizontal,
  ChevronRight,
  ArrowUpRight,
  Plus,
  ShieldCheck,
  Check,
  Copy,
  X,
} from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Terra App — Mobile Real Estate Platform" },
      {
        name: "description",
        content: "Mobile layout for Terra real estate plot management, visual site map, bookings, installments, and Tally sync.",
      },
    ],
  }),
  component: TerraMobileAppScreen,
});

// Mock Initial Data matching the Android Room DB & Supabase setup
interface PlotItem {
  id: number;
  number: string;
  project: string;
  sizeSqFt: number;
  facing: string;
  status: "available" | "booked" | "reserved" | "blocked";
  price: number;
  ratePerSqFt: number;
}

interface BookingItem {
  id: number;
  customerName: string;
  customerPhone: string;
  plotNumber: string;
  projectName: string;
  agreementValue: number;
  downPayment: number;
  status: "PENDING_CRM" | "PENDING_ACCOUNTS" | "PENDING_MANAGEMENT" | "APPROVED";
  date: string;
}

interface InstallmentItem {
  id: number;
  buyerName: string;
  buyerPhone: string;
  plotNumber: string;
  installmentNo: number;
  totalInstallments: number;
  amount: number;
  dueDate: string;
  status: "UPCOMING" | "OVERDUE" | "PAID";
  paidDate?: string;
  receiptNumber?: string;
}

interface LeadItem {
  id: number;
  name: string;
  phone: string;
  projectName: string;
  budget: string;
  stage: "Inquiry" | "Site Visit" | "Negotiation" | "Token Paid";
  notes: string;
}

const INITIAL_PLOTS: PlotItem[] = [
  { id: 101, number: "101", project: "Emerald Palms", sizeSqFt: 1500, facing: "East", status: "booked", price: 3300000, ratePerSqFt: 2200 },
  { id: 102, number: "102", project: "Emerald Palms", sizeSqFt: 1500, facing: "North", status: "booked", price: 3300000, ratePerSqFt: 2200 },
  { id: 103, number: "103", project: "Emerald Palms", sizeSqFt: 1800, facing: "East", status: "available", price: 3960000, ratePerSqFt: 2200 },
  { id: 104, number: "104", project: "Emerald Palms", sizeSqFt: 1800, facing: "West", status: "available", price: 3960000, ratePerSqFt: 2200 },
  { id: 105, number: "105", project: "Emerald Palms", sizeSqFt: 2400, facing: "North-East Corner", status: "reserved", price: 5760000, ratePerSqFt: 2400 },
  { id: 106, number: "106", project: "Emerald Palms", sizeSqFt: 1500, facing: "South", status: "available", price: 3300000, ratePerSqFt: 2200 },
  { id: 107, number: "107", project: "Emerald Palms", sizeSqFt: 1500, facing: "East", status: "available", price: 3300000, ratePerSqFt: 2200 },
  { id: 108, number: "108", project: "Emerald Palms", sizeSqFt: 2000, facing: "North", status: "blocked", price: 4400000, ratePerSqFt: 2200 },
  { id: 109, number: "109", project: "Emerald Palms", sizeSqFt: 1500, facing: "West", status: "available", price: 3300000, ratePerSqFt: 2200 },
  { id: 110, number: "110", project: "Emerald Palms", sizeSqFt: 1800, facing: "East", status: "available", price: 3960000, ratePerSqFt: 2200 },
  { id: 111, number: "111", project: "Emerald Palms", sizeSqFt: 2100, facing: "North", status: "booked", price: 4620000, ratePerSqFt: 2200 },
  { id: 112, number: "112", project: "Emerald Palms", sizeSqFt: 1500, facing: "South", status: "available", price: 3300000, ratePerSqFt: 2200 },
];

const INITIAL_BOOKINGS: BookingItem[] = [
  {
    id: 1,
    customerName: "Priya Sharma",
    customerPhone: "+91 98765 43210",
    plotNumber: "102",
    projectName: "Emerald Palms",
    agreementValue: 3300000,
    downPayment: 500000,
    status: "APPROVED",
    date: "14 Sep 2026",
  },
  {
    id: 2,
    customerName: "Rajesh Kumar Patel",
    customerPhone: "+91 98220 12345",
    plotNumber: "101",
    projectName: "Emerald Palms",
    agreementValue: 3300000,
    downPayment: 500000,
    status: "PENDING_ACCOUNTS",
    date: "15 Sep 2026",
  },
  {
    id: 3,
    customerName: "Vikram Malhotra",
    customerPhone: "+91 99100 88776",
    plotNumber: "111",
    projectName: "Emerald Palms",
    agreementValue: 4620000,
    downPayment: 750000,
    status: "PENDING_CRM",
    date: "16 Sep 2026",
  },
];

const INITIAL_INSTALLMENTS: InstallmentItem[] = [
  {
    id: 1,
    buyerName: "Priya Sharma",
    buyerPhone: "+91 98765 43210",
    plotNumber: "102",
    installmentNo: 1,
    totalInstallments: 5,
    amount: 560000,
    dueDate: "01 Oct 2026",
    status: "UPCOMING",
  },
  {
    id: 2,
    buyerName: "Rajesh Kumar Patel",
    buyerPhone: "+91 98220 12345",
    plotNumber: "101",
    installmentNo: 1,
    totalInstallments: 5,
    amount: 560000,
    dueDate: "28 Sep 2026",
    status: "OVERDUE",
  },
  {
    id: 3,
    buyerName: "Anand Deshmukh",
    buyerPhone: "+91 94230 55443",
    plotNumber: "204",
    installmentNo: 2,
    totalInstallments: 5,
    amount: 600000,
    dueDate: "10 Sep 2026",
    status: "PAID",
    paidDate: "08 Sep 2026",
    receiptNumber: "REC-2026-098",
  },
];

const INITIAL_LEADS: LeadItem[] = [
  {
    id: 1,
    name: "Sunil Verma",
    phone: "+91 98112 33445",
    projectName: "Emerald Palms",
    budget: "₹35-40 Lakhs",
    stage: "Site Visit",
    notes: "Interested in East-facing 1500 sq.ft plot. Site visit booked for Saturday.",
  },
  {
    id: 2,
    name: "Deepika Rao",
    phone: "+91 97400 66778",
    projectName: "Palm Meadows",
    budget: "₹50-60 Lakhs",
    stage: "Negotiation",
    notes: "Discussing payment milestones. Requested 6-month EMI plan.",
  },
  {
    id: 3,
    name: "Amitabh Banerjee",
    phone: "+91 98301 22334",
    projectName: "Emerald Palms",
    budget: "₹45 Lakhs",
    stage: "Token Paid",
    notes: "Token ₹1,00,000 paid. Preparing booking document.",
  },
];

function TerraMobileAppScreen() {
  const [viewMode, setViewMode] = useState<"phone" | "full">("phone");
  const [activeTab, setActiveTab] = useState<"overview" | "plots" | "bookings" | "installments" | "leads" | "treasury">("overview");

  // State
  const [plots, setPlots] = useState<PlotItem[]>(INITIAL_PLOTS);
  const [bookings, setBookings] = useState<BookingItem[]>(INITIAL_BOOKINGS);
  const [installments, setInstallments] = useState<InstallmentItem[]>(INITIAL_INSTALLMENTS);
  const [leads] = useState<LeadItem[]>(INITIAL_LEADS);

  // Modals & Dialogs
  const [selectedPlot, setSelectedPlot] = useState<PlotItem | null>(null);
  const [plotFilter, setPlotFilter] = useState<string>("all");
  const [activeWhatsAppModal, setActiveWhatsAppModal] = useState<{
    type: "booking" | "emi";
    name: string;
    phone: string;
    details: string;
    url: string;
  } | null>(null);
  const [activeTallyModal, setActiveTallyModal] = useState<{
    title: string;
    xml: string;
  } | null>(null);
  const [activePaymentModal, setActivePaymentModal] = useState<InstallmentItem | null>(null);
  const [utrInput, setUtrInput] = useState<string>("");

  // Cloud integration states
  const [pingStatus, setPingStatus] = useState<"idle" | "pinging" | "success">("idle");
  const [syncStatus, setSyncStatus] = useState<"idle" | "syncing" | "synced">("idle");

  const handleTestPing = () => {
    setPingStatus("pinging");
    setTimeout(() => {
      setPingStatus("success");
      toast.success("✓ Supabase connection verified (Project: zolbuckwnjsxfgqqkcjj)");
    }, 900);
  };

  const handleSyncCloud = () => {
    setSyncStatus("syncing");
    setTimeout(() => {
      setSyncStatus("synced");
      toast.success(`✓ Synced ${bookings.length} plot booking(s) to Supabase cloud table!`);
    }, 1200);
  };

  const handleApproveBooking = (bookingId: number) => {
    setBookings((prev) =>
      prev.map((b) => {
        if (b.id !== bookingId) return b;
        if (b.status === "PENDING_CRM") return { ...b, status: "PENDING_ACCOUNTS" };
        if (b.status === "PENDING_ACCOUNTS") return { ...b, status: "PENDING_MANAGEMENT" };
        if (b.status === "PENDING_MANAGEMENT") return { ...b, status: "APPROVED" };
        return b;
      })
    );
    toast.success("Booking approval state updated successfully.");
  };

  const openWhatsAppBooking = (b: BookingItem) => {
    const text = `*TERRA PLOTS - BOOKING CONFIRMATION*\n\nDear ${b.customerName},\nYour plot booking for *Plot #${b.plotNumber}* in *${b.projectName}* has been processed.\n\n• Agreement Value: ₹${(b.agreementValue / 100000).toFixed(2)} Lakhs\n• Advance Paid: ₹${(b.downPayment / 100000).toFixed(2)} Lakhs\n• Booking Date: ${b.date}\n• Status: Confirmed\n\n_Meta Cloud WhatsApp Template: plot_booking_confirmation_\n_Phone ID: 1126770290524197_`;
    const encoded = encodeURIComponent(text);
    const url = `https://wa.me/${b.customerPhone.replace(/[^0-9]/g, "")}?text=${encoded}`;
    setActiveWhatsAppModal({
      type: "booking",
      name: b.customerName,
      phone: b.customerPhone,
      details: `Plot #${b.plotNumber} · ${b.projectName} · ₹${(b.agreementValue / 100000).toFixed(2)} L`,
      url,
    });
  };

  const openWhatsAppEmi = (i: InstallmentItem) => {
    const text = `*TERRA PLOTS - EMI PAYMENT STATEMENT*\n\nDear ${i.buyerName},\nStatement for *Plot #${i.plotNumber}*:\n\n• Installment: #${i.installmentNo} of ${i.totalInstallments}\n• Due Amount: ₹${(i.amount / 100000).toFixed(2)} Lakhs\n• Due Date: ${i.dueDate}\n• Status: ${i.status}\n\nPlease submit payment receipt via UTR.\n\n_Meta Cloud Template: customer_emi_statement_v2_\n_Phone ID: 1126770290524197_`;
    const encoded = encodeURIComponent(text);
    const url = `https://wa.me/${i.buyerPhone.replace(/[^0-9]/g, "")}?text=${encoded}`;
    setActiveWhatsAppModal({
      type: "emi",
      name: i.buyerName,
      phone: i.buyerPhone,
      details: `Installment #${i.installmentNo} · Due: ${i.dueDate} · ₹${(i.amount / 100000).toFixed(2)} L`,
      url,
    });
  };

  const openTallyXml = (b: BookingItem) => {
    const xml = `<ENVELOPE>
  <HEADER>
    <TALLYREQUEST>Import Data</TALLYREQUEST>
  </HEADER>
  <BODY>
    <IMPORTDATA>
      <REQUESTDESC>
        <REPORTNAME>Vouchers</REPORTNAME>
        <STATICVARIABLES>
          <SVCURRENTCOMPANY>HAEGL Tech</SVCURRENTCOMPANY>
        </STATICVARIABLES>
      </REQUESTDESC>
      <REQUESTDATA>
        <TALLYMESSAGE xmlns:UDF="TallyUDF">
          <VOUCHER VCHTYPE="Sales" ACTION="Create">
            <DATE>20260917</DATE>
            <VOUCHERTYPENAME>Sales</VOUCHERTYPENAME>
            <REFERENCE>TERRA-PL-${b.plotNumber}</REFERENCE>
            <PARTYLEDGERNAME>Customer - ${b.customerName}</PARTYLEDGERNAME>
            <NARRATION>Sale of Plot #${b.plotNumber} in ${b.projectName}</NARRATION>
            <ALLLEDGERENTRIES.LIST>
              <LEDGERNAME>Customer - ${b.customerName}</LEDGERNAME>
              <ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>
              <AMOUNT>-${b.agreementValue}</AMOUNT>
            </ALLLEDGERENTRIES.LIST>
            <ALLLEDGERENTRIES.LIST>
              <LEDGERNAME>Plot Sales Revenue</LEDGERNAME>
              <ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>
              <AMOUNT>${b.agreementValue}</AMOUNT>
            </ALLLEDGERENTRIES.LIST>
          </VOUCHER>
        </TALLYMESSAGE>
      </REQUESTDATA>
    </IMPORTDATA>
  </BODY>
</ENVELOPE>`;

    setActiveTallyModal({
      title: `Tally XML Voucher (Plot #${b.plotNumber})`,
      xml,
    });
  };

  const handleRecordReceipt = () => {
    if (!activePaymentModal || !utrInput) {
      toast.error("Please enter a valid payment UTR or reference number");
      return;
    }
    setInstallments((prev) =>
      prev.map((i) =>
        i.id === activePaymentModal.id
          ? {
              ...i,
              status: "PAID",
              paidDate: "17 Sep 2026",
              receiptNumber: utrInput,
            }
          : i
      )
    );
    toast.success(`Receipt recorded with UTR ${utrInput} for ${activePaymentModal.buyerName}`);
    setActivePaymentModal(null);
    setUtrInput("");
  };

  const filteredPlots = plots.filter((p) => {
    if (plotFilter === "all") return true;
    return p.status === plotFilter;
  });

  return (
    <div className="min-h-screen bg-neutral-900 text-neutral-100 flex flex-col items-center justify-start py-2 sm:py-6 px-0 sm:px-4 font-sans selection:bg-amber-600 selection:text-white">
      {/* Top Controls: Mobile View Mode Switcher */}
      <header className="w-full max-w-md mx-auto mb-3 px-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <div className="h-2.5 w-2.5 rounded-full bg-emerald-500 animate-pulse" />
          <span className="text-xs font-semibold uppercase tracking-wider text-neutral-300">Terra Mobile App</span>
        </div>

        <div className="flex items-center gap-1 bg-neutral-800 p-1 rounded-full border border-neutral-700">
          <button
            onClick={() => setViewMode("phone")}
            className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium transition-all ${
              viewMode === "phone" ? "bg-amber-600 text-white shadow-sm" : "text-neutral-400 hover:text-neutral-200"
            }`}
          >
            <Smartphone className="size-3.5" />
            <span>Mobile Phone</span>
          </button>
          <button
            onClick={() => setViewMode("full")}
            className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium transition-all ${
              viewMode === "full" ? "bg-amber-600 text-white shadow-sm" : "text-neutral-400 hover:text-neutral-200"
            }`}
          >
            <Maximize2 className="size-3.5" />
            <span>Full View</span>
          </button>
        </div>
      </header>

      {/* Main Container: Either Phone Frame or Full Responsive */}
      <div
        className={`w-full transition-all duration-300 ${
          viewMode === "phone"
            ? "max-w-[420px] rounded-[42px] border-[6px] border-neutral-800 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.9)] overflow-hidden bg-neutral-950 min-h-[840px] flex flex-col relative"
            : "max-w-4xl rounded-2xl border border-neutral-800 bg-neutral-950 shadow-2xl flex flex-col overflow-hidden min-h-[800px]"
        }`}
      >
        {/* Phone Dynamic Island & Status Bar (in Phone mode) */}
        {viewMode === "phone" && (
          <div className="w-full pt-3 px-6 pb-2 bg-neutral-950 flex items-center justify-between select-none text-[11px] font-semibold text-neutral-400 border-b border-neutral-900 z-50">
            <span>9:41</span>
            {/* Dynamic Island Pill */}
            <div className="h-4 w-24 bg-neutral-800 rounded-full flex items-center justify-center">
              <div className="h-1.5 w-1.5 rounded-full bg-neutral-600 mr-2" />
              <div className="h-2 w-2 rounded-full bg-neutral-900 border border-neutral-700" />
            </div>
            <div className="flex items-center gap-1.5">
              <span>5G</span>
              <span className="text-[9px]">100%</span>
            </div>
          </div>
        )}

        {/* Mobile Header Bar */}
        <div className="w-full bg-neutral-950/90 backdrop-blur-md px-4 py-3 border-b border-neutral-800 flex items-center justify-between sticky top-0 z-40">
          <div className="flex items-center gap-2.5">
            <div className="h-8 w-8 rounded-xl bg-amber-600/20 border border-amber-600/40 flex items-center justify-center font-bold text-amber-500 text-sm">
              T
            </div>
            <div>
              <div className="flex items-center gap-1.5">
                <span className="font-bold text-sm text-neutral-100">Terra App</span>
                <span className="px-1.5 py-0.5 rounded text-[9px] font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30">
                  v2.4
                </span>
              </div>
              <p className="text-[10px] text-neutral-400 flex items-center gap-1">
                <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                Cloud Synced · HAEGL Tech
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <div className="px-2 py-1 rounded-md bg-neutral-800 text-[10px] font-medium text-neutral-300 border border-neutral-700">
              Admin
            </div>
          </div>
        </div>

        {/* Scrollable Mobile Content Body */}
        <div className="flex-1 overflow-y-auto pb-24 p-3.5 space-y-4 bg-neutral-950">
          {/* TAB 1: OVERVIEW / DASHBOARD */}
          {activeTab === "overview" && (
            <div className="space-y-4 animate-in fade-in duration-200">
              {/* Connected Cloud Services Status Card */}
              <div className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <CloudQueue className="size-4 text-amber-500" />
                    <span className="text-xs font-bold text-neutral-200">Connected Services</span>
                  </div>
                  <span className="text-[10px] px-2 py-0.5 rounded-full font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                    LIVE CONFIG
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-semibold text-neutral-400">Supabase</span>
                      <span className="text-[9px] text-emerald-400 font-bold">READY</span>
                    </div>
                    <p className="text-[11px] text-neutral-200 font-medium truncate">zolbuckwnjsxfgqqkcjj</p>
                    <button
                      onClick={handleTestPing}
                      disabled={pingStatus === "pinging"}
                      className="w-full mt-1.5 py-1 px-2 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-[10px] font-medium text-amber-400 border border-neutral-700 flex items-center justify-center gap-1 transition-colors"
                    >
                      <RefreshCw className={`size-2.5 ${pingStatus === "pinging" ? "animate-spin" : ""}`} />
                      {pingStatus === "pinging" ? "Testing..." : "Test Ping"}
                    </button>
                  </div>

                  <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="text-[10px] font-semibold text-neutral-400">WhatsApp API</span>
                      <span className="text-[9px] text-emerald-400 font-bold">ACTIVE</span>
                    </div>
                    <p className="text-[11px] text-neutral-200 font-medium truncate">ID: 1126770290524197</p>
                    <button
                      onClick={handleSyncCloud}
                      disabled={syncStatus === "syncing"}
                      className="w-full mt-1.5 py-1 px-2 rounded-lg bg-neutral-800 hover:bg-neutral-700 text-[10px] font-medium text-emerald-400 border border-neutral-700 flex items-center justify-center gap-1 transition-colors"
                    >
                      <RefreshCw className={`size-2.5 ${syncStatus === "syncing" ? "animate-spin" : ""}`} />
                      {syncStatus === "syncing" ? "Syncing..." : "Sync Cloud"}
                    </button>
                  </div>
                </div>
              </div>

              {/* 2x2 Metric KPI Grid */}
              <div className="grid grid-cols-2 gap-2.5">
                <div className="p-3 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-1">
                  <span className="text-[10px] font-medium text-neutral-400">Gross Sales Value</span>
                  <div className="text-lg font-bold text-neutral-100">₹1.85 Cr</div>
                  <span className="text-[10px] text-emerald-400 font-medium">↑ +18% this month</span>
                </div>
                <div className="p-3 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-1">
                  <span className="text-[10px] font-medium text-neutral-400">Plots Sold / Total</span>
                  <div className="text-lg font-bold text-amber-500">48 / 60</div>
                  <span className="text-[10px] text-neutral-400 font-medium">80% Inventory Sold</span>
                </div>
                <div className="p-3 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-1">
                  <span className="text-[10px] font-medium text-neutral-400">Pending Approvals</span>
                  <div className="text-lg font-bold text-amber-400">3</div>
                  <span className="text-[10px] text-amber-400/80 font-medium">Action required</span>
                </div>
                <div className="p-3 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-1">
                  <span className="text-[10px] font-medium text-neutral-400">Collections (Escrow)</span>
                  <div className="text-lg font-bold text-emerald-400">₹42.5 L</div>
                  <span className="text-[10px] text-neutral-400 font-medium">HDFC Bank A/c</span>
                </div>
              </div>

              {/* Active Projects Summary */}
              <div className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-neutral-200">Active Layouts</span>
                  <button
                    onClick={() => setActiveTab("plots")}
                    className="text-[10px] text-amber-500 font-semibold flex items-center hover:underline"
                  >
                    View Map <ChevronRight className="size-3" />
                  </button>
                </div>

                <div className="space-y-2">
                  <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 flex items-center justify-between">
                    <div>
                      <div className="text-xs font-bold text-neutral-200">Emerald Palms</div>
                      <div className="text-[10px] text-neutral-400">Devenahalli Highway · 24 Plots</div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs font-bold text-emerald-400">18 Booked</div>
                      <div className="text-[10px] text-neutral-400">6 Available</div>
                    </div>
                  </div>

                  <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 flex items-center justify-between">
                    <div>
                      <div className="text-xs font-bold text-neutral-200">Palm Meadows</div>
                      <div className="text-[10px] text-neutral-400">Sarjapur Extension · 20 Plots</div>
                    </div>
                    <div className="text-right">
                      <div className="text-xs font-bold text-amber-400">15 Booked</div>
                      <div className="text-[10px] text-neutral-400">5 Available</div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Recent Bookings Quick Access */}
              <div className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-neutral-200">Recent Bookings</span>
                  <button
                    onClick={() => setActiveTab("bookings")}
                    className="text-[10px] text-amber-500 font-semibold flex items-center hover:underline"
                  >
                    All Bookings <ChevronRight className="size-3" />
                  </button>
                </div>

                {bookings.map((b) => (
                  <div
                    key={b.id}
                    className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 flex items-center justify-between"
                  >
                    <div>
                      <div className="text-xs font-bold text-neutral-200">{b.customerName}</div>
                      <div className="text-[10px] text-neutral-400">Plot #{b.plotNumber} · {b.projectName}</div>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <button
                        onClick={() => openWhatsAppBooking(b)}
                        className="p-1.5 rounded-lg bg-emerald-950 text-emerald-400 border border-emerald-800 hover:bg-emerald-900 transition-colors"
                        title="Send WhatsApp"
                      >
                        <Send className="size-3" />
                      </button>
                      <button
                        onClick={() => openTallyXml(b)}
                        className="p-1.5 rounded-lg bg-neutral-800 text-neutral-300 border border-neutral-700 hover:bg-neutral-700 transition-colors"
                        title="Tally XML"
                      >
                        <FileCode className="size-3" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* TAB 2: SITE MAP & PLOTS */}
          {activeTab === "plots" && (
            <div className="space-y-4 animate-in fade-in duration-200">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-neutral-100">Site Map Visualizer</h3>
                  <p className="text-[10px] text-neutral-400">Emerald Palms Masterplan</p>
                </div>
                <div className="flex items-center gap-1 text-[10px]">
                  <span className="flex items-center gap-1 text-emerald-400"><span className="h-2 w-2 rounded-full bg-emerald-500" />Avail</span>
                  <span className="flex items-center gap-1 text-amber-500"><span className="h-2 w-2 rounded-full bg-amber-500" />Booked</span>
                  <span className="flex items-center gap-1 text-blue-400"><span className="h-2 w-2 rounded-full bg-blue-500" />Resvd</span>
                </div>
              </div>

              {/* Status Filter Chips */}
              <div className="flex items-center gap-1.5 overflow-x-auto pb-1 text-xs">
                {["all", "available", "booked", "reserved", "blocked"].map((f) => (
                  <button
                    key={f}
                    onClick={() => setPlotFilter(f)}
                    className={`px-3 py-1 rounded-full capitalize text-[11px] font-medium whitespace-nowrap transition-colors ${
                      plotFilter === f
                        ? "bg-amber-600 text-white font-semibold"
                        : "bg-neutral-900 text-neutral-400 hover:text-neutral-200 border border-neutral-800"
                    }`}
                  >
                    {f}
                  </button>
                ))}
              </div>

              {/* Interactive Grid of Plots */}
              <div className="grid grid-cols-3 gap-2.5">
                {filteredPlots.map((plot) => {
                  let badgeColor = "bg-emerald-950/80 border-emerald-700 text-emerald-300";
                  if (plot.status === "booked") badgeColor = "bg-amber-950/80 border-amber-700 text-amber-300";
                  if (plot.status === "reserved") badgeColor = "bg-blue-950/80 border-blue-700 text-blue-300";
                  if (plot.status === "blocked") badgeColor = "bg-neutral-900 border-neutral-700 text-neutral-500";

                  return (
                    <button
                      key={plot.id}
                      onClick={() => setSelectedPlot(plot)}
                      className={`p-3 rounded-2xl border flex flex-col items-center justify-center text-center transition-all hover:scale-[1.03] active:scale-95 ${badgeColor}`}
                    >
                      <span className="text-[10px] uppercase font-bold tracking-wider opacity-80">Plot</span>
                      <span className="text-base font-extrabold my-0.5">#{plot.number}</span>
                      <span className="text-[9px] font-medium">{plot.sizeSqFt} sq.ft</span>
                      <span className="text-[8px] opacity-80 mt-1 capitalize">{plot.status}</span>
                    </button>
                  );
                })}
              </div>

              <p className="text-[10px] text-center text-neutral-500">
                Tap any plot to inspect dimensions, square yardage, facing, and launch booking.
              </p>
            </div>
          )}

          {/* TAB 3: BOOKINGS */}
          {activeTab === "bookings" && (
            <div className="space-y-3.5 animate-in fade-in duration-200">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-neutral-100">Plot Bookings</h3>
                  <p className="text-[10px] text-neutral-400">Sequential approval pipeline</p>
                </div>
                <span className="text-xs font-bold text-amber-500">{bookings.length} Active</span>
              </div>

              {bookings.map((b) => (
                <div key={b.id} className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-3">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="text-xs font-bold text-neutral-100">{b.customerName}</div>
                      <div className="text-[11px] text-neutral-400">{b.customerPhone}</div>
                      <div className="text-[11px] font-medium text-amber-500 mt-0.5">
                        Plot #{b.plotNumber} · {b.projectName}
                      </div>
                    </div>
                    <span
                      className={`text-[9px] px-2 py-0.5 rounded-md font-bold uppercase tracking-wider ${
                        b.status === "APPROVED"
                          ? "bg-emerald-950 text-emerald-400 border border-emerald-800"
                          : "bg-amber-950 text-amber-400 border border-amber-800"
                      }`}
                    >
                      {b.status.replace("_", " ")}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-xs pt-2 border-t border-neutral-800/80">
                    <div>
                      <span className="text-[10px] text-neutral-500">Agreement</span>
                      <div className="font-bold text-neutral-200">₹{(b.agreementValue / 100000).toFixed(2)} L</div>
                    </div>
                    <div>
                      <span className="text-[10px] text-neutral-500">Advance Paid</span>
                      <div className="font-bold text-emerald-400">₹{(b.downPayment / 100000).toFixed(2)} L</div>
                    </div>
                    <div className="text-right">
                      <span className="text-[10px] text-neutral-500">Date</span>
                      <div className="text-neutral-300 font-medium">{b.date}</div>
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div className="flex items-center gap-2 pt-2 border-t border-neutral-800/80">
                    <button
                      onClick={() => openWhatsAppBooking(b)}
                      className="flex-1 py-1.5 px-2 rounded-xl bg-emerald-950/70 hover:bg-emerald-900 text-emerald-300 border border-emerald-800 text-[11px] font-medium flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <Send className="size-3" />
                      <span>WhatsApp</span>
                    </button>

                    <button
                      onClick={() => openTallyXml(b)}
                      className="flex-1 py-1.5 px-2 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-200 border border-neutral-700 text-[11px] font-medium flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <FileCode className="size-3" />
                      <span>Tally XML</span>
                    </button>

                    {b.status !== "APPROVED" && (
                      <button
                        onClick={() => handleApproveBooking(b.id)}
                        className="py-1.5 px-2.5 rounded-xl bg-amber-600 hover:bg-amber-500 text-white text-[11px] font-medium flex items-center justify-center gap-1 transition-colors"
                        title="Advance Approval Stage"
                      >
                        <Check className="size-3" />
                        <span>Approve</span>
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* TAB 4: INSTALLMENTS / LEDGER */}
          {activeTab === "installments" && (
            <div className="space-y-3.5 animate-in fade-in duration-200">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-neutral-100">Installment Ledger</h3>
                  <p className="text-[10px] text-neutral-400">Milestone receipts & UTR sync</p>
                </div>
              </div>

              {installments.map((i) => (
                <div key={i.id} className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-3">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="text-xs font-bold text-neutral-100">{i.buyerName}</div>
                      <div className="text-[11px] text-neutral-400">Plot #{i.plotNumber} · Inst #{i.installmentNo}/{i.totalInstallments}</div>
                    </div>
                    <span
                      className={`text-[9px] px-2 py-0.5 rounded-md font-bold uppercase tracking-wider ${
                        i.status === "PAID"
                          ? "bg-emerald-950 text-emerald-400 border border-emerald-800"
                          : i.status === "OVERDUE"
                          ? "bg-red-950 text-red-400 border border-red-800"
                          : "bg-amber-950 text-amber-400 border border-amber-800"
                      }`}
                    >
                      {i.status}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-xs pt-2 border-t border-neutral-800/80">
                    <div>
                      <span className="text-[10px] text-neutral-500">Amount Due</span>
                      <div className="font-bold text-neutral-200">₹{(i.amount / 100000).toFixed(2)} L</div>
                    </div>
                    <div>
                      <span className="text-[10px] text-neutral-500">Due Date</span>
                      <div className="font-semibold text-neutral-300">{i.dueDate}</div>
                    </div>
                    {i.receiptNumber && (
                      <div className="text-right">
                        <span className="text-[10px] text-neutral-500">Receipt Ref</span>
                        <div className="font-mono text-[10px] text-emerald-400">{i.receiptNumber}</div>
                      </div>
                    )}
                  </div>

                  <div className="flex items-center gap-2 pt-2 border-t border-neutral-800/80">
                    <button
                      onClick={() => openWhatsAppEmi(i)}
                      className="flex-1 py-1.5 px-2 rounded-xl bg-emerald-950/70 hover:bg-emerald-900 text-emerald-300 border border-emerald-800 text-[11px] font-medium flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <Send className="size-3" />
                      <span>WhatsApp Statement</span>
                    </button>

                    {i.status !== "PAID" && (
                      <button
                        onClick={() => {
                          setActivePaymentModal(i);
                          setUtrInput(`UTR-${Math.floor(100000 + Math.random() * 900000)}`);
                        }}
                        className="py-1.5 px-3 rounded-xl bg-amber-600 hover:bg-amber-500 text-white text-[11px] font-medium flex items-center justify-center gap-1 transition-colors"
                      >
                        <WalletCards className="size-3" />
                        <span>Record Receipt</span>
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* TAB 5: LEADS CRM */}
          {activeTab === "leads" && (
            <div className="space-y-3.5 animate-in fade-in duration-200">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-neutral-100">Leads CRM Pipeline</h3>
                  <p className="text-[10px] text-neutral-400">Prospective buyer follow-ups</p>
                </div>
              </div>

              {leads.map((lead) => (
                <div key={lead.id} className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-2.5">
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="text-xs font-bold text-neutral-100">{lead.name}</div>
                      <div className="text-[11px] text-neutral-400">{lead.phone}</div>
                    </div>
                    <span className="text-[9px] px-2 py-0.5 rounded-md font-bold uppercase tracking-wider bg-amber-950 text-amber-400 border border-amber-800">
                      {lead.stage}
                    </span>
                  </div>

                  <div className="text-xs text-neutral-300">
                    <span className="text-neutral-500">Project:</span> {lead.projectName} · <span className="text-neutral-500">Budget:</span> {lead.budget}
                  </div>
                  <p className="text-[11px] text-neutral-400 bg-neutral-950 p-2 rounded-xl border border-neutral-800/80">
                    "{lead.notes}"
                  </p>

                  <div className="flex items-center gap-2 pt-1">
                    <a
                      href={`tel:${lead.phone}`}
                      className="flex-1 py-1.5 px-2 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-200 border border-neutral-700 text-[11px] font-medium flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <Phone className="size-3" />
                      <span>Call Buyer</span>
                    </a>
                    <a
                      href={`https://wa.me/${lead.phone.replace(/[^0-9]/g, "")}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 py-1.5 px-2 rounded-xl bg-emerald-950 hover:bg-emerald-900 text-emerald-300 border border-emerald-800 text-[11px] font-medium flex items-center justify-center gap-1.5 transition-colors"
                    >
                      <Send className="size-3" />
                      <span>WhatsApp Chat</span>
                    </a>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* TAB 6: TREASURY & TALLY */}
          {activeTab === "treasury" && (
            <div className="space-y-3.5 animate-in fade-in duration-200">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-neutral-100">Project Treasury</h3>
                  <p className="text-[10px] text-neutral-400">Escrow balances & Tally sync</p>
                </div>
                <span className="text-xs font-bold text-emerald-400">HAEGL Tech</span>
              </div>

              <div className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-3">
                <span className="text-xs font-bold text-neutral-200">Project Bank Accounts</span>
                <div className="space-y-2">
                  <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 flex items-center justify-between">
                    <div>
                      <div className="text-xs font-bold text-neutral-200">Emerald Palms Escrow</div>
                      <div className="text-[10px] text-neutral-400">HDFC Bank A/c #9921</div>
                    </div>
                    <div className="text-right font-bold text-emerald-400 text-sm">₹72,50,000</div>
                  </div>
                  <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 flex items-center justify-between">
                    <div>
                      <div className="text-xs font-bold text-neutral-200">Palm Meadows Escrow</div>
                      <div className="text-[10px] text-neutral-400">ICICI Bank A/c #4412</div>
                    </div>
                    <div className="text-right font-bold text-emerald-400 text-sm">₹48,00,000</div>
                  </div>
                </div>
              </div>

              <div className="p-3.5 rounded-2xl bg-neutral-900 border border-neutral-800 space-y-2.5">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-bold text-neutral-200">Tally XML Connector</span>
                  <span className="text-[10px] text-emerald-400 font-bold">READY</span>
                </div>
                <p className="text-[11px] text-neutral-400">
                  Every booking and installment is automatically formatted into standard Tally Prime XML vouchers for company <strong className="text-neutral-200">HAEGL Tech</strong>.
                </p>
                <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800 font-mono text-[10px] text-neutral-300">
                  POST http://localhost:9000 &rarr; &lt;CREATED&gt;1&lt;/CREATED&gt;
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Native Mobile Bottom Navigation Bar */}
        <nav className="w-full bg-neutral-950/95 backdrop-blur-xl border-t border-neutral-800/80 px-2 py-2 flex items-center justify-around z-40 absolute bottom-0 inset-x-0">
          {[
            { id: "overview", label: "Overview", icon: LayoutDashboard },
            { id: "plots", label: "Site Map", icon: Map },
            { id: "bookings", label: "Bookings", icon: ClipboardList },
            { id: "installments", label: "Ledger", icon: WalletCards },
            { id: "leads", label: "Leads", icon: Contact2 },
            { id: "treasury", label: "Treasury", icon: Landmark },
          ].map((tab) => {
            const active = activeTab === tab.id;
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`flex flex-col items-center justify-center py-1 px-2 rounded-xl transition-all ${
                  active ? "text-amber-500 font-bold scale-105" : "text-neutral-400 hover:text-neutral-200"
                }`}
              >
                <Icon className={`size-4 mb-0.5 ${active ? "text-amber-500" : "text-neutral-400"}`} />
                <span className="text-[10px] tracking-tight">{tab.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Plot Details Modal */}
        {selectedPlot && (
          <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-end sm:items-center justify-center p-0 sm:p-4">
            <div className="w-full max-w-sm bg-neutral-900 border border-neutral-800 rounded-t-3xl sm:rounded-3xl p-5 space-y-4 shadow-2xl animate-in slide-in-from-bottom duration-200">
              <div className="flex items-center justify-between">
                <div>
                  <span className="text-[10px] uppercase font-bold text-amber-500">Plot Details</span>
                  <h4 className="text-lg font-bold text-neutral-100">Plot #{selectedPlot.number}</h4>
                </div>
                <button
                  onClick={() => setSelectedPlot(null)}
                  className="p-1.5 rounded-full bg-neutral-800 text-neutral-400 hover:text-white"
                >
                  <X className="size-4" />
                </button>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs">
                <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800">
                  <span className="text-[10px] text-neutral-500">Area</span>
                  <div className="font-bold text-neutral-200">{selectedPlot.sizeSqFt} sq.ft</div>
                  <div className="text-[10px] text-neutral-400">{(selectedPlot.sizeSqFt / 9).toFixed(1)} sq.yd</div>
                </div>
                <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800">
                  <span className="text-[10px] text-neutral-500">Facing</span>
                  <div className="font-bold text-neutral-200">{selectedPlot.facing}</div>
                </div>
                <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800">
                  <span className="text-[10px] text-neutral-500">Base Rate</span>
                  <div className="font-bold text-neutral-200">₹{selectedPlot.ratePerSqFt} / sq.ft</div>
                </div>
                <div className="p-2.5 rounded-xl bg-neutral-950 border border-neutral-800">
                  <span className="text-[10px] text-neutral-500">Total Price</span>
                  <div className="font-bold text-amber-500">₹{(selectedPlot.price / 100000).toFixed(2)} Lakhs</div>
                </div>
              </div>

              <button
                onClick={() => {
                  toast.success(`Booking form launched for Plot #${selectedPlot.number}!`);
                  setSelectedPlot(null);
                  setActiveTab("bookings");
                }}
                className="w-full py-2.5 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-semibold text-xs flex items-center justify-center gap-2 transition-colors"
              >
                <span>Book Plot #{selectedPlot.number}</span>
                <ArrowUpRight className="size-4" />
              </button>
            </div>
          </div>
        )}

        {/* WhatsApp Dispatch Modal */}
        {activeWhatsAppModal && (
          <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-end sm:items-center justify-center p-0 sm:p-4">
            <div className="w-full max-w-sm bg-neutral-900 border border-neutral-800 rounded-t-3xl sm:rounded-3xl p-5 space-y-4 shadow-2xl animate-in slide-in-from-bottom duration-200">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Send className="size-4 text-emerald-400" />
                  <h4 className="text-sm font-bold text-neutral-100">WhatsApp Cloud Dispatch</h4>
                </div>
                <button
                  onClick={() => setActiveWhatsAppModal(null)}
                  className="p-1.5 rounded-full bg-neutral-800 text-neutral-400 hover:text-white"
                >
                  <X className="size-4" />
                </button>
              </div>

              <div className="p-3 rounded-xl bg-neutral-950 border border-neutral-800 space-y-1.5 text-xs">
                <div className="text-neutral-400">Recipient: <strong className="text-neutral-200">{activeWhatsAppModal.name}</strong></div>
                <div className="text-neutral-400">Phone: <strong className="text-neutral-200">{activeWhatsAppModal.phone}</strong></div>
                <div className="text-neutral-400">Subject: <span className="text-neutral-300">{activeWhatsAppModal.details}</span></div>
                <div className="text-[10px] text-emerald-400 font-mono mt-1">
                  Template: {activeWhatsAppModal.type === "booking" ? "plot_booking_confirmation" : "customer_emi_statement_v2"}
                </div>
              </div>

              <div className="flex items-center gap-2">
                <a
                  href={activeWhatsAppModal.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  onClick={() => {
                    toast.success(`WhatsApp message sent to ${activeWhatsAppModal.name}`);
                    setActiveWhatsAppModal(null);
                  }}
                  className="flex-1 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs flex items-center justify-center gap-2 transition-colors"
                >
                  <span>Open WhatsApp</span>
                  <ArrowUpRight className="size-3.5" />
                </a>
                <button
                  onClick={() => setActiveWhatsAppModal(null)}
                  className="py-2 px-3 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-300 text-xs font-medium"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Tally XML Viewer Modal */}
        {activeTallyModal && (
          <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-end sm:items-center justify-center p-0 sm:p-4">
            <div className="w-full max-w-md bg-neutral-900 border border-neutral-800 rounded-t-3xl sm:rounded-3xl p-5 space-y-3 shadow-2xl animate-in slide-in-from-bottom duration-200">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <FileCode className="size-4 text-amber-500" />
                  <h4 className="text-sm font-bold text-neutral-100">{activeTallyModal.title}</h4>
                </div>
                <button
                  onClick={() => setActiveTallyModal(null)}
                  className="p-1.5 rounded-full bg-neutral-800 text-neutral-400 hover:text-white"
                >
                  <X className="size-4" />
                </button>
              </div>

              <p className="text-[11px] text-neutral-400">
                Company Target: <strong className="text-neutral-200">HAEGL Tech</strong>. Ready to import via Tally Prime HTTP XML Port 9000.
              </p>

              <pre className="p-3 rounded-xl bg-neutral-950 border border-neutral-800 text-[10px] font-mono text-emerald-400 overflow-x-auto max-h-48 whitespace-pre">
                {activeTallyModal.xml}
              </pre>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => {
                    navigator.clipboard.writeText(activeTallyModal.xml);
                    toast.success("Tally XML envelope copied to clipboard!");
                  }}
                  className="flex-1 py-2 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-semibold text-xs flex items-center justify-center gap-1.5 transition-colors"
                >
                  <Copy className="size-3.5" />
                  <span>Copy XML</span>
                </button>
                <button
                  onClick={() => setActiveTallyModal(null)}
                  className="py-2 px-3 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-300 text-xs font-medium"
                >
                  Done
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Record Receipt Modal */}
        {activePaymentModal && (
          <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-xs flex items-end sm:items-center justify-center p-0 sm:p-4">
            <div className="w-full max-w-sm bg-neutral-900 border border-neutral-800 rounded-t-3xl sm:rounded-3xl p-5 space-y-3.5 shadow-2xl animate-in slide-in-from-bottom duration-200">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-bold text-neutral-100">Record Payment Receipt</h4>
                <button
                  onClick={() => setActivePaymentModal(null)}
                  className="p-1.5 rounded-full bg-neutral-800 text-neutral-400 hover:text-white"
                >
                  <X className="size-4" />
                </button>
              </div>

              <p className="text-xs text-neutral-400">
                Recording payment of <strong className="text-neutral-200">₹{(activePaymentModal.amount / 100000).toFixed(2)} Lakhs</strong> for {activePaymentModal.buyerName} (Plot #{activePaymentModal.plotNumber}).
              </p>

              <div className="space-y-1.5">
                <label className="text-[11px] text-neutral-400 font-medium">Bank UTR / Transaction Reference</label>
                <input
                  type="text"
                  value={utrInput}
                  onChange={(e) => setUtrInput(e.target.value)}
                  placeholder="e.g. UTR-829102"
                  className="w-full px-3 py-2 rounded-xl bg-neutral-950 border border-neutral-700 text-xs text-neutral-100 placeholder-neutral-500 focus:outline-none focus:border-amber-500 font-mono"
                />
              </div>

              <div className="flex items-center gap-2 pt-1">
                <button
                  onClick={handleRecordReceipt}
                  className="flex-1 py-2 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-semibold text-xs transition-colors"
                >
                  Confirm & Generate Receipt
                </button>
                <button
                  onClick={() => setActivePaymentModal(null)}
                  className="py-2 px-3 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-neutral-300 text-xs font-medium"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
