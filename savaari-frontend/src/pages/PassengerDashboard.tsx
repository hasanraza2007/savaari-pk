import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function PassengerDashboard() {
  const [wallet, setWallet] = useState<any>(null)
  const [history, setHistory] = useState<any[]>([])
  const [amount, setAmount] = useState('')
  const [msg, setMsg] = useState('')
  const [tab, setTab] = useState('wallet')
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  const loadWallet = () => fetch(`/api/passenger/wallet/${user.userId}`).then(r => r.json()).then(setWallet)

  useEffect(() => {
    if (!user.success) { navigate('/login'); return }
    loadWallet()
    fetch(`/api/passenger/history/${user.userId}`).then(r => r.json()).then(setHistory)
  }, [])

  const topup = async () => {
    const val = parseFloat(amount)
    if (!val || val <= 0) return
    const res = await fetch('/api/passenger/wallet/topup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: user.userId, amount: val })
    })
    const data = await res.json()
    if (data.success) { setMsg(`✅ Topped up! New balance: PKR ${data.newBalance}`); setAmount(''); loadWallet() }
  }

  const logout = () => { localStorage.clear(); navigate('/login') }

  return (
    <div style={s.page}>
      <div style={s.sidebar}>
        <h2 style={s.logo}>🚗 SavaariPK</h2>
        <p style={s.roleText}>Passenger</p>
        <p style={s.nameText}>{user.name}</p>
        <nav style={{ flex: 1 }}>
          {[['wallet','💳 Wallet'],['history','📋 Ride History']].map(([key, label]) => (
            <button key={key} style={tab === key ? s.navActive : s.nav} onClick={() => setTab(key)}>{label}</button>
          ))}
        </nav>
        <button style={s.logout} onClick={logout}>Logout</button>
      </div>

      <div style={s.content}>
        {tab === 'wallet' && <>
          <h2 style={s.pageTitle}>My Wallet</h2>
          <div style={s.balanceCard}>
            <p style={{ opacity: 0.8, marginBottom: '8px' }}>Current Balance</p>
            <h1 style={{ fontSize: '42px', margin: 0 }}>PKR {wallet?.balance?.toFixed(2) ?? '...'}</h1>
          </div>
          <div style={s.card}>
            <h3 style={{ marginBottom: '16px' }}>Top Up</h3>
            <input style={s.input} type="number" placeholder="Enter amount (PKR)"
              value={amount} onChange={e => setAmount(e.target.value)} />
            <button style={s.btn} onClick={topup}>Top Up Now</button>
            {msg && <p style={{ color: '#10b981', marginTop: '12px' }}>{msg}</p>}
          </div>
          <div style={s.card}>
            <h3 style={{ marginBottom: '16px' }}>Recent Transactions</h3>
            {wallet?.transactions?.length === 0
              ? <p style={{ color: '#6b7280' }}>No transactions yet.</p>
              : wallet?.transactions?.slice(0, 6).map((t: any, i: number) => (
                  <div key={i} style={s.txRow}>
                    <span style={{ color: '#374151' }}>{t.Type} — {t.Description}</span>
                    <span style={{ color: t.Type === 'TOPUP' ? '#10b981' : '#ef4444', fontWeight: 'bold' }}>
                      {t.Type === 'TOPUP' ? '+' : '-'}PKR {t.Amount}
                    </span>
                  </div>
                ))
            }
          </div>
        </>}

        {tab === 'history' && <>
          <h2 style={s.pageTitle}>Ride History</h2>
          {history.length === 0
            ? <p>No rides yet.</p>
            : <table style={s.table}>
                <thead><tr><th>From</th><th>To</th><th>Type</th><th>Vehicle</th><th>Fare</th><th>Status</th></tr></thead>
                <tbody>
                  {history.map((r: any) => (
                    <tr key={r.BookingID}>
                      <td>{r.OriginCity}</td><td>{r.DestinationCity}</td>
                      <td>{r.RideType}</td><td>{r.VehicleType}</td>
                      <td>PKR {r.FareShare}</td><td>{r.Status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
          }
        </>}
      </div>
    </div>
  )
}

const s: any = {
  page: { display: 'flex', minHeight: '100vh', fontFamily: 'sans-serif' },
  sidebar: { width: '230px', background: '#1e3a5f', color: 'white', padding: '28px 16px', display: 'flex', flexDirection: 'column' },
  logo: { color: 'white', marginBottom: '4px', fontSize: '20px' },
  roleText: { color: '#93c5fd', fontSize: '13px', marginBottom: '4px' },
  nameText: { color: 'white', fontWeight: 'bold', marginBottom: '24px' },
  nav: { display: 'block', width: '100%', padding: '10px 12px', marginBottom: '4px', background: 'transparent', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontSize: '14px' },
  navActive: { display: 'block', width: '100%', padding: '10px 12px', marginBottom: '4px', background: '#1a56db', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontSize: '14px' },
  logout: { padding: '10px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', marginTop: 'auto' },
  content: { flex: 1, padding: '36px', background: '#f9fafb' },
  pageTitle: { marginBottom: '24px', color: '#111827' },
  balanceCard: { background: 'linear-gradient(135deg, #1a56db, #1e3a5f)', color: 'white', padding: '32px', borderRadius: '16px', marginBottom: '20px', textAlign: 'center' },
  card: { background: 'white', padding: '24px', borderRadius: '12px', marginBottom: '16px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' },
  input: { width: '100%', padding: '12px', border: '1px solid #d1d5db', borderRadius: '8px', fontSize: '15px', marginBottom: '12px', display: 'block' },
  btn: { padding: '12px 28px', background: '#1a56db', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontSize: '15px' },
  txRow: { display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid #f3f4f6' },
  table: { width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)', overflow: 'hidden' },
}