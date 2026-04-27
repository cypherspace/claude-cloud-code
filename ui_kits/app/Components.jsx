// Bubblymarble UI kit primitives — web mirrors of M3 Compose patterns
// Loaded after React + Babel. Uses tokens from /colors_and_type.css.

const BMC = {
  primary: '#38BDF8',
  onPrimary: '#002B3D',
  secondary: '#2DD4BF',
  onSecondary: '#003733',
  bg: '#0B1220',
  surface: '#111827',
  surface1: '#131C2E',
  surface2: '#182238',
  surface3: '#1D2942',
  outline: '#334155',
  outlineVar: '#1F2937',
  onSurface: '#E2E8F0',
  onSurfaceVar: '#94A3B8',
  error: '#F87171',
};

// ────────────────────────────────────────────────────────────
// Buttons
// ────────────────────────────────────────────────────────────
function BMButton({ children, onClick, disabled, variant = 'filled', tone = 'primary', style }) {
  const tones = {
    primary:   { bg: BMC.primary,   fg: BMC.onPrimary },
    secondary: { bg: BMC.secondary, fg: BMC.onSecondary },
  };
  const t = tones[tone] || tones.primary;
  const filled = {
    background: disabled ? '#1F2937' : t.bg,
    color: disabled ? '#475569' : t.fg,
    border: 'none',
  };
  const outlined = {
    background: 'transparent',
    color: disabled ? '#475569' : BMC.primary,
    border: `1px solid ${disabled ? BMC.outlineVar : BMC.outline}`,
  };
  const text = {
    background: 'transparent',
    color: disabled ? '#475569' : BMC.primary,
    border: 'none',
  };
  const variants = { filled, outlined, text };
  return (
    <button onClick={disabled ? undefined : onClick} disabled={disabled} style={{
      ...variants[variant],
      borderRadius: 9999, padding: '10px 24px',
      fontFamily: 'Inter, system-ui, sans-serif',
      fontSize: 14, fontWeight: 500, letterSpacing: 0.1,
      cursor: disabled ? 'not-allowed' : 'pointer',
      transition: 'filter 150ms cubic-bezier(0.2,0,0,1)',
      ...style,
    }}>{children}</button>
  );
}

// ────────────────────────────────────────────────────────────
// Card
// ────────────────────────────────────────────────────────────
function BMCard({ children, elev = 1, style, onClick }) {
  const bg = elev === 2 ? BMC.surface2 : elev === 3 ? BMC.surface3 : BMC.surface;
  return (
    <div onClick={onClick} style={{
      background: bg, borderRadius: 12, padding: 16,
      cursor: onClick ? 'pointer' : 'default',
      transition: 'background 150ms cubic-bezier(0.2,0,0,1)',
      ...style,
    }}>{children}</div>
  );
}

