import { BriefcaseBusiness } from "lucide-react";
import { Link } from "react-router-dom";

type AuthShellProps = {
  title: string;
  subtitle: string;
  footerText: string;
  footerLinkText: string;
  footerLinkTo: string;
  children: React.ReactNode;
};

export function AuthShell({
  title,
  subtitle,
  footerText,
  footerLinkText,
  footerLinkTo,
  children,
}: AuthShellProps) {
  return (
    <main className="auth-page">
      <section className="auth-panel" aria-labelledby="auth-title">
        <div className="brand-mark" aria-hidden="true">
          <BriefcaseBusiness size={24} />
        </div>
        <h1 id="auth-title">{title}</h1>
        <p>{subtitle}</p>
        {children}
        <footer className="auth-footer">
          {footerText} <Link to={footerLinkTo}>{footerLinkText}</Link>
        </footer>
      </section>
    </main>
  );
}
