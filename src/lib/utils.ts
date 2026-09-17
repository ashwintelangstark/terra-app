import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/** Formats a numeric value or string into Indian Rupees format with commas (e.g. 2500000 -> "25,00,000") */
export function formatIndianNumber(value: number | string | null | undefined): string {
  if (value === null || value === undefined || value === "") return "";
  const str = String(value).replace(/,/g, "").trim();
  if (!str) return "";
  const parts = str.split(".");
  let intPart = parts[0].replace(/\D/g, "");
  const decPart = parts.length > 1 ? "." + parts[1].replace(/\D/g, "").slice(0, 2) : "";

  if (!intPart) return decPart ? "0" + decPart : "";

  let lastThree = intPart.substring(intPart.length - 3);
  const otherNumbers = intPart.substring(0, intPart.length - 3);
  if (otherNumbers !== "") {
    lastThree = "," + lastThree;
  }
  const formattedInt = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + lastThree;
  return formattedInt + decPart;
}

/** Parses a formatted Indian Rupees string back into a numeric value */
export function parseIndianNumber(value: number | string | null | undefined): number {
  if (value === null || value === undefined || value === "") return 0;
  const cleaned = String(value).replace(/,/g, "").trim();
  const parsed = parseFloat(cleaned);
  return isNaN(parsed) ? 0 : parsed;
}
