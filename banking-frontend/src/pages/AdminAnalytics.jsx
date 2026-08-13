import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    ArrowLeft, Filter, ShieldCheck, Landmark, Users, Activity,
    TrendingUp, ChevronDown, List, Database, Zap, PieChart, BarChart3, Search, ArrowLeftRight
} from 'lucide-react';
import { Bar, Pie, Line, Doughnut } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement, PointElement, LineElement } from 'chart.js';
import API from '../api';

ChartJS.register(CategoryScale, LinearScale, BarElement, ArcElement, PointElement, LineElement, Title, Tooltip, Legend);

const AdminAnalytics = () => {
    // --- 1. STATE MANAGEMENT ---
    const [view, setView] = useState('overview');
    const [range, setRange] = useState('all');
    const [limit, setLimit] = useState(10);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [sortBy, setSortBy] = useState('transactionDate');
    const [order, setOrder] = useState('desc');

    const [allData, setAllData] = useState({});
    const [totalStats, setTotalStats] = useState({ balance: 0, users: 0 });
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    // --- 2. DATA FETCHING ---
    useEffect(() => { fetchAdminData(); }, [view, range, limit, sortBy, order, startDate, endDate]);

    const fetchAdminData = async () => {
        setLoading(true);
        try {
            const s = startDate ? `${startDate}T00:00:00` : '';
            const e = endDate ? `${endDate}T23:59:59` : '';
            const p = `range=${range}&start=${s}&end=${e}&limit=${limit || 1000}`;

            const [bal, count, vol, spenders, accounts, growth, cash, rev, dist, activity, search] = await Promise.all([
                API.get('/admin/analytics/total-balance'),
                API.get('/admin/users/count'),
                API.get('/admin/analytics/transaction-volume'),
                API.get(`/admin/analytics/top-spenders?${p}`),
                API.get(`/admin/analytics/top-accounts-range?${p}`),
                API.get(`/admin/analytics/growth?range=${range}&start=${s}&end=${e}`),
                API.get(`/admin/analytics/cash-flow?range=${range}&start=${s}&end=${e}`),
                API.get('/admin/analytics/monthly-revenue'),
                API.get('/admin/analytics/account-distribution'),
                API.get(`/admin/analytics/user-activity?range=${range}`),
                API.get(`/admin/transactions/search?${p}&sortBy=${sortBy}&direction=${order}`)
            ]);

            setTotalStats({ balance: bal.data, users: count.data });
            setAllData({
                vol: vol.data,
                spenders: spenders.data,
                accounts: accounts.data,
                growth: growth.data,
                cash: cash.data,
                rev: rev.data,
                dist: dist.data,
                activity: activity.data,
                history: search.data
            });
        } catch (err) { console.error("Admin Data Error", err); }
        finally { setLoading(false); }
    };

    const menuItems = [
        { id: 'overview', name: 'Intelligence Grid', icon: <Database size={18}/> },
        { id: 'growth', name: 'User Growth', icon: <TrendingUp size={18}/> },
        { id: 'volume', name: 'Transaction Load', icon: <Zap size={18}/> },
        { id: 'cashflow', name: 'System Cash Flow', icon: <BarChart3 size={18}/> },
        { id: 'revenue', name: 'Bank Revenue', icon: <Landmark size={18}/> },
        { id: 'spenders', name: 'Top Spenders', icon: <Activity size={18}/> },
        { id: 'wealth', name: 'Top Accounts', icon: <PieChart size={18}/> },
        { id: 'history', name: 'System Ledger', icon: <List size={18}/> },
    ];

    return (
        <div className="flex min-h-screen bg-slate-950 text-slate-100 font-sans">
            {/* --- SIDEBAR --- */}
            <aside className="w-72 bg-slate-900 border-r border-slate-800 p-8 flex flex-col h-screen sticky top-0 z-30">
                <button onClick={() => navigate('/admin-dashboard')} className="flex items-center gap-2 text-slate-500 font-bold text-[10px] uppercase mb-12 hover:text-emerald-400 transition-all">
                    <ArrowLeft size={14}/> Terminal Home
                </button>
                <h2 className="text-2xl font-black italic tracking-tighter mb-10 text-emerald-400">ADMIN INTEL</h2>
                <nav className="space-y-2 text-left">
                    {menuItems.map(item => (
                        <button key={item.id} onClick={() => setView(item.id)} className={`w-full px-6 py-4 rounded-2xl text-[10px] font-black uppercase tracking-[0.2em] transition-all flex items-center gap-4 ${view === item.id ? 'bg-emerald-600 text-white shadow-emerald-500/20 shadow-lg' : 'text-slate-500 hover:bg-slate-800'}`}>
                            {item.icon} {item.name}
                        </button>
                    ))}
                </nav>
            </aside>

            {/* --- MAIN PANEL --- */}
            <main className="flex-1 p-10 overflow-y-auto">
                <div className="flex justify-between items-end mb-12">
                    <div className="text-left">
                        <h1 className="text-5xl font-black italic uppercase tracking-tighter">{view} Matrix</h1>
                        <p className="text-slate-500 text-[10px] font-black uppercase tracking-[0.3em] mt-2 ml-1">Central Oversight Engine</p>
                    </div>

                    {/* FILTERS */}
                    <div className="flex flex-wrap gap-3 bg-slate-900 p-3 rounded-[2rem] border border-slate-800 shadow-2xl">
                        <select value={range} onChange={e => setRange(e.target.value)} className="bg-slate-800 px-4 py-2 rounded-xl text-[10px] font-black uppercase outline-none text-emerald-400 border border-slate-700">
                            <option value="all">Range: None</option>
                            <option value="today">Today</option>
                            <option value="week">Week</option>
                            <option value="month">Month</option>
                            <option value="year">Year</option>
                            <option value="custom">Custom</option>
                        </select>

                        {range === 'custom' && (
                            <div className="flex gap-2 animate-in fade-in slide-in-from-right-4">
                                <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} className="bg-slate-800 text-[10px] p-2 rounded-lg text-white border border-slate-700"/>
                                <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} className="bg-slate-800 text-[10px] p-2 rounded-lg text-white border border-slate-700"/>
                            </div>
                        )}

                        {(view === 'spenders' || view === 'wealth' || view === 'history') && (
                            <input type="number" placeholder="Limit" value={limit} onChange={e => setLimit(e.target.value)} className="bg-slate-800 w-20 px-4 py-2 rounded-xl text-[10px] font-black text-blue-400 border border-slate-700" />
                        )}
                    </div>
                </div>

                {loading ? (
                    <div className="h-96 flex items-center justify-center font-black text-slate-800 animate-pulse text-6xl italic">DECRYPTING...</div>
                ) : (
                    <div className={view === 'overview' ? "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6" : "w-full"}>
                        {view === 'overview' ? (
                            <>
                                <StatCard title="Bank Balance" val={`₹${totalStats.balance?.toLocaleString()}`} icon={<Landmark/>}/>
                                <StatCard title="Total Nodes" val={totalStats.users} icon={<Users/>}/>
                                <div className="lg:col-span-2 row-span-2">
                                    <ChartContainer id="ov-growth" title="Growth Metrics" chart={<Line key="ov-g" data={getGrowthData(allData.growth)} options={darkOps}/>}/>
                                </div>
                                <ChartContainer id="ov-cash" title="Cash Flow" chart={<Pie key="ov-c" data={getCashData(allData.cash)} options={darkOps}/>}/>
                                <ChartContainer id="ov-vol" title="Transaction Volume" chart={<Bar key="ov-v" data={getVolData(allData.vol)} options={darkOps}/>}/>
                                <div className="lg:col-span-2">
                                    <ChartContainer id="ov-spend" title="Top System Spenders" chart={<Bar key="ov-s" data={getAmountBarData(allData.spenders)} options={darkOps}/>}/>
                                </div>
                                <div className="lg:col-span-2">
                                    <ChartContainer id="ov-wealth" title="Wealthiest Accounts" chart={<Bar key="ov-w" data={getAmountBarData(allData.accounts)} options={darkOps}/>}/>
                                </div>
                            </>
                        ) : (
                            <div className="bg-slate-900 p-10 rounded-[3rem] border border-slate-800 min-h-[600px] shadow-2xl">
                                {view === 'growth' && <Line key="det-g" data={getGrowthData(allData.growth)} options={darkOps} />}
                                {view === 'volume' && <Bar key="det-v" data={getVolData(allData.vol)} options={darkOps} />}
                                {view === 'spenders' && <Bar key="det-s" data={getAmountBarData(allData.spenders)} options={darkOps} />}
                                {view === 'wealth' && <Bar key="det-w" data={getAmountBarData(allData.accounts)} options={{...darkOps, indexAxis: 'y'}} />}
                                {view === 'cashflow' && <div className="h-[500px] w-[500px] mx-auto"><Doughnut key="det-c" data={getCashData(allData.cash)} options={darkOps} /></div>}
                                {view === 'revenue' && <Bar key="det-r" data={getAmountBarData(allData.rev)} options={darkOps} />}
                                {view === 'history' && <AdminHistoryTable list={allData.history} />}
                            </div>
                        )}
                    </div>
                )}
            </main>
        </div>
    );
};

// --- CHART DATA GENERATORS ---

const getCashData = (data) => ({
    labels: ['Inflow', 'Outflow'],
    datasets: [{ data: [data?.inflow || 0, data?.outflow || 0], backgroundColor: ['#10b981', '#f43f5e'], borderWidth: 0 }]
});

const getVolData = (data) => ({
    labels: Object.keys(data || {}),
    datasets: [{ label: 'Transactions', data: Object.values(data || {}), backgroundColor: '#8b5cf6', borderRadius: 8 }]
});

const getAmountBarData = (data) => ({
    labels: Object.keys(data || {}).map(k => k.length > 12 ? k.substring(0,10)+'..' : k),
    datasets: [{ label: 'Volume (₹)', data: Object.values(data || {}), backgroundColor: '#3b82f6', borderRadius: 8 }]
});

const getGrowthData = (data) => ({
    labels: ['Previous', 'Current'],
    datasets: [
        { label: 'Users', data: [0, data?.newUsers], borderColor: '#10b981', tension: 0.4 },
        { label: 'Trans', data: [0, data?.transactions], borderColor: '#3b82f6', tension: 0.4 }
    ]
});

// --- UI COMPONENTS ---

const StatCard = ({ title, val, icon }) => (
    <div className="bg-slate-900 p-6 rounded-[2rem] border border-slate-800 flex items-center gap-5">
        <div className="p-4 bg-slate-800 rounded-2xl text-emerald-400">{icon}</div>
        <div className="text-left">
            <p className="text-[9px] font-black text-slate-500 uppercase tracking-widest">{title}</p>
            <h3 className="text-xl font-black text-white">{val}</h3>
        </div>
    </div>
);

const ChartContainer = ({ title, chart, id }) => (
    <div key={id} className="bg-slate-900/50 p-8 rounded-[2.5rem] border border-slate-800 h-full flex flex-col group hover:border-emerald-500/20 transition-all">
        <h3 className="text-[9px] font-black uppercase text-slate-500 mb-6 tracking-widest text-left group-hover:text-emerald-400">{title}</h3>
        <div className="flex-1 w-full overflow-hidden">{chart}</div>
    </div>
);

const AdminHistoryTable = ({ list }) => (
    <div className="w-full overflow-x-auto">
        <table className="w-full text-left">
            <thead>
                <tr className="text-[10px] font-black uppercase text-slate-500 border-b border-slate-800">
                    <th className="pb-4">Timestamp</th><th className="pb-4">Type</th><th className="pb-4">Node ID</th><th className="pb-4 text-right">Volume (₹)</th>
                </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
                {list?.map(t => (
                    <tr key={t.id} className="text-xs font-bold hover:bg-white/5 transition-colors">
                        <td className="py-5 text-slate-400">{new Date(t.transactionDate).toLocaleString()}</td>
                        <td className="py-5 uppercase"><span className="px-2 py-1 bg-slate-800 rounded text-[9px]">{t.type}</span></td>
                        <td className="py-5 font-mono text-emerald-500">#{t.account?.id || 'SYS'}</td>
                        <td className="py-5 text-right font-black">₹{t.amount?.toLocaleString()}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    </div>
);

const darkOps = {
    maintainAspectRatio: false,
    plugins: {
        legend: { display: false },
        tooltip: {
            callbacks: {
                label: (context) => {
                    const label = context.dataset.label || '';
                    const value = context.parsed.y || context.parsed || 0;
                    return label.includes('₹') ? `${label}: ₹${value.toLocaleString()}` : `${label}: ${value} Units`;
                }
            }
        }
    },
    scales: {
        y: { grid: { color: '#1e293b' }, ticks: { color: '#475569', font: { size: 9 } } },
        x: { grid: { display: false }, ticks: { color: '#475569', font: { size: 9 } } }
    }
};

export default AdminAnalytics;

