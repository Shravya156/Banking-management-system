import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Users, Landmark, AlertTriangle, ShieldCheck, LogOut, Activity, Unlock, BarChart3, ChevronRight } from 'lucide-react';
import API from '../api';

const AdminDashboard = () => {
    const [stats, setStats] = useState({ totalBalance: 0, userCount: 0 });
    const [lockedUsers, setLockedUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => { fetchAdminData(); }, []);

    const fetchAdminData = async () => {
        try {
            const [bal, count, locked] = await Promise.all([
                API.get('/admin/analytics/total-balance'),
                API.get('/admin/users/count'),
                API.get('/admin/users/locked')

            ]);
            setStats({ totalBalance: bal.data, userCount: count.data });
            setLockedUsers(locked.data);
        } catch (err) {
            console.error(err);
            if (err.response?.status === 403) navigate('/dashboard');
        } finally { setLoading(false); }
    };

    const handleUnlock = async (email) => {
        try {
            await API.post(`/admin/users/unlock/${email}`);
            fetchAdminData();
        } catch (err) { alert("Unlock failed"); }
    };

    if (loading) return <div className="h-screen bg-slate-900 flex items-center justify-center font-black text-blue-500 italic">SYSTEM BOOTING...</div>;

    return (
        <div className="min-h-screen bg-slate-950 text-slate-100 pb-12">
            {/* TOP BAR */}
            <nav className="bg-slate-900/50 backdrop-blur-md px-8 py-6 flex justify-between items-center border-b border-slate-800 sticky top-0 z-50">
                <div className="flex items-center gap-3">
                    <ShieldCheck className="text-emerald-400" size={28}/>
                    <span className="text-xl font-black italic tracking-tighter uppercase">Central Command</span>
                </div>
                <button onClick={() => { localStorage.clear(); navigate('/login'); }} className="text-[10px] font-black uppercase tracking-widest text-slate-500 hover:text-rose-500 flex items-center gap-2 transition-colors">
                    <LogOut size={16}/> Terminate Session
                </button>
            </nav>

            <div className="max-w-7xl mx-auto mt-10 px-6">
                {/* 🚀 NEW SECTION: ANALYTICS LAUNCHER */}
                <div className="mb-10 grid grid-cols-1 lg:grid-cols-3 gap-6">
                    <div
                        onClick={() => navigate('/admin-analytics')}
                        className="lg:col-span-2 group relative overflow-hidden bg-gradient-to-r from-blue-900 to-indigo-900 p-8 rounded-[2.5rem] border border-blue-500/30 shadow-2xl cursor-pointer hover:scale-[1.01] transition-all"
                    >
                        <div className="absolute right-0 top-0 h-full w-1/3 bg-white/5 skew-x-12 translate-x-20"></div>
                        <div className="relative z-10 flex justify-between items-center">
                            <div>
                                <h3 className="text-2xl font-black italic uppercase tracking-tighter mb-2">Launch Intelligence Center</h3>
                                <p className="text-blue-200 text-xs font-bold uppercase tracking-widest opacity-60">Deep dive into system growth, cash flow, and risk metrics</p>
                            </div>
                            <div className="bg-white/10 p-4 rounded-3xl group-hover:bg-blue-500 transition-colors">
                                <BarChart3 size={32} className="text-white" />
                            </div>
                        </div>
                    </div>

                    <div className="bg-slate-900 p-8 rounded-[2.5rem] border border-slate-800 flex items-center justify-between">
                        <div>
                            <p className="text-slate-500 text-[10px] font-black uppercase tracking-widest">System Status</p>
                            <h2 className="text-2xl font-black text-emerald-400 mt-1 uppercase italic">Operational</h2>
                        </div>
                        <Activity className="text-emerald-500 animate-pulse" size={32} />
                    </div>
                </div>

                {/* KPI GRID */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
                    <div className="bg-slate-900 p-8 rounded-[2rem] border border-slate-800">
                        <Landmark className="text-blue-400 mb-4" size={32}/>
                        <p className="text-slate-500 text-[10px] font-black uppercase tracking-widest">Global Liquid Assets</p>
                        <h2 className="text-3xl font-black mt-1 tracking-tighter text-left">₹{stats.totalBalance?.toLocaleString('en-IN')}</h2>
                    </div>
                    <div className="bg-slate-900 p-8 rounded-[2rem] border border-slate-800">
                        <Users className="text-purple-400 mb-4" size={32}/>
                        <p className="text-slate-500 text-[10px] font-black uppercase tracking-widest">Active Nodes</p>
                        <h2 className="text-3xl font-black mt-1 text-left">{stats.userCount} ACCOUNTS</h2>
                    </div>
                    <div className="bg-rose-500/5 p-8 rounded-[2rem] border border-rose-500/20">
                        <AlertTriangle className="text-rose-500 mb-4" size={32}/>
                        <p className="text-rose-500/50 text-[10px] font-black uppercase tracking-widest">Threats Restricted</p>
                        <h2 className="text-3xl font-black mt-1 text-left">{lockedUsers.length} BLOCKED</h2>
                    </div>
                </div>

                {/* BOTTOM SECTION: LOCKED USERS */}
                <div className="bg-slate-900 rounded-[3rem] overflow-hidden border border-slate-800 shadow-2xl">
                    <div className="p-8 border-b border-slate-800 flex justify-between items-center">
                        <h3 className="font-black italic text-sm uppercase tracking-widest text-slate-400">Security Restricted Accounts</h3>
                    </div>
                    <div className="divide-y divide-slate-800">
                        {lockedUsers.length === 0 ? (
                            <div className="p-20 text-center text-slate-600 font-bold uppercase text-xs tracking-widest italic">No active threats detected in the perimeter</div>
                        ) : (
                            lockedUsers.map(user => (
                                <div key={user.id} className="p-8 flex justify-between items-center hover:bg-white/5 transition-colors">
                                    <div className="text-left">
                                        <p className="font-black text-lg uppercase tracking-tight">{user.name}</p>
                                        <p className="text-xs font-bold text-slate-500 uppercase">{user.email}</p>
                                    </div>
                                    <button
                                        onClick={() => handleUnlock(user.email)}
                                        className="flex items-center gap-3 bg-emerald-500 text-slate-950 px-6 py-3 rounded-2xl font-black uppercase text-[10px] hover:bg-emerald-400 transition-all shadow-lg shadow-emerald-500/20 active:scale-95"
                                    >
                                        <Unlock size={16}/> Bypass Restriction
                                    </button>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;