// Bubblymarble — feature screens. Compact mirrors of Compose screens.
// Globals: React, BM* primitives from Components.jsx, BMC palette.

// Plans screen
function PlansScreen({ onGenerate, plans, generating }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16 }}>
      <BMScreenTitle>Plans</BMScreenTitle>
      <BMButton onClick={onGenerate} disabled={generating} style={{ width: '100%' }}>
        {generating ? 'Generating…' : 'Generate plan'}
      </BMButton>
      {plans.length === 0 ? (
        <div style={{ color: BMC.onSurfaceVar, fontFamily: 'Inter, sans-serif', fontSize: 14 }}>
          No plans yet. Generate one to begin.
        </div>
      ) : plans.map(p => (
        <BMCard key={p.id}>
          <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 16, fontWeight: 500, color: BMC.onSurface, letterSpacing: 0.15 }}>{p.name}</div>
          <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar, marginTop: 4 }}>
            {p.exercises} exercises · {p.ai ? 'AI' : 'Template'}
          </div>
          {p.description && (
            <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar, marginTop: 4 }}>
              {p.description}
            </div>
          )}
        </BMCard>
      ))}
    </div>
  );
}

// Workouts screen
function WorkoutsScreen({ templates, onStart }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16 }}>
      <BMScreenTitle>Today's workouts</BMScreenTitle>
      {templates.length === 0 ? (
        <div style={{ color: BMC.onSurfaceVar, fontSize: 14, fontFamily: 'Inter, sans-serif' }}>
          No workouts yet. Generate a plan first.
        </div>
      ) : templates.map(t => (
        <BMCard key={t.id}>
          <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 16, fontWeight: 500, color: BMC.onSurface, letterSpacing: 0.15 }}>{t.name}</div>
          <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar, marginTop: 4 }}>{t.count} exercises</div>
          <BMButton onClick={() => onStart(t.id)} style={{ width: '100%', marginTop: 8 }}>Start</BMButton>
        </BMCard>
      ))}
    </div>
  );
}

// Workout Runner screen
function RunnerScreen({ session, onComplete, onCancel }) {
  const [reps, setReps] = React.useState(String(session.targetReps));
  const [weight, setWeight] = React.useState('');
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 16, padding: 20 }}>
      <BMScreenTitle>{session.name}</BMScreenTitle>
      <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 14, fontWeight: 500, color: BMC.onSurfaceVar, letterSpacing: 0.1 }}>
        Exercise {session.idx + 1} of {session.total}
      </div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 22, fontWeight: 500, color: BMC.onSurface }}>{session.exercise}</div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 16, fontWeight: 500, color: BMC.onSurface, letterSpacing: 0.15 }}>
        Set {session.set} / {session.totalSets}
      </div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 14, color: BMC.onSurfaceVar }}>
        Target: {session.targetReps} reps
      </div>
      <div style={{ display: 'flex', gap: 8 }}>
        <BMTextField label="Reps" value={reps} onChange={e => setReps(e.target.value.replace(/\D/g, '').slice(0, 3))} style={{ flex: 1 }} />
        <BMTextField label="Weight (kg)" value={weight} onChange={e => setWeight(e.target.value.replace(/[^\d.]/g, '').slice(0, 6))} style={{ flex: 1 }} />
      </div>
      <BMButton onClick={onComplete} style={{ width: '100%' }}>Complete set</BMButton>
      <BMButton variant="outlined" onClick={onCancel} style={{ width: '100%' }}>Cancel workout</BMButton>
    </div>
  );
}

// Meals screen
function MealsScreen({ date, totals, meals, onPrev, onNext, onToday, onAdd, onEdit }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16 }}>
      {/* Day header */}
      <div style={{ display: 'flex', alignItems: 'center' }}>
        <button onClick={onPrev} style={{ background: 'transparent', border: 'none', color: BMC.onSurface, cursor: 'pointer', padding: 8 }}>
          <span className="material-icons">chevron_left</span>
        </button>
        <div style={{ flex: 1, fontFamily: 'Inter, sans-serif', fontSize: 22, fontWeight: 500, color: BMC.onSurface }}>{date}</div>
        <BMButton variant="text" onClick={onToday}>Today</BMButton>
        <button onClick={onNext} style={{ background: 'transparent', border: 'none', color: BMC.onSurface, cursor: 'pointer', padding: 8 }}>
          <span className="material-icons">chevron_right</span>
        </button>
      </div>
      {/* Totals */}
      <div style={{ display: 'flex', gap: 8 }}>
        <BMStatCard label="kcal" value={totals.kcal} />
        <BMStatCard label="Protein" value={`${totals.p}g`} />
        <BMStatCard label="Carbs" value={`${totals.c}g`} />
        <BMStatCard label="Fat" value={`${totals.f}g`} />
      </div>
      {meals.length === 0 ? (
        <div style={{ color: BMC.onSurfaceVar, fontSize: 14, fontFamily: 'Inter, sans-serif' }}>No meals logged yet.</div>
      ) : meals.map(m => (
        <BMCard key={m.id}>
          <div style={{ display: 'flex', alignItems: 'baseline' }}>
            <div style={{ flex: 1, fontFamily: 'Inter, sans-serif', fontSize: 16, fontWeight: 500, color: BMC.onSurface, letterSpacing: 0.15 }}>{m.name}</div>
            <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, fontWeight: 500, color: BMC.onSurfaceVar, letterSpacing: 0.5 }}>{m.type}</div>
          </div>
          <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar, marginTop: 4 }}>
            {m.kcal} kcal · P {m.p}g · C {m.c}g · F {m.f}g
          </div>
          <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar, marginTop: 4 }}>
            {m.items}
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 4 }}>
            <BMButton variant="text" onClick={() => onEdit(m.id)}>Edit</BMButton>
          </div>
        </BMCard>
      ))}
      {/* Floating Action Button */}
      <button onClick={onAdd} style={{
        position: 'absolute', bottom: 88, right: 16,
        height: 56, padding: '0 20px', display: 'flex', alignItems: 'center', gap: 8,
        background: BMC.primary, color: BMC.onPrimary,
        border: 'none', borderRadius: 16, cursor: 'pointer',
        fontFamily: 'Inter, sans-serif', fontSize: 14, fontWeight: 500, letterSpacing: 0.1,
        boxShadow: '0 6px 16px rgba(0,0,0,0.4)',
      }}>
        <span className="material-icons" style={{ fontSize: 20 }}>add</span>
        Log meal
      </button>
    </div>
  );
}

// Measurements / Body screen
function BodyScreen({ latest, weights }) {
  const min = Math.min(...weights);
  const max = Math.max(...weights);
  const range = Math.max(max - min, 0.01);
  const w = 320;
  const h = 80;
  const pts = weights.map((v, i) => {
    const x = (i / (weights.length - 1)) * w;
    const y = h - ((v - min) / range) * h;
    return `${x},${y}`;
  }).join(' ');
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16 }}>
      <BMScreenTitle>Measurements</BMScreenTitle>
      <BMCard>
        <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 16, fontWeight: 500, color: BMC.onSurface, letterSpacing: 0.15 }}>Health Connect</div>
        <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar, marginTop: 8 }}>
          Pulls weight, body fat, lean mass and BMR from any source publishing to Health Connect.
        </div>
        <BMButton style={{ width: '100%', marginTop: 12 }}>Sync last 30 days</BMButton>
      </BMCard>
      <BMSectionHeader>Body composition</BMSectionHeader>
      <div style={{ display: 'flex', gap: 8 }}>
        <BMStatCard label="Weight" value={`${latest.weight} kg`} />
        <BMStatCard label="Body fat" value={`${latest.bf} %`} />
      </div>
      <div style={{ display: 'flex', gap: 8 }}>
        <BMStatCard label="Lean mass" value={`${latest.lean} kg`} />
        <BMStatCard label="BMR" value={`${latest.bmr}`} />
      </div>
      <BMSectionHeader>Weight trend (last {weights.length} entries)</BMSectionHeader>
      <BMCard>
        <div style={{ display: 'flex' }}>
          <div style={{ flex: 1, fontFamily: 'Inter, sans-serif', fontSize: 16, fontWeight: 500, color: BMC.onSurface, letterSpacing: 0.15 }}>
            Latest {weights[weights.length - 1].toFixed(1)} kg
          </div>
          <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar }}>
            Range {min.toFixed(1)}–{max.toFixed(1)} kg
          </div>
        </div>
        <svg width="100%" height={h} viewBox={`0 0 ${w} ${h}`} style={{ marginTop: 8 }}>
          <polyline fill="none" stroke="#0EA5E9" strokeWidth="3" points={pts} />
        </svg>
      </BMCard>
      <BMSectionHeader>Tape measurements</BMSectionHeader>
      {[
        { label: 'Chest', latest: '102.5 cm · 12 Mar' },
        { label: 'Waist', latest: '84.0 cm · 12 Mar' },
        { label: 'Biceps left', latest: 'No entries yet' },
      ].map(r => (
        <BMCard key={r.label}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 16, fontWeight: 500, color: BMC.onSurface }}>{r.label}</div>
              <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar, marginTop: 2 }}>{r.latest}</div>
            </div>
            <BMButton variant="text">Log</BMButton>
          </div>
        </BMCard>
      ))}
    </div>
  );
}

