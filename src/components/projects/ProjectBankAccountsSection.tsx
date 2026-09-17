import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Building2,
  CreditCard,
  Plus,
  Copy,
  Check,
  Star,
  Eye,
  EyeOff,
  Pencil,
  Trash2,
  ShieldCheck,
  Landmark,
} from "lucide-react";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";
import { AddEditBankAccountDialog } from "./AddEditBankAccountDialog";
import { ConfirmDeleteBankAccountDialog } from "./ConfirmDeleteBankAccountDialog";

interface ProjectBankAccountsSectionProps {
  projectId: string;
  projectName: string;
  canEdit?: boolean;
}

const TYPE_BADGES: Record<string, { label: string; style: string }> = {
  Escrow: {
    label: "RERA Escrow",
    style: "bg-blue-500/15 text-blue-700 dark:text-blue-400 border-blue-500/30",
  },
  Current: {
    label: "Current A/C",
    style: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 border-emerald-500/30",
  },
  Collection: {
    label: "Collection",
    style: "bg-purple-500/15 text-purple-700 dark:text-purple-400 border-purple-500/30",
  },
  Operating: {
    label: "Operating",
    style: "bg-amber-500/15 text-amber-700 dark:text-amber-400 border-amber-500/30",
  },
};

export function ProjectBankAccountsSection({
  projectId,
  projectName,
  canEdit = true,
}: ProjectBankAccountsSectionProps) {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState<any | null>(null);

  const [deletingAccount, setDeletingAccount] = useState<any | null>(null);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [revealedIds, setRevealedIds] = useState<Set<string>>(new Set());

  // Fetch project bank accounts
  const { data: bankAccounts = [], isLoading } = useQuery({
    queryKey: ["project-bank-accounts", projectId],
    queryFn: async () => {
      const { data, error } = await (supabase as any)
        .from("project_bank_accounts")
        .select("*")
        .eq("project_id", projectId)
        .order("is_primary", { ascending: false })
        .order("created_at", { ascending: true });
      if (error) {
        console.error("Error fetching project_bank_accounts:", error);
        return [];
      }
      return data ?? [];
    },
  });

  // Set Primary Mutation
  const setPrimaryMutation = useMutation({
    mutationFn: async (accId: string) => {
      // 1. Reset all to non-primary for this project
      await (supabase as any)
        .from("project_bank_accounts")
        .update({ is_primary: false })
        .eq("project_id", projectId);

      // 2. Set chosen as primary
      const { error } = await (supabase as any)
        .from("project_bank_accounts")
        .update({ is_primary: true })
        .eq("id", accId);

      if (error) throw error;
    },
    onSuccess: () => {
      toast.success("Primary bank account updated!");
      queryClient.invalidateQueries({ queryKey: ["project-bank-accounts", projectId] });
    },
    onError: (err: any) => toast.error(err.message || "Failed to update primary account"),
  });

  // Delete Account Mutation
  const deleteMutation = useMutation({
    mutationFn: async (accId: string) => {
      const { error } = await (supabase as any)
        .from("project_bank_accounts")
        .delete()
        .eq("id", accId);
      if (error) throw error;
    },
    onSuccess: () => {
      toast.success("Bank account removed.");
      queryClient.invalidateQueries({ queryKey: ["project-bank-accounts", projectId] });
    },
    onError: (err: any) => toast.error(err.message || "Failed to remove account"),
  });

  const handleCopy = (text: string, id: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    toast.success("Account details copied to clipboard!");
    setTimeout(() => setCopiedId(null), 2000);
  };

  const toggleReveal = (id: string) => {
    setRevealedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const maskAccountNo = (accNo: string) => {
    if (!accNo || accNo.length <= 4) return accNo;
    const last4 = accNo.slice(-4);
    const maskedPart = "•".repeat(Math.max(4, accNo.length - 4));
    return `${maskedPart} ${last4}`;
  };

  return (
    <section className="bg-card border rounded-lg p-6 mb-10 shadow-xs">
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-display text-2xl">Project Bank Accounts</h2>
            <Badge variant="outline" className="text-xs font-semibold px-2 py-0.5">
              {bankAccounts.length} {bankAccounts.length === 1 ? "Account" : "Accounts"} Linked
            </Badge>
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Registered banking accounts for {projectName} used in inter-project transfers and financial settlement.
          </p>
        </div>
        {canEdit && (
          <Button
            onClick={() => {
              setEditingAccount(null);
              setDialogOpen(true);
            }}
            className="bg-terracotta text-white hover:bg-terracotta/90"
            size="sm"
          >
            <Plus className="h-4 w-4 mr-1.5" /> Add Bank Account
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className="py-8 text-center text-sm text-muted-foreground">
          Loading project bank accounts...
        </div>
      ) : bankAccounts.length === 0 ? (
        <div className="py-10 px-4 text-center border border-dashed rounded-lg bg-muted/20">
          <Landmark className="h-10 w-10 text-muted-foreground mx-auto mb-3 opacity-60" />
          <p className="text-sm font-medium">No bank accounts linked to {projectName}</p>
          <p className="text-xs text-muted-foreground mt-1 max-w-md mx-auto">
            Add 1, 2, or 3 bank accounts (Escrow, Current, or Collection) so treasury transactions can be routed directly to this project's specific bank accounts.
          </p>
          {canEdit && (
            <Button
              onClick={() => {
                setEditingAccount(null);
                setDialogOpen(true);
              }}
              variant="outline"
              size="sm"
              className="mt-4 border-terracotta/30 text-terracotta hover:bg-terracotta/10"
            >
              <Plus className="h-3.5 w-3.5 mr-1" /> Add First Bank Account
            </Button>
          )}
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {bankAccounts.map((acc: any) => {
            const isRevealed = revealedIds.has(acc.id);
            const badge = TYPE_BADGES[acc.account_type] || TYPE_BADGES.Escrow;

            return (
              <div
                key={acc.id}
                className={`relative rounded-xl p-5 border transition-all ${
                  acc.is_primary
                    ? "bg-gradient-to-br from-terracotta/5 via-card to-card border-terracotta/40 shadow-sm ring-1 ring-terracotta/20"
                    : "bg-card border-border hover:border-muted-foreground/30 shadow-xs"
                }`}
              >
                {/* Header: Bank Name & Type */}
                <div className="flex items-start justify-between gap-2 mb-3">
                  <div className="flex items-center gap-2">
                    <div className="p-2 rounded-lg bg-terracotta/10 text-terracotta shrink-0">
                      <Building2 className="h-5 w-5" />
                    </div>
                    <div>
                      <h4 className="font-semibold text-base leading-tight truncate max-w-[170px]">
                        {acc.bank_name}
                      </h4>
                      <p className="text-xs text-muted-foreground truncate max-w-[170px]">
                        {acc.account_name}
                      </p>
                    </div>
                  </div>
                  <div className="flex flex-col items-end gap-1">
                    <Badge variant="outline" className={`text-[10px] px-2 py-0.5 ${badge.style}`}>
                      {badge.label}
                    </Badge>
                    {acc.is_primary && (
                      <span className="inline-flex items-center gap-1 text-[10px] font-semibold text-amber-600 bg-amber-50 dark:bg-amber-950/40 dark:text-amber-400 px-1.5 py-0.5 rounded border border-amber-200 dark:border-amber-800">
                        <Star className="h-2.5 w-2.5 fill-amber-500 text-amber-500" /> Primary
                      </span>
                    )}
                  </div>
                </div>

                {/* Account Number Section */}
                <div className="mt-4 p-3 rounded-lg bg-muted/40 border border-muted-foreground/10 space-y-1">
                  <span className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider block">
                    Account Number
                  </span>
                  <div className="flex items-center justify-between">
                    <span className="font-mono font-medium text-sm text-foreground tracking-wider">
                      {isRevealed ? acc.account_number : maskAccountNo(acc.account_number)}
                    </span>
                    <div className="flex items-center gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6 text-muted-foreground hover:text-foreground"
                        onClick={() => toggleReveal(acc.id)}
                        title={isRevealed ? "Hide Account Number" : "Show Account Number"}
                      >
                        {isRevealed ? <EyeOff className="h-3.5 w-3.5" /> : <Eye className="h-3.5 w-3.5" />}
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6 text-muted-foreground hover:text-foreground"
                        onClick={() => handleCopy(acc.account_number, acc.id)}
                        title="Copy Account Number"
                      >
                        {copiedId === acc.id ? (
                          <Check className="h-3.5 w-3.5 text-emerald-600" />
                        ) : (
                          <Copy className="h-3.5 w-3.5" />
                        )}
                      </Button>
                    </div>
                  </div>
                </div>

                {/* IFSC & Branch */}
                <div className="mt-3 grid grid-cols-2 gap-2 text-xs">
                  <div>
                    <span className="text-[10px] text-muted-foreground block uppercase">IFSC Code</span>
                    <span className="font-mono font-semibold uppercase tracking-wider text-foreground">
                      {acc.ifsc_code}
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] text-muted-foreground block uppercase">Branch</span>
                    <span className="text-foreground truncate block" title={acc.branch_name || "N/A"}>
                      {acc.branch_name || "Main Branch"}
                    </span>
                  </div>
                </div>

                {/* Card Actions */}
                {canEdit && (
                  <div className="mt-4 pt-3 border-t flex items-center justify-between text-xs">
                    {!acc.is_primary ? (
                      <button
                        onClick={() => setPrimaryMutation.mutate(acc.id)}
                        disabled={setPrimaryMutation.isPending}
                        className="text-muted-foreground hover:text-terracotta font-medium flex items-center gap-1 transition-colors"
                      >
                        <ShieldCheck className="h-3.5 w-3.5" /> Make Primary
                      </button>
                    ) : (
                      <span className="text-emerald-600 dark:text-emerald-400 font-medium flex items-center gap-1">
                        <Check className="h-3.5 w-3.5" /> Primary A/C
                      </span>
                    )}

                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => {
                          setEditingAccount(acc);
                          setDialogOpen(true);
                        }}
                        className="p-1 rounded text-muted-foreground hover:text-foreground hover:bg-muted transition-colors"
                        title="Edit Bank Details"
                      >
                        <Pencil className="h-3.5 w-3.5" />
                      </button>
                      <button
                        onClick={() => {
                          setDeletingAccount(acc);
                          setDeleteConfirmOpen(true);
                        }}
                        className="p-1 rounded text-muted-foreground hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
                        title="Delete Bank Account"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Dialog for Add / Edit */}
      <AddEditBankAccountDialog
        projectId={projectId}
        account={editingAccount}
        open={dialogOpen}
        onOpenChange={setDialogOpen}
      />

      {/* Confirmation Dialog for Delete */}
      <ConfirmDeleteBankAccountDialog
        account={deletingAccount}
        projectName={projectName}
        open={deleteConfirmOpen}
        onOpenChange={setDeleteConfirmOpen}
      />
    </section>
  );
}
