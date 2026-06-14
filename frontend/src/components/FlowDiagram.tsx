import { Fragment } from 'react';

export interface FlowStep {
  label: string;
  done: boolean;
}

/**
 * Horizontal flow diagram of the FERKO exam-scheduling workflow
 * (Dohvati studente → Uredi dvorane → Definiranje rasporeda → Dodjela asistenata → Objavi).
 * The first not-done step is highlighted as the current step.
 */
export function FlowDiagram({ steps }: { steps: FlowStep[] }) {
  const activeIndex = steps.findIndex((s) => !s.done);
  return (
    <div className="flow">
      {steps.map((step, i) => (
        <Fragment key={step.label}>
          <div
            className={`flow-node ${step.done ? 'done' : i === activeIndex ? 'active' : 'pending'}`}
          >
            <span className="flow-badge">{step.done ? '✓' : i + 1}</span>
            <span className="flow-label">{step.label}</span>
          </div>
          {i < steps.length - 1 && <span className="flow-arrow">→</span>}
        </Fragment>
      ))}
    </div>
  );
}
