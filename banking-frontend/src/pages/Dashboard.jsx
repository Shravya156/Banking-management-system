import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogOut, Plus, ArrowUpRight, Send, Wallet, History, X, ShieldCheck, KeyRound, RefreshCcw, PieChart as ChartIcon } from 'lucide-react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Pie } from 'react-chartjs-2';
import API from '../api';

ChartJS.register(ArcElement, Tooltip, Legend);

const Dashboard = () => {
    // --- STATE MANAGEMENT ---
    const [account, setAccount] = useState({ balance: 0, accountNumber: '', pinSet: false });
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [hasNoAccount, setHasNoAccount] = useState(false);
    const [isProcessing, setIsProcessing] = useState(false);

    // Transaction Modal State
    const [showModal, setShowModal] = useState(null);
    const [amount, setAmount] = useState('');
    const [toAccountNumber, setToAccountNumber] = useState('');
    const [pin, setPin] = useState('');
    const [otp, setOtp] = useState('');
    const [requiresOtp, setRequiresOtp] = useState(false);

    // Reset PIN Modal State
    const [isResettingPin, setIsResettingPin] = useState(false);
    const [resetStep, setResetStep] = useState(1);
    const [newPin, setNewPin] = useState('');
    const [confirmPin, setConfirmPin] = useState('');
    const [resendTimer, setResendTimer] = useState(0);

    const [error, setError] = useState('');
    const navigate = useNavigate();

    // --- DATA FETCHING ---
    const fetchDashboardData = async () => {
        try {
            const accRes = await API.get('/accounts/my-account');
            setAccount(accRes.data);
            setHasNoAccount(false);
            const transRes = await API.get('/accounts/transactions');
            setTransactions(transRes.data);
        } catch (err) {
            if (err.response?.status === 404 || err.response?.status === 400) setHasNoAccount(true);
            else if (err.response?.status === 401) navigate('/login');
        } finally { setLoading(false); }
    };

    useEffect(() => { fetchDashboardData(); }, []);

    // Timer for Resend OTP
    useEffect(() => {
        if (resendTimer > 0) {
            const timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
            return () => clearTimeout(timer);
        }
    }, [resendTimer]);

    // --- ANALYTICS LOGIC (For the small Pie Chart) ---
    const inflow = transactions.filter(t => t.type.includes('IN') || t.type === 'DEPOSIT').reduce((acc, t) => acc + t.amount, 0);
    const outflow = transactions.filter(t => t.type.includes('OUT') || t.type === 'WITHDRAW').reduce((acc, t) => acc + t.amount, 0);

    const chartData = {
        labels: ['Inflow', 'Outflow'],
        datasets: [{
            data: [inflow || 1, outflow || 0], // Fallback to 1 to show empty chart
            backgroundColor: ['#10b981', '#f43f5e'],
            borderWidth: 0,
        }],
    };

    // --- HANDLERS ---
    const closePinModal = () => {
        setIsResettingPin(false);
        setResetStep(1);
        setOtp('');
        setNewPin('');
        setConfirmPin('');
        setError('');
    };

    const handleRequestResetOtp = async () => {
        setIsProcessing(true);
        try {
            await API.post('/accounts/request-pin-reset');
            setResetStep(2);
            setResendTimer(60);
        } catch (err) { setError("Failed to send code."); }
        finally { setIsProcessing(false); }
    };

    const handleVerifyResetOtp = async (e) => {
        e.preventDefault();
        setIsProcessing(true);
        try {
            const email = localStorage.getItem('userEmail');
            await API.post(`/auth/verify-reset-otp?email=${email}&otp=${otp}`);
            setResetStep(3);
        } catch (err) { setError("Invalid Code."); }
        finally { setIsProcessing(false); }
    };

    const handleUpdatePin = async (e) => {
        e.preventDefault();
        if (newPin !== confirmPin) { setError("PINs do not match."); return; }
        setIsProcessing(true);
        try {
            await API.post(`/accounts/reset-pin?newPin=${newPin}`);
            alert("PIN Updated!");
            closePinModal();
            fetchDashboardData();
        } catch (err) { setError("Update failed."); }
        finally { setIsProcessing(false); }
    };

    const handleTransaction = async (e) => {
        e.preventDefault();
        setIsProcessing(true);
        setError('');
        try {
            const params = { amount };
            if (showModal !== 'deposit') params.pin = pin;
            if (showModal === 'transfer') params.toAccountNumber = toAccountNumber;
            if (otp) params.otp = otp;

            await API.post(`/accounts/${showModal}`, null, { params });
            setShowModal(null); setPin(''); setAmount(''); setToAccountNumber(''); setOtp(''); setRequiresOtp(false);
            fetchDashboardData();
            alert("Success!");
        } catch (err) {
            if (err.response?.data?.status === "OTP_REQUIRED") {
                setRequiresOtp(true);
                setError("Security verification required.");
            } else { setError(err.response?.data?.message || "Transaction Failed."); }
        } finally { setIsProcessing(false); }
    };

    if (loading) return <div className="h-screen flex items-center justify-center font-bold text-blue-700">AUTHENTICATING...</div>;

    return (
        <div className="min-h-screen bg-slate-50 pb-12 text-slate-800">
            {/* NAVBAR */}
            <nav className="bg-white px-8 py-4 shadow-sm flex justify-between items-center sticky top-0 z-40">
                <div className="text-xl font-black text-blue-700 italic tracking-tighter">FORTIS TRUST</div>
                <button onClick={() => { localStorage.clear(); navigate('/login'); }} className="text-[10px] font-black uppercase text-slate-400 hover:text-red-600 flex items-center gap-2">
                    <LogOut size={16} /> Logout
                </button>
            </nav>

            <div className="max-w-6xl mx-auto mt-8 px-4 text-left">
                {hasNoAccount ? (
                    /* SCREEN: NEW USER INITIALIZATION */
                    <div className="max-w-md mx-auto mt-20 text-center bg-white p-12 rounded-[3rem] shadow-xl border-2 border-dashed border-blue-100">
                        <Plus className="text-blue-600 mx-auto mb-6" size={40} />
                        <h2 className="text-3xl font-black text-slate-800 italic mb-2 uppercase tracking-tighter">Welcome</h2>
                        <p className="text-slate-400 text-xs font-bold uppercase mb-8">Your vault is ready to be opened.</p>
                        <button onClick={async () => { await API.post('/accounts/create-my-first-account'); fetchDashboardData(); }} className="w-full py-5 bg-blue-700 text-white rounded-2xl font-black uppercase text-xs shadow-lg">Initialize My Account</button>
                    </div>
                ) : !account.pinSet ? (
                    /* SCREEN: SET PIN ONBOARDING */
                    <div className="max-w-md mx-auto mt-20 text-center bg-white p-12 rounded-[3rem] shadow-xl border-2 border-blue-600">
                        <ShieldCheck className="text-blue-600 mx-auto mb-6" size={48} />
                        <h2 className="text-2xl font-black text-slate-800 uppercase italic mb-8">Set Transaction PIN</h2>
                        <form onSubmit={async (e) => { e.preventDefault(); setIsProcessing(true); await API.post(`/accounts/set-pin?pin=${pin}`); setIsProcessing(false); setPin(''); fetchDashboardData(); }} className="space-y-4">
                            <input type="password" maxLength="6" required value={pin} onChange={e => setPin(e.target.value)} className="w-full bg-slate-50 border-2 p-4 text-center text-3xl font-black tracking-[0.5em] rounded-2xl outline-none focus:border-blue-600" placeholder="000000" />
                            <button type="submit" disabled={isProcessing} className="w-full py-5 bg-blue-700 text-white rounded-2xl font-black uppercase text-xs">{isProcessing ? 'Saving...' : 'Set Security PIN'}</button>
                        </form>
                    </div>
                ) : (
                    /* DASHBOARD GRID */
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                        {/* LEFT COLUMN */}
                        <div className="space-y-6">
                            {/* BALANCE BOX */}
                            <div className="bg-gradient-to-br from-blue-600 to-blue-800 rounded-[2rem] p-8 text-white shadow-2xl relative overflow-hidden">
                                <p className="text-blue-100 text-[10px] font-bold uppercase tracking-widest mb-1">Total Balance</p>
                                <h2 className="text-4xl font-black mb-10 tracking-tight">₹{(account.balance || 0).toLocaleString('en-IN')}</h2>
                                <p className="text-blue-300 text-[10px] font-mono tracking-widest uppercase">ID: {account.accountNumber}</p>
                            </div>

                            {/* RESET PIN BUTTON */}
                            <button onClick={() => setIsResettingPin(true)} className="w-full py-4 bg-white rounded-2xl border border-slate-100 shadow-sm flex items-center justify-center gap-3 text-[10px] font-black uppercase tracking-widest text-slate-400 hover:text-blue-600 transition-all">
                                <KeyRound size={14} /> Change Security PIN
                            </button>

                            {/* OPERATIONS CARD WITH CHART */}
                            <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-100">
                                <h3 className="font-black text-slate-400 text-[10px] uppercase tracking-widest mb-6">Operations</h3>
                                <div className="grid grid-cols-3 gap-4 mb-8">
                                    <button onClick={() => {setShowModal('deposit'); setError('');}} className="flex flex-col items-center gap-2 group"><div className="bg-blue-50 text-blue-600 p-4 rounded-2xl group-hover:bg-blue-600 group-hover:text-white transition-all"><Plus size={20}/></div><span className="text-[9px] font-black uppercase text-slate-400">Deposit</span></button>
                                    <button onClick={() => {setShowModal('withdraw'); setError('');}} className="flex flex-col items-center gap-2 group"><div className="bg-orange-50 text-orange-600 p-4 rounded-2xl group-hover:bg-orange-600 group-hover:text-white transition-all"><ArrowUpRight size={20}/></div><span className="text-[9px] font-black uppercase text-slate-400">Withdraw</span></button>
                                    <button onClick={() => {setShowModal('transfer'); setError('');}} className="flex flex-col items-center gap-2 group"><div className="bg-emerald-50 text-emerald-600 p-4 rounded-2xl group-hover:bg-emerald-600 group-hover:text-white transition-all"><Send size={20}/></div><span className="text-[9px] font-black uppercase text-slate-400">Transfer</span></button>
                                </div>

                                {/* CHART SECTION */}
                                <div className="pt-6 border-t border-slate-50 flex flex-col items-center">
                                    <p className="text-[9px] font-black text-slate-300 uppercase mb-4 tracking-widest">Recent Cash Flow</p>
                                    <div className="h-32 w-32 mb-6">
                                        {transactions.length > 0 ? <Pie data={chartData} options={{ maintainAspectRatio: false, plugins: { legend: { display: false } } }} /> : <p className="text-[9px] text-slate-200 font-bold uppercase mt-8">No Activity</p>}
                                    </div>
                                    <button onClick={() => navigate('/analytics')} className="w-full py-3 bg-slate-50 border border-slate-100 rounded-xl text-[9px] font-black uppercase tracking-widest text-blue-600 hover:bg-blue-600 hover:text-white transition-all flex items-center justify-center gap-2">
                                        <ChartIcon size={12}/> View Detailed Analytics
                                    </button>
                                </div>
                            </div>
                        </div>

                        {/* RIGHT COLUMN: HISTORY */}
                        <div className="lg:col-span-2">
                             <div className="bg-white rounded-[2rem] shadow-sm border border-slate-100 overflow-hidden min-h-[500px]">
                                <div className="p-6 border-b border-slate-50 flex items-center gap-2 font-black text-slate-800 uppercase tracking-tighter">
                                    <History size={18} className="text-blue-600" /> Recent Transactions
                                </div>
                                <div className="divide-y divide-slate-50">
                                    {transactions.length === 0 ? <div className="p-20 text-center text-slate-300 font-bold uppercase text-[10px]">No Transactions found</div> :
                                    transactions.map(t => (
                                        <div key={t.id} className="p-5 flex justify-between items-center hover:bg-slate-50 transition-colors">
                                            <div className="flex items-center gap-4 text-left">
                                                <div className={`p-2.5 rounded-xl ${t.type.includes('IN') || t.type === 'DEPOSIT' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'}`}><Plus size={18}/></div>
                                                <div><p className="font-black text-xs uppercase">{t.type}</p><p className="text-[9px] font-bold text-slate-400 uppercase">{new Date(t.transactionDate).toLocaleDateString()}</p></div>
                                            </div>
                                            <p className={`font-black ${t.type.includes('IN') || t.type === 'DEPOSIT' ? 'text-emerald-600' : 'text-rose-600'}`}>₹{t.amount.toLocaleString()}</p>
                                        </div>
                                    ))}
                                </div>
                             </div>
                        </div>
                    </div>
                )}
            </div>

            {/* MODAL: RESET PIN */}
            {isResettingPin && (
                <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-md flex items-center justify-center p-4 z-50">
                    <div className="bg-white rounded-[2.5rem] w-full max-w-md p-10 shadow-2xl relative border-t-8 border-emerald-500">
                        <button onClick={closePinModal} className="absolute right-8 top-8 text-slate-300 hover:text-slate-800 transition-transform hover:rotate-90"><X /></button>
                        <h2 className="text-2xl font-black mb-1 text-slate-800 uppercase italic">Security Reset</h2>
                        <p className="text-slate-400 text-[10px] font-bold uppercase mb-8">Phase {resetStep} of 3</p>
                        {error && <div className="mb-6 p-4 rounded-xl text-[10px] font-black border bg-rose-50 text-rose-700 uppercase">{error}</div>}

                        {resetStep === 1 && (
                            <button onClick={handleRequestResetOtp} disabled={isProcessing} className="w-full py-5 bg-blue-700 text-white rounded-2xl font-black uppercase text-xs">{isProcessing ? 'Sending...' : 'Request OTP Code'}</button>
                        )}
                        {resetStep === 2 && (
                            <form onSubmit={handleVerifyResetOtp} className="space-y-6">
                                <input type="text" maxLength="6" required value={otp} onChange={e => setOtp(e.target.value)} className="w-full bg-slate-50 border-2 p-4 text-center text-3xl font-black tracking-[0.5em] rounded-2xl" placeholder="000000" />
                                <button type="submit" disabled={isProcessing} className="w-full py-5 bg-blue-700 text-white rounded-2xl font-black uppercase text-xs">Verify Code</button>
                                <div className="text-center pt-2">
                                    {resendTimer > 0 ? <span className="text-[10px] font-bold text-slate-300 uppercase">Retry in {resendTimer}s</span> :
                                    <button type="button" onClick={handleRequestResetOtp} className="text-[10px] font-black text-blue-600 flex items-center gap-2 mx-auto uppercase"><RefreshCcw size={12}/> Resend Code</button>}
                                </div>
                            </form>
                        )}
                        {resetStep === 3 && (
                            <form onSubmit={handleUpdatePin} className="space-y-5 text-left">
                                <label className="text-[9px] font-black text-slate-400 uppercase ml-2">New PIN</label>
                                <input type="password" maxLength="6" required value={newPin} onChange={e => setNewPin(e.target.value)} className="w-full bg-slate-50 border-2 p-4 text-center text-2xl font-black tracking-[0.5em] rounded-2xl" placeholder="******" />
                                <label className="text-[9px] font-black text-slate-400 uppercase ml-2">Confirm New PIN</label>
                                <input type="password" maxLength="6" required value={confirmPin} onChange={e => setConfirmPin(e.target.value)} className="w-full bg-slate-50 border-2 p-4 text-center text-2xl font-black tracking-[0.5em] rounded-2xl" placeholder="******" />
                                <button type="submit" disabled={isProcessing} className="w-full py-5 bg-emerald-600 text-white rounded-2xl font-black uppercase text-xs">Finalize Security Update</button>
                            </form>
                        )}
                    </div>
                </div>
            )}

            {/* MODAL: TRANSACTIONS */}
            {showModal && (
                <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-md flex items-center justify-center p-4 z-50">
                    <div className={`bg-white rounded-[2.5rem] w-full max-w-md p-10 shadow-2xl relative border-t-8 ${requiresOtp ? 'border-orange-500' : 'border-blue-600'}`}>
                        <button onClick={() => {setShowModal(null); setRequiresOtp(false); setOtp(''); setPin('');}} className="absolute right-8 top-8 text-slate-300 hover:text-slate-800"><X /></button>
                        <h2 className="text-2xl font-black mb-8 text-slate-800 uppercase italic tracking-tighter">{showModal} Funds</h2>
                        {error && <div className="mb-6 p-4 rounded-xl text-[10px] font-black border bg-rose-50 text-rose-700 uppercase">{error}</div>}

                        <form onSubmit={handleTransaction} className="space-y-5 text-left">
                            {!requiresOtp ? (
                                <>
                                    {showModal === 'transfer' && (
                                        <div>
                                            <label className="text-[9px] font-black text-slate-400 uppercase ml-1">Recipient Account</label>
                                            <input type="text" required value={toAccountNumber} onChange={e => setToAccountNumber(e.target.value)} className="w-full mt-1 bg-slate-50 border-2 p-4 font-bold rounded-2xl outline-none focus:border-blue-500" placeholder="ACC-XXXXXXXX" />
                                        </div>
                                    )}
                                    <div>
                                        <label className="text-[9px] font-black text-slate-400 uppercase ml-1">Amount (₹)</label>
                                        <input type="number" required value={amount} onChange={e => setAmount(e.target.value)} className="w-full mt-1 bg-slate-50 border-2 p-4 text-2xl font-black rounded-2xl outline-none focus:border-blue-500" placeholder="0.00" />
                                    </div>
                                    {showModal !== 'deposit' && (
                                        <div className="pt-2">
                                            <div className="flex justify-between items-center px-1"><label className="text-[9px] font-black text-slate-400 uppercase">Secure PIN</label><button type="button" onClick={() => {setShowModal(null); setIsResettingPin(true);}} className="text-[9px] font-black text-blue-600 uppercase hover:underline">Forgot PIN?</button></div>
                                            <input type="password" required maxLength="6" value={pin} onChange={e => setPin(e.target.value)} className="w-full mt-1 bg-slate-50 border-2 p-4 text-center text-2xl font-black tracking-[0.5em] rounded-2xl outline-none focus:border-blue-600" placeholder="******" />
                                        </div>
                                    )}
                                </>
                            ) : (
                                <div className="space-y-4">
                                    <label className="text-[10px] font-black text-orange-600 uppercase block text-center">Challenge: Enter OTP from email</label>
                                    <input type="text" required maxLength="6" value={otp} onChange={e => setOtp(e.target.value)} className="w-full bg-orange-50 border-2 p-4 text-center text-3xl font-black tracking-[0.5em] text-orange-700 rounded-2xl outline-none" placeholder="000000" />
                                </div>
                            )}
                            <button type="submit" disabled={isProcessing} className={`w-full py-5 rounded-2xl font-black uppercase text-xs text-white shadow-xl ${isProcessing ? 'bg-slate-300' : requiresOtp ? 'bg-orange-500' : 'bg-blue-700'}`}>
                                {isProcessing ? 'Working...' : requiresOtp ? 'Verify & Execute' : 'Execute ' + showModal}
                            </button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Dashboard;