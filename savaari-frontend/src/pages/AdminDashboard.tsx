import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function AdminDashboard() {
  const [stats, setStats] = useState<any>(null)
  const [drivers, setDrivers] = useState<any[]>([])
  const [fares, setFares] = useState<any[]>([])
  const [tab, setTab] = useState('dashboard')
  const navigate = useNavigate()
  const user = JSON.parse(localStorage.getItem('user') || '{}')

  const loadDrivers = () => fetch('/api/admin/drivers/pending').then(r => r.json()).then(setDrivers)

  useEffect(() => {
    if (!user.success) { navigate('/login'); return }
    fetch('/api/admin/dashboard').then(r => r.json()).then(setStats)
    fetch('/api/admin/fares').then(r => r.json()).then(setFares)
    loadDrivers()
  }, [])

  const approve = async (driverId: number, decision: string) => {
    await fetch('/api/admin/driver/approve', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ driverId, decision })
    })
    loadDrivers()
  }

  const logout = () => { localStorage.clear(); navigate('/login') }

  return (
    <div style={s.page}>
      <div style={s.sidebar}>
        <h2 style={s.logo}>🚗 SavaariPK</h2>
        <p style={s.roleText}>Admin Panel</p>
        <nav style={{ flex: 1 }}>
          {[['dashboard','📊 Dashboard'],['drivers','👤 Driver Approvals'],['fares','💰 Fare Config']].map(([key, label]) => (
            <button key={key} style={tab === key ? s.navActive : s.nav} onClick={() => setTab(key)}>{label}</button>
          ))}
        </nav>
        <button style={s.logout} onClick={logout}>Logout</button>
      </div>

      <div style={s.content}>
        {tab === 'dashboard' && <>
          <h2 style={s.pageTitle}>Dashboard Overview</h2>
          <div style={s.statsGrid}>
            {stats && [
              ['Total Passengers', stats.totalPassengers],
              ['Total Drivers', stats.totalDrivers],
              ['Pending Approvals', stats.pendingApprovals],
              ['Rides Today', stats.ridesToday],
              ['Revenue Today', 'PKR ' + (stats.revenueToday || 0)],
            ].map(([label, val]) => (
              <div key={label as string} style={s.statCard}>
                <h3 style={s.statNum}>{val}</h3>
                <p style={s.statLabel}>{label}</p>
              </div>
            ))}
          </div>
        </>}

        {tab === 'drivers' && <>
          <h2 style={s.pageTitle}>Pending Approvals ({drivers.length})</h2>
          {drivers.length === 0
            ? <p>No pending approvals. ✅</p>
            : <table style={s.table}>
                <thead><tr><th>Name</th><th>Phone</th><th>CNIC</th><th>Vehicle</th><th>Action</th></tr></thead>
                <tbody>
                  {drivers.map((d: any) => (
                    <tr key={d.DriverID}>
                      <td>{d.FullName}</td><td>{d.Phone}</td><td>{d.CNIC}</td>
                      <td>{d.VehicleType} · {d.Make} {d.Model}</td>
                      <td>
                        <button style={s.approveBtn} onClick={() => approve(d.DriverID, 'Approved')}>Approve</button>
                        <button style={s.rejectBtn} onClick={() => approve(d.DriverID, 'Rejected')}>Reject</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
          }
        </>}

        {tab === 'fares' && <>
          <h2 style={s.pageTitle}>Fare Configuration</h2>
          <table style={s.table}>
            <thead><tr><th>Vehicle Type</th><th>Base Fare (PKR)</th><th>Per Km (PKR)</th><th>Commission %</th></tr></thead>
            <tbody>
              {fares.map((f: any) => (
                <tr key={f.VehicleType}>
                  <td>{f.VehicleType}</td><td>{f.BaseFare}</td><td>{f.PerKmRate}</td><td>{f.CommissionPct}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>}
      </div>
    </div>
  )
}

const s: any = {
  page: { display: 'flex', minHeight: '100vh', fontFamily: 'sans-serif' },
  sidebar: { width: '230px', background: '#1e3a5f', color: 'white', padding: '28px 16px', display: 'flex', flexDirection: 'column', gap: '4px' },
  logo: { color: 'white', marginBottom: '4px', fontSize: '20px' },
  roleText: { color: '#93c5fd', fontSize: '13px', marginBottom: '24px' },
  nav: { display: 'block', width: '100%', padding: '10px 12px', marginBottom: '4px', background: 'transparent', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontSize: '14px' },
  navActive: { display: 'block', width: '100%', padding: '10px 12px', marginBottom: '4px', background: '#1a56db', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontSize: '14px' },
  logout: { padding: '10px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', marginTop: 'auto' },
  content: { flex: 1, padding: '36px', background: '#f9fafb' },
  pageTitle: { marginBottom: '24px', color: '#111827' },
  statsGrid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' },
  statCard: { background: 'white', padding: '24px', borderRadius: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)', textAlign: 'center' },
  statNum: { fontSize: '28px', color: '#1a56db', marginBottom: '8px' },
  statLabel: { color: '#6b7280', fontSize: '14px' },
  table: { width: '100%', borderCollapse: 'collapse', background: 'white', borderRadius: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)', overflow: 'hidden' },
  approveBtn: { padding: '6px 14px', background: '#10b981', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', marginRight: '8px', fontSize: '13px' },
  rejectBtn: { padding: '6px 14px', background: '#ef4444', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '13px' },
}