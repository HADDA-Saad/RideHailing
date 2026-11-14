export function Spinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="spinner" role="status" aria-live="polite">
      <div className="spinner__dot" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}
