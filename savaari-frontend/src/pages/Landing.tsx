import { useNavigate } from 'react-router-dom'

export default function Landing() {
  const navigate = useNavigate()
  return (
    <div style={s.page}>
      <div style={s.hero}>
        <h1 style={s.title}>🚗 SavaariPK</h1>
        <p style={s.tagline}>Pakistan's smarter ride-hailing & carpooling platform</p>
        <p style={s.desc}>Wallet-powered rides for solo travel and carpooling across Pakistan.<br />From Lahore to Karachi — every ride, fairer.</p>
        <button style={s.btn} onClick={() => navigate('/login')}>Get Started →</button>
        <div style={s.features}>
          <div style={s.feature}><h3>🏍 Multiple Vehicles</h3><p>Bike, Rickshaw, Car, Van & more</p></div>
          <div style={s.feature}><h3>💳 Wallet Payments</h3><p>No cash — fast & safe</p></div>
          <div style={s.feature}><h3>🤝 Carpooling</h3><p>Share rides, split fares</p></div>
        </div>
      </div>
    </div>
  )
}

const s: any = {
  page: { minHeight: '100vh', background: 'linear-gradient(135deg, #1e3a5f 0%, #1a56db 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  hero: { textAlign: 'center', color: 'white', maxWidth: '620px', padding: '40px' },
  title: { fontSize: '52px', marginBottom: '16px' },
  tagline: { fontSize: '22px', opacity: 0.9, marginBottom: '16px' },
  desc: { fontSize: '16px', opacity: 0.75, marginBottom: '32px', lineHeight: 1.7 },
  btn: { padding: '16px 40px', background: 'white', color: '#1a56db', border: 'none', borderRadius: '12px', fontSize: '18px', cursor: 'pointer', fontWeight: 'bold', marginBottom: '48px' },
  features: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' },
  feature: { background: 'rgba(255,255,255,0.1)', padding: '20px', borderRadius: '12px' },
}