// Stats screen
function StatsScreen({ stats }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16 }}>
      <BMScreenTitle>Stats</BMScreenTitle>
      <div style={{ display: 'flex', gap: 12 }}>
        <BMStatCard label="Current streak" value={`${stats.streak} d`} />
        <BMStatCard label="Longest" value={`${stats.longest} d`} />
      </div>
      <div style={{ display: 'flex', gap: 12 }}>
        <BMStatCard label="Sessions" value={stats.sessions} />
        <BMStatCard label="Last 7 d" value={stats.last7d} />
      </div>
      <div style={{ display: 'flex', gap: 12 }}>
        <BMStatCard label="Total sets" value={stats.totalSets} />
        <BMStatCard label="Volume (kg)" value={stats.volume} />
      </div>
    </div>
  );
}

// Settings screen
function SettingsScreen() {
  const [key, setKey] = React.useState('');
  const [hour, setHour] = React.useState('18');
  const [minute, setMinute] = React.useState('00');
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16 }}>
      <BMScreenTitle>Settings</BMScreenTitle>
      <BMSectionHeader>Gemini API key</BMSectionHeader>
      <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12, color: BMC.onSurfaceVar }}>Not set</div>
      <BMTextField label="Paste new key" value={key} onChange={e => setKey(e.target.value)} type="password" />
      <div style={{ display: 'flex', gap: 8 }}>
        <BMButton style={{ flex: 1 }} disabled={!key}>Save key</BMButton>
        <BMButton variant="outlined" style={{ flex: 1 }} onClick={() => setKey('')}>Clear</BMButton>
      </div>
      <BMSectionHeader>Daily reminder</BMSectionHeader>
      <div style={{ display: 'flex', gap: 8 }}>
        <BMTextField label="Hour (0-23)" value={hour} onChange={e => setHour(e.target.value.replace(/\D/g, '').slice(0, 2))} style={{ flex: 1 }} />
        <BMTextField label="Minute" value={minute} onChange={e => setMinute(e.target.value.replace(/\D/g, '').slice(0, 2))} style={{ flex: 1 }} />
      </div>
      <div style={{ display: 'flex', gap: 8 }}>
        <BMButton style={{ flex: 1 }}>Schedule</BMButton>
        <BMButton variant="outlined" style={{ flex: 1 }}>Cancel reminders</BMButton>
      </div>
    </div>
  );
}

// Onboarding screen
function OnboardingScreen({ onDone }) {
  const [name, setName] = React.useState('');
  const [goal, setGoal] = React.useState('Lose weight');
  const [exp, setExp] = React.useState('Beginner');
  const [equip, setEquip] = React.useState('Home gym');
  const [target, setTarget] = React.useState(4);
  const goals = ['Lose weight', 'Build muscle', 'Maintain', 'General health'];
  const exps = ['Beginner', 'Intermediate', 'Advanced'];
  const equips = ['None', 'Home gym', 'Full gym'];
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 24, background: BMC.bg, height: '100%', overflow: 'auto' }}>
      <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 24, fontWeight: 400, color: BMC.onSurface }}>
        Welcome to Bubblymarble
      </div>
      <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 14, color: BMC.onSurfaceVar }}>
        Tell us a bit about yourself so we can build a plan.
      </div>
      <div style={{ marginTop: 8 }}>
        <BMTextField label="Your name" value={name} onChange={e => setName(e.target.value)} />
      </div>
      <BMSectionHeader>Goal</BMSectionHeader>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {goals.map(g => <BMChip key={g} selected={goal === g} onClick={() => setGoal(g)}>{g}</BMChip>)}
      </div>
      <BMSectionHeader>Experience</BMSectionHeader>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {exps.map(e => <BMChip key={e} selected={exp === e} onClick={() => setExp(e)}>{e}</BMChip>)}
      </div>
      <BMSectionHeader>Equipment access</BMSectionHeader>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        {equips.map(e => <BMChip key={e} selected={equip === e} onClick={() => setEquip(e)}>{e}</BMChip>)}
      </div>
      <BMSectionHeader>Sessions per week: {target}</BMSectionHeader>
      <input type="range" min="1" max="7" value={target} onChange={e => setTarget(+e.target.value)}
        style={{ accentColor: BMC.primary }} />
      <div style={{ marginTop: 16 }}>
        <BMButton onClick={onDone} disabled={!name.trim()} style={{ width: '100%' }}>Get started</BMButton>
      </div>
    </div>
  );
}

Object.assign(window, {
  PlansScreen, WorkoutsScreen, RunnerScreen, MealsScreen,
  BodyScreen, StatsScreen, SettingsScreen, OnboardingScreen,
});
