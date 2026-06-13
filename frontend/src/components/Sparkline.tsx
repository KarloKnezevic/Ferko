interface SparklineProps {
  values: number[];
  width?: number;
  height?: number;
  color?: string;
}

/** A tiny inline SVG convergence curve for an optimizer's penalty history. */
export function Sparkline({ values, width = 160, height = 36, color = '#0a8a8a' }: SparklineProps) {
  if (values.length < 2) {
    return <span className="muted">—</span>;
  }
  const max = Math.max(...values);
  const min = Math.min(...values);
  const span = max - min || 1;
  const stepX = width / (values.length - 1);
  const points = values
    .map((v, i) => {
      const x = i * stepX;
      const y = height - ((v - min) / span) * (height - 4) - 2;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(' ');
  return (
    <svg width={width} height={height} className="sparkline" role="img" aria-label="krivulja konvergencije">
      <polyline points={points} fill="none" stroke={color} strokeWidth="1.5" />
    </svg>
  );
}
