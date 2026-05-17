import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function Login() {
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const login = async () => {
    setLoading(true); setError('')
    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, password })
      })
      const data = await res.json()
      if (data.success) {
        localStorage.setItem('user', JSON.stringify(data))
        navigate('/' + data.role)
      } else {
        setError(data.message)
      }
    } catch {
      setError('Cannot connect to server. Is the backend running?')
    }
    setLoading(false)
  }

  const fill = (p: string, pw: string) => { setPhone(p); setPassword(pw) }

  return (
    <div style={s.page}>
      <div style={s.card}>
        <h1 style={s.logo}>🚗 SavaariPK</h1>
        <h2 style={s.heading}>Sign In</h2>
        <input style={s.input} placeholder="Phone (e.g. 03001234567)"
          value={phone} onChange={e => setPhone(e.target.value)} />
        <input style={s.input} type="password" placeholder="Password"
          value={password} onChange={e => setPassword(e.target.value)} />
        {error && <p style={s.error}>{error}</p>}
        <button style={s.btn} onClick={login} disabled={loading}>
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
        <div style={s.demoBox}>
          <p style={s.demoTitle}>Demo Accounts (click to fill)</p>
          <div style={s.demoRow}>
            <button style={s.demoBtn} onClick={() => fill('03000000000', 'admin123')}>Admin</button>
            <button style={s.demoBtn} onClick={() => fill('03001234567', 'admin123')}>Passenger</button>
            <button style={s.demoBtn} onClick={() => fill('03009876543', 'admin123')}>Driver</button>
          </div>
        </div>
      </div>
    </div>
  )
}

const s: any = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f0f4f8' },
  card: { background: 'white', padding: '40px', borderRadius: '16px', width: '380px', boxShadow: '0 4px 24px rgba(0,0,0,0.1)' },
  logo: { textAlign: 'center', color: '#1a56db', marginBottom: '4px', fontSize: '28px' },
  heading: { textAlign: 'center', color: '#374151', marginBottom: '24px', fontWeight: 500 },
  input: { width: '100%', padding: '12px', marginBottom: '12px', border: '1px solid #d1d5db', borderRadius: '8px', fontSize: '15px', display: 'block' },
  btn: { width: '100%', padding: '13px', background: '#1a56db', color: 'white', border: 'none', borderRadius: '8px', fontSize: '16px', cursor: 'pointer', marginBottom: '16px' },
  error: { color: '#ef4444', marginBottom: '12px', textAlign: 'center', fontSize: '14px' },
  demoBox: { background: '#f9fafb', borderRadius: '8px', padding: '12px', border: '1px solid #e5e7eb' },
  demoTitle: { fontSize: '12px', color: '#6b7280', marginBottom: '8px', textAlign: 'center' },
  demoRow: { display: 'flex', gap: '8px', justifyContent: 'center' },
  demoBtn: { padding: '7px 14px', border: '1px solid #d1d5db', borderRadius: '6px', cursor: 'pointer', background: 'white', fontSize: '13px' },
}