// ────────────────────────────────────────────────────────────
// Stat card (label / value)
// ────────────────────────────────────────────────────────────
function BMStatCard({ label, value, style }) {
  return (
    <div style={{
      background: BMC.surface, borderRadius: 12, padding: 16, flex: 1,
      ...style,
    }}>
      <div style={{
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: 12, fontWeight: 500, letterSpacing: 0.5,
        color: BMC.onSurfaceVar, textTransform: 'uppercase',
      }}>{label}</div>
      <div style={{
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: 28, fontWeight: 400, marginTop: 4,
        color: BMC.onSurface, fontVariantNumeric: 'tabular-nums',
      }}>{value}</div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Filter chip
// ────────────────────────────────────────────────────────────
function BMChip({ children, selected, onClick }) {
  return (
    <button onClick={onClick} style={{
      padding: '6px 12px',
      borderRadius: 8,
      border: `1px solid ${selected ? BMC.primary : BMC.outline}`,
      background: selected ? 'rgba(56,189,248,0.18)' : 'transparent',
      color: selected ? BMC.primary : BMC.onSurface,
      fontFamily: 'Inter, system-ui, sans-serif',
      fontSize: 14, fontWeight: 500, letterSpacing: 0.1,
      cursor: 'pointer',
      transition: 'all 150ms cubic-bezier(0.2,0,0,1)',
    }}>{children}</button>
  );
}

// ────────────────────────────────────────────────────────────
// Section header (matches Compose SectionHeader)
// ────────────────────────────────────────────────────────────
function BMSectionHeader({ children }) {
  return (
    <div style={{
      fontFamily: 'Inter, system-ui, sans-serif',
      fontSize: 16, fontWeight: 500, letterSpacing: 0.15,
      padding: '8px 0',
      color: BMC.onSurface,
    }}>{children}</div>
  );
}

// ────────────────────────────────────────────────────────────
// Text field (OutlinedTextField mirror)
// ────────────────────────────────────────────────────────────
function BMTextField({ label, value, onChange, placeholder, type = 'text', style }) {
  const [focused, setFocused] = React.useState(false);
  const has = focused || value;
  return (
    <div style={{ position: 'relative', ...style }}>
      <input
        type={type}
        value={value}
        onChange={onChange}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        placeholder={focused ? placeholder : ''}
        style={{
          width: '100%', boxSizing: 'border-box',
          background: 'transparent',
          color: BMC.onSurface,
          border: `${focused ? 2 : 1}px solid ${focused ? BMC.primary : BMC.outline}`,
          borderRadius: 4,
          padding: focused ? '11px 15px' : '12px 16px',
          fontFamily: 'Inter, system-ui, sans-serif',
          fontSize: 16, lineHeight: '24px',
          outline: 'none',
        }}
      />
      <span style={{
        position: 'absolute',
        left: has ? 12 : 16,
        top: has ? -8 : 12,
        fontSize: has ? 12 : 16,
        color: focused ? BMC.primary : BMC.onSurfaceVar,
        background: BMC.bg,
        padding: has ? '0 4px' : 0,
        fontFamily: 'Inter, system-ui, sans-serif',
        pointerEvents: 'none',
        transition: 'all 150ms cubic-bezier(0.2,0,0,1)',
      }}>{label}</span>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Bottom navigation bar — 6 tabs (Plans, Workouts, Meals, Body, Stats, Settings)
// ────────────────────────────────────────────────────────────
function BMNavBar({ tabs, current, onSelect }) {
  return (
    <div style={{
      display: 'flex',
      background: BMC.surface1,
      padding: '12px 4px 8px',
      borderTop: `1px solid ${BMC.outlineVar}`,
    }}>
      {tabs.map(t => {
        const sel = t.id === current;
        return (
          <button key={t.id} onClick={() => onSelect(t.id)} style={{
            flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
            padding: '4px 0', background: 'transparent', border: 'none',
            color: sel ? BMC.onSurface : BMC.onSurfaceVar,
            cursor: 'pointer',
          }}>
            <div style={{
              width: 56, height: 28, borderRadius: 14,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              background: sel ? 'rgba(56,189,248,0.22)' : 'transparent',
              transition: 'background 150ms cubic-bezier(0.2,0,0,1)',
            }}>
              <span className="material-icons" style={{
                fontSize: 22, color: sel ? BMC.primary : BMC.onSurfaceVar,
              }}>{t.icon}</span>
            </div>
            <span style={{
              fontFamily: 'Inter, system-ui, sans-serif',
              fontSize: 11, fontWeight: 500, letterSpacing: 0.5,
            }}>{t.label}</span>
          </button>
        );
      })}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Screen header — title in headline-small
// ────────────────────────────────────────────────────────────
function BMScreenTitle({ children }) {
  return (
    <div style={{
      fontFamily: 'Inter, system-ui, sans-serif',
      fontSize: 24, fontWeight: 400, lineHeight: '32px',
      color: BMC.onSurface,
      padding: '4px 0',
    }}>{children}</div>
  );
}

Object.assign(window, {
  BMC, BMButton, BMCard, BMStatCard, BMChip, BMSectionHeader,
  BMTextField, BMNavBar, BMScreenTitle,
});
