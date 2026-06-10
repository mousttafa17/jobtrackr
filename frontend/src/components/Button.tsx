import type { ButtonHTMLAttributes } from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "ghost" | "danger";
};

export function Button({ className = "", variant = "primary", ...props }: ButtonProps) {
  return <button className={`button button-${variant} ${className}`} {...props} />;
}
