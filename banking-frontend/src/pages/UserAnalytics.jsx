import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Filter, Calendar, BarChart3, PieChart, Activity, HeartPulse, ListOrdered, LayoutGrid } from 'lucide-react';
import { Bar, Pie, Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement, PointElement, LineElement } from 'chart.js';
import API from '../api';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, PointElement, LineElement, Title, Tooltip, Legend);

const UserAnalytics = () => {
    const [view, setView] = useState('overview');
    const [range, setRange] = useState('month');
    const [limit, setLimit] = useState(10);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    const [accountInfo, setAccountInfo] = useState(null);
    const [allData, setAllData] = useState({ spending: 0, creditDebit: null, frequency: null, health: 0, transactions: [] });
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const today = new Date().toISOString().split('T')[0];

    useEffect(() => {
        const init = async () => {
            try {
                const acc = await API.get('/accounts/my-account');
                setAccountInfo(acc.data);
                fetchUserAnalytics();
            } catch (e) { navigate('/dashboard'); }
        };
        init();
    }, [view, range, limit, startDate, endDate]);

    const fetchUserAnalytics = async () => {
        setLoading(true);
        try {
            const s = startDate ? `${startDate}T00:00:00` : '';
            const e = endDate ? `${endDate}T23:59:59` : '';
            const p = `range=${range}&start=${s}&end=${e}`;

            const [spend, cd, freq, health, trans] = await Promise.all([
                API.get(`/users/analytics/spending?${p}`),
                API.get(`/users/analytics/credit-debit?${p}`),
                API.get(`/users/analytics/frequency?${p}`),
                API.get(`/users/analytics/health?${p}`),
                API.get(`/users/transactions?${p}&limit=${limit || ''}`)
            ]);

            setAllData({ spending: spend.data, creditDebit: cd.data, frequency: freq.data, health: health.data, transactions: trans.data });
        } catch (err) { console.error(err); }
        finally { setLoading(false); }
    };

    return (
        <div className="flex min-h-screen bg-white text-slate-900">
            {/* SIDEBAR */}
            <aside className="w-72 bg-slate-50 border-r border-slate-200 p-6 sticky top-0 h-screen flex flex-col">
                <button onClick={() => navigate('/dashboard')} className="flex items-center gap-2 text-slate-400 font-black text-[10px] uppercase mb-10 hover:text-blue-600 transition-all">
                    <ArrowLeft size={14}/> Back to Dashboard
                </button>
                <h2 className="text-2xl font-black italic tracking-tighter mb-8 text-blue-700">PERSONAL INTEL</h2>
                <nav className="space-y-2 text-left">
                    {['overview', 'spending', 'credit-debit', 'frequency', 'health', 'transactions'].map(id => (
                        <button key={id} onClick={() => setView(id)} className={`w-full px-5 py-4 rounded-2xl text-[11px] font-black uppercase tracking-widest transition-all ${view === id ? 'bg-blue-600 text-white shadow-xl' : 'text-slate-400 hover:bg-white hover:shadow-sm'}`}>
                            {id === 'overview' ? 'Full Grid View' : id.replace('-', ' ')}
                        </button>
                    ))}
                </nav>
            </aside>

            <main className="flex-1 p-10 overflow-y-auto">
                <div className="flex justify-between items-end mb-12">
                    <div className="text-left">
                        <h1 className="text-4xl font-black italic tracking-tighter uppercase">{view.replace('-', ' ')}</h1>
                        <p className="text-slate-400 text-xs font-bold uppercase tracking-widest mt-1 ml-1">Live Financial Analysis</p>
                    </div>

                    {/* FILTERS */}
                    <div className="flex gap-3 bg-slate-50 p-2 rounded-[2rem] border border-slate-100 shadow-inner">
                        <select value={range} onChange={e => setRange(e.target.value)} className="bg-white px-4 py-2 rounded-2xl text-[10px] font-black uppercase outline-none border border-slate-200">
                            <option value="all">Time: None</option>
                            <option value="today">Today</option>
                            <option value="week">Weekly</option>
                            <option value="month">Monthly</option>
                            <option value="year">Yearly</option>
                            <option value="custom">Custom</option>
                        </select>

                        {range === 'custom' && (
                            <div className="flex items-center gap-2 animate-in fade-in slide-in-from-right-4">
                                <input type="date" min={accountInfo?.user?.createdAt?.split('T')[0]} max={today} value={startDate} onChange={e => setStartDate(e.target.value)} className="bg-white px-2 py-2 rounded-lg text-[10px] font-bold border border-blue-200" />
                                <input type="date" max={today} value={endDate} onChange={e => setEndDate(e.target.value)} className="bg-white px-2 py-2 rounded-lg text-[10px] font-bold border border-blue-200" />
                            </div>
                        )}

                        {(view === 'transactions' || view === 'overview') && (
                            <select value={limit} onChange={e => setLimit(Number(e.target.value))} className="bg-white px-4 py-2 rounded-2xl text-[10px] font-black uppercase outline-none border border-slate-200">
                                <option value="0">Limit: None</option>
                                <option value="5">Top 5</option>
                                <option value="10">Top 10</option>
                                <option value="20">Top 20</option>
                            </select>
                        )}
                    </div>
                </div>

                {loading ? <div className="h-96 flex items-center justify-center font-black text-slate-200 animate-pulse tracking-[1em]">DECRYPTING...</div> : (
                    <div className={view === 'overview' ? "grid grid-cols-1 md:grid-cols-2 gap-8" : "w-full"}>
                        {view === 'overview' ? (
                            <>
                                <GridCard id="cd" title="Inflow vs Outflow" chart={<Doughnut key="cd-chart" data={getCDData(allData.creditDebit)} />} />
                                <GridCard id="freq" title="Transaction habits" chart={<Bar key="freq-chart" data={getFreqData(allData.frequency)} options={{indexAxis:'y', maintainAspectRatio:false}} />} />
                                <GridCard id="health" title="System Health" chart={<HealthGauge score={allData.health} />} />
                                <GridCard id="ledger" title="Recent Ledger" chart={<SimpleList list={allData.transactions} />} />
                            </>
                        ) : (
                            <div className="bg-white p-12 rounded-[3rem] shadow-2xl border border-slate-50 min-h-[600px] flex items-center justify-center">
                                {view === 'spending' && <Bar key="spending-lg" data={{labels: ['Total Spending'], datasets:[{label: '₹', data: [allData.spending], backgroundColor: '#3b82f6', borderRadius: 20}]}} />}
                                {view === 'credit-debit' && <div className="h-[450px]"><Doughnut key="cd-lg" data={getCDData(allData.creditDebit)} /></div>}
                                {view === 'frequency' && <Bar key="freq-lg" data={getFreqData(allData.frequency)} options={{indexAxis:'y'}} />}
                                {view === 'health' && <HealthGauge score={allData.health} large />}
                                {view === 'transactions' && <FullTable list={allData.transactions} />}
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
};

const GridCard = ({ title, chart, id }) => (
    <div key={id} className="bg-white p-8 rounded-[2.5rem] border border-slate-100 shadow-sm flex flex-col h-[400px]">
        <h3 className="text-[10px] font-black uppercase text-slate-400 mb-6 tracking-widest text-left">{title}</h3>
        <div className="flex-1 flex items-center justify-center overflow-hidden w-full">{chart}</div>
    </div>
);

const HealthGauge = ({ score, large }) => {
    const s = Math.round(score);
    return (
        <div className="text-center">
            <div className={`${large ? 'text-[12rem]' : 'text-7xl'} font-black italic tracking-tighter ${s > 70 ? 'text-emerald-500' : 'text-rose-500'}`}>{s}%</div>
            <p className="text-slate-300 font-bold uppercase text-[10px] mt-4 tracking-[0.5em]">Stability Index</p>
        </div>
    );
};

const SimpleList = ({ list }) => (
    <div className="w-full divide-y divide-slate-50">
        {list.slice(0,5).map(t => (
            <div key={t.id} className="py-4 flex justify-between items-center text-left">
                <div><p className="text-[10px] font-black uppercase">{t.type}</p><p className="text-[8px] font-bold text-slate-300">{new Date(t.transactionDate).toLocaleDateString()}</p></div>
                <p className="font-black text-xs">₹{t.amount.toLocaleString()}</p>
            </div>
        ))}
    </div>
);

const FullTable = ({ list }) => (
    <div className="w-full overflow-x-auto text-left">
        <table className="w-full">
            <thead>
                <tr className="text-[10px] font-black uppercase text-slate-400 border-b border-slate-100">
                    <th className="pb-4">Date</th><th className="pb-4">Type</th><th className="pb-4 text-right">Amount</th>
                </tr>
            </thead>
            <tbody className="divide-y divide-slate-50">
                {list.map(t => (
                    <tr key={t.id} className="text-sm font-bold">
                        <td className="py-6 text-slate-400">{new Date(t.transactionDate).toLocaleString()}</td>
                        <td className="py-6 uppercase">{t.type}</td>
                        <td className={`py-6 text-right font-black ${t.type.includes('IN') || t.type === 'DEPOSIT' ? 'text-emerald-500' : 'text-rose-500'}`}>₹{t.amount.toLocaleString()}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    </div>
);

const getCDData = (data) => ({
    labels: ['Credit', 'Debit'],
    datasets: [{ data: [data?.credit || 0, data?.debit || 0], backgroundColor: ['#10b981', '#f43f5e'], borderWidth: 0 }]
});

const getFreqData = (data) => ({
    labels: Object.keys(data || {}),
    datasets: [{ label: 'Transactions', data: Object.values(data || {}), backgroundColor: '#6366f1', borderRadius: 10 }]
});

export default UserAnalytics;


