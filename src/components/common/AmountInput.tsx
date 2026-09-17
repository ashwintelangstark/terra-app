import React, { forwardRef, useEffect, useRef, useState } from "react";
import { Input } from "@/components/ui/input";
import { cn, formatIndianNumber, parseIndianNumber } from "@/lib/utils";

export interface AmountInputProps
  extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "onChange" | "value"> {
  value?: number | string | null;
  onChangeValue?: (numericValue: number, formattedValue: string) => void;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  showCurrencySymbol?: boolean;
}

export const AmountInput = forwardRef<HTMLInputElement, AmountInputProps>(
  (
    {
      value,
      onChangeValue,
      onChange,
      className,
      showCurrencySymbol = true,
      placeholder = "0",
      ...props
    },
    ref,
  ) => {
    const inputRef = useRef<HTMLInputElement | null>(null);

    // Keep internal string state for seamless typing experience
    const [displayValue, setDisplayValue] = useState<string>(() => formatIndianNumber(value));

    // Sync state when incoming prop changes externally
    useEffect(() => {
      const formatted = formatIndianNumber(value);
      const currentNumeric = parseIndianNumber(displayValue);
      const incomingNumeric = parseIndianNumber(value);
      
      if (currentNumeric !== incomingNumeric || (value === "" && displayValue !== "")) {
        setDisplayValue(formatted);
      }
    }, [value]);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const rawInput = e.target.value;
      const cursorPosition = e.target.selectionStart ?? rawInput.length;
      
      // Calculate how many non-comma characters were before the cursor
      const nonCommasBeforeCursor = rawInput.slice(0, cursorPosition).replace(/,/g, "").length;

      const formatted = formatIndianNumber(rawInput);
      const numericVal = parseIndianNumber(formatted);

      setDisplayValue(formatted);

      if (onChangeValue) {
        onChangeValue(numericVal, formatted);
      }

      if (onChange) {
        // Pass event with formatted value
        const targetObj = e.target;
        targetObj.value = formatted;
        onChange(e);
      }

      // Preserve cursor position relative to digits
      requestAnimationFrame(() => {
        const el = inputRef.current;
        if (!el) return;
        let newPos = 0;
        let count = 0;
        for (let i = 0; i < formatted.length; i++) {
          if (formatted[i] !== ",") {
            count++;
          }
          if (count >= nonCommasBeforeCursor) {
            newPos = i + 1;
            break;
          }
        }
        if (newPos === 0 && formatted.length > 0) newPos = formatted.length;
        try {
          el.setSelectionRange(newPos, newPos);
        } catch {
          // Ignore if input type doesn't support selection range
        }
      });
    };

    return (
      <div className="relative flex items-center w-full">
        {showCurrencySymbol && (
          <span className="absolute left-3 text-muted-foreground flex items-center pointer-events-none text-xs font-bold select-none">
            ₹
          </span>
        )}
        <Input
          {...props}
          ref={(node) => {
            inputRef.current = node;
            if (typeof ref === "function") ref(node);
            else if (ref) (ref as React.MutableRefObject<HTMLInputElement | null>).current = node;
          }}
          type="text"
          inputMode="numeric"
          placeholder={placeholder}
          value={displayValue}
          onChange={handleInputChange}
          className={cn(showCurrencySymbol && "pl-7", className)}
        />
      </div>
    );
  },
);

AmountInput.displayName = "AmountInput";
