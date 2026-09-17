/**
 * Utility for phone number formatting, 10-digit manual entry, and +91 country code handling.
 */

/**
 * Sanitizes phone input: extracts the 10-digit mobile number.
 * Automatically strips country prefixes like "+91", "91", or leading "0" if pasted,
 * giving the user complete freedom to type or paste only the 10-digit number.
 */
export function sanitizePhoneInput(input: string): string {
  if (!input) return "";
  let digits = input.replace(/\D/g, "");
  
  // If user pasted with country code (+91 / 91) having 12 digits
  if (digits.length === 12 && digits.startsWith("91")) {
    digits = digits.slice(2);
  }
  // If user entered leading 0 with 11 digits
  else if (digits.length === 11 && digits.startsWith("0")) {
    digits = digits.slice(1);
  }

  return digits.slice(0, 10);
}

/**
 * Formats a 10-digit mobile number with the '+91' country code prefix for database storage, WhatsApp, and SMS.
 */
export function formatPhoneWithCountryCode(input: string): string {
  const digits = sanitizePhoneInput(input);
  if (!digits) return "";
  return `+91${digits}`;
}

/**
 * Checks if phone number is a valid 10-digit Indian mobile number.
 * Validates strictly that length is 10 digits and starts with 6, 7, 8, or 9.
 */
export function isValid10DigitPhone(input: string): boolean {
  const digits = sanitizePhoneInput(input);
  return /^[6-9]\d{9}$/.test(digits);
}

/**
 * Returns a user-friendly error message string if the phone number is invalid, or null if valid.
 */
export function getPhoneValidationError(input: string): string | null {
  const digits = sanitizePhoneInput(input);
  if (!digits) {
    return "Phone number is required.";
  }
  if (digits.length < 10) {
    return `Phone number must be exactly 10 digits (${digits.length}/10 entered).`;
  }
  if (!/^[6-9]/.test(digits)) {
    return "Phone number must be a valid 10-digit mobile number starting with 6, 7, 8, or 9.";
  }
  return null;
}
