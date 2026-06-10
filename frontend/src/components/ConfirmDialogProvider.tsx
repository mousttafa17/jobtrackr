import { createContext, useCallback, useContext, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { AlertTriangle } from "lucide-react";
import { Button } from "./Button";

type ConfirmOptions = {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  dangerous?: boolean;
};

type ConfirmState = ConfirmOptions & {
  onConfirm: () => void;
  onCancel: () => void;
};

type ConfirmDialogContextValue = {
  confirm: (options: ConfirmOptions) => Promise<boolean>;
};

const ConfirmDialogContext = createContext<ConfirmDialogContextValue | undefined>(undefined);

export function ConfirmDialogProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<ConfirmState | null>(null);

  const confirm = useCallback((options: ConfirmOptions) => {
    return new Promise<boolean>((resolve) => {
      setState({
        ...options,
        onConfirm: () => {
          setState(null);
          resolve(true);
        },
        onCancel: () => {
          setState(null);
          resolve(false);
        },
      });
    });
  }, []);

  const value = useMemo(() => ({ confirm }), [confirm]);

  return (
    <ConfirmDialogContext.Provider value={value}>
      {children}
      {state && (
        <div className="modal-backdrop" role="presentation">
          <section
            className="confirm-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="confirm-dialog-title"
          >
            <div className={`confirm-icon ${state.dangerous ? "danger" : ""}`} aria-hidden="true">
              <AlertTriangle size={24} />
            </div>
            <h2 id="confirm-dialog-title">{state.title}</h2>
            <p>{state.message}</p>
            <div className="modal-actions">
              <Button type="button" variant="ghost" onClick={state.onCancel}>
                {state.cancelLabel ?? "Cancel"}
              </Button>
              <Button type="button" variant={state.dangerous ? "danger" : "primary"} onClick={state.onConfirm}>
                {state.confirmLabel ?? "Confirm"}
              </Button>
            </div>
          </section>
        </div>
      )}
    </ConfirmDialogContext.Provider>
  );
}

export function useConfirmDialog() {
  const context = useContext(ConfirmDialogContext);
  if (!context) {
    throw new Error("useConfirmDialog must be used within ConfirmDialogProvider");
  }

  return context;
}
