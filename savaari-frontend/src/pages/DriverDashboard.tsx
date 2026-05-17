import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function DriverDashboard() {
  const [earnings, setEarnings] = useState<any>(null)
  const [rides, setRides] = useState<any[]>([])
  const [isOnline, setIsOnline] = useState(false)
  const [tab, setTab] = useState('earnings')
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const driverId = user.driverId

  useEffect(() => {
    if (!user.success) { navigate('/login'); return }
    fetch(`/api/driver/${driverId}/earnings`).then(r => r.json()).then(setEarnings)
    fetch(`/api/driver/${driverId}/rides`).then(r => r.json()).then(setRides)
  }, [])

  const toggleOnline = async () => {
    const next = !isOnline
    await fetch(`/api/driver/${driverId}/status`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ isOnline: next })
    })
    setIsOnline(next)
  }

  const logout = () => { localStorage.clear(); navigate('/login') }

  return (
    <div style={s.page}>
      <div style={s.sidebar}>
        <h2 style={s.logo}>🚗 SavaariPK</h2>
        <p style={s.roleText}>Driver</p>
        <p style={s.nameText}>{user.name}</p>
        <div style={{ marginBottom: '24px' }}>
          <p style={{ color: '#93c5fd', fontSize: '13px', marginBottom: '8px' }}>Status</p>
          <button style={{ ...s.onlineBtn, background: isOnline ? '#10b981' : '#6b7280' }} onClick={toggleOnline}>
            {isOnline ? '🟢 Online' : '⚫ Offline'}
          </button>
        </div>
        <nav style={{ flex: 1 }}>
          {[['earnings','💰 Earnings'],['rides','🚗 My Rides']].map(([key, label]) => (
            <button key={key} style={tab === key ? s.navActive : s.nav} onClick={() => setTab(key)}>{label}</button>
          ))}
        </nav>
        <button style={s.logout} onClick={logout}>Logout</button>
      </div>

      <div style={s.content}>
        {tab === 'earnings' && <>
          <h2 style={s.pageTitle}>Earnings Overview</h2>
          <div style={s.statsGrid}>
            {earnings && [
              ['Wallet Balance', `PKR ${earnings.walletBalance?.toFixed(2)}`],
              ['Total Earnings', `PKR ${earnings.totalEarnings?.toFixed(2)}`],
              ['Total Rides', earnings.totalRides],
              ['Avg Rating', `⭐ ${earnings.avgRating?.toFixed(1)}`],
            ].map(([label, val]) => (
              <div key={label as string} style={s.statCard}>
                <h3 style={s.statNum}>{val}</h3>
                <p style={s.statLabel}>{label}</p>
              </div>
            ))}
          </div>
        </>}

        {tab === 'rides' && <>
          <h2 style={s.pageTitle}>My Rides</h2>
          {rides.length === 0
            ? <p>No rides posted yet.</p>
            : <table style={s.table}>
                <thead><tr><th>From</th><th>To</th><th>Vehicle</th><th>Fare</th><th>Seats Left</th><th>Status</th></tr></thead>
                <tbody>
                  {rides.map((r: any) => (
                    <tr key={r.RideID}>
                      <td>{r.OriginCity}</td><td>{r.DestinationCity}</td>
                      <td>{r.VehicleType}</td><td>PKR {r.TotalFare}</td>
                      <td>{r.SeatsAvailable}/{r.SeatsTotal}</td><td>{r.Status}</td>
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
  onlineBtn: { padding: '10px', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', width: '100%', fontSize: '14px', fontWeight: 'bold' },
  nav: { display: 'block', width: '100%', padding: '10px 12px', marginBottom: '4px', background: 'transparent', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontSize: '14px' },
  navActive: { display: 'block', width: '100%', padding: '10px 12px', marginBottom: '4px', background: '#1a56db', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontSize: '14px' },
  logout: { padding: '10px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', marginTop: 'auto' },
  content: { flex: 1, padding: '36px', background: '#f9fafb' },
  pageTitle: { marginBottom: '24px', color: '#111827' },
  statsGrid: { display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '16px' },
  statCard: { background: 'white', padding: '28px', borderRadius: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)', textAlign: 'center' },
  statNum: { fontSize: '26px', color: '#1a56db', marginBottom: '8px' },
  statLabel: { color: '#6b7280', fontSize: '14px' },
  table: { width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)', overflow: 'hidden' },
}