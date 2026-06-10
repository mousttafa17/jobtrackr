export function LoadingState({ label = "Loading..." }: { label?: string }) {
  return (
    <div className="loading-state" role="status" aria-label={label}>
      <span className="loading-spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}
