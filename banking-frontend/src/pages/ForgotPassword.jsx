import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Mail, Lock, ArrowLeft, ShieldCheck, RefreshCcw, Eye, EyeOff } from 'lucide-react';
import API from '../api';

const ForgotPassword = () => {
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState('');
    const [otp, setOtp] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [resendTimer, setResendTimer] = useState(0);

    const navigate = useNavigate();

    useEffect(() => {
        if (resendTimer > 0) {
            const timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
            return () => clearTimeout(timer);
        }
    }, [resendTimer]);

    const handleRequestOtp = async (e) => {
        if (e) e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await API.post(`/auth/forgot-password?email=${email}`);
            setStep(2);
            setResendTimer(30);
        } catch (err) { setError("Account not found."); }
        finally { setLoading(false); }
    };

    const handleVerifyOtp = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            // 🔥 REAL VERIFICATION: Ask the server if this OTP is correct
            await API.post(`/auth/verify-reset-otp?email=${email}&otp=${otp}`);
            setStep(3); // Only move forward if server says YES
        } catch (err) {
            setError("Invalid verification code. Please check your email.");
        } finally { setLoading(false); }
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        if (newPassword !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }
        setLoading(true);
        try {
            // Note: Since verifyOtpOnly deletes the OTP after use,
            // the backend reset-password logic should be adjusted
            // if it tries to verify it a second time.
            // (Standard approach: Step 2 returns a 'token' to use in Step 3)
            await API.post(`/auth/reset-password?email=${email}&otp=${otp}&newPassword=${newPassword}`);
            alert("Success! Password updated.");
            navigate('/login');
        } catch (err) { setError("Session expired. Please start over."); }
        finally { setLoading(false); }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
            <div className="bg-white p-10 rounded-[2.5rem] shadow-xl w-full max-w-md">
                <Link to="/login" className="flex items-center gap-2 text-[10px] font-black text-slate-400 mb-6 hover:text-blue-600 uppercase tracking-widest transition-colors">
                    <ArrowLeft size={14}/> Back to Login
                </Link>

                <h2 className="text-3xl font-black text-slate-800 tracking-tighter mb-2 italic">SECURE RESET</h2>
                <p className="text-slate-400 text-[10px] font-bold uppercase tracking-widest mb-8">Step {step} of 3</p>

                {error && <div className="mb-6 p-4 bg-rose-50 text-rose-600 text-[10px] font-black rounded-xl border border-rose-100 uppercase">⚠️ {error}</div>}

                {step === 1 && (
                    <form onSubmit={handleRequestOtp} className="space-y-6 text-left">
                        <div>
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Registered Email</label>
                            <div className="relative mt-2">
                                <Mail className="absolute left-4 top-4 text-slate-300" size={20}/>
                                <input type="email" required value={email} onChange={e => setEmail(e.target.value)}
                                       className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 outline-none focus:border-blue-500 font-bold" placeholder="your@email.com" />
                            </div>
                        </div>
                        <button type="submit" disabled={loading} className="w-full py-5 bg-blue-700 text-white rounded-2xl font-black uppercase tracking-widest text-xs shadow-lg disabled:bg-slate-300">
                            {loading ? 'Processing...' : 'Send Reset Code'}
                        </button>
                    </form>
                )}

                {step === 2 && (
                    <form onSubmit={handleVerifyOtp} className="space-y-6">
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest block text-center mb-4">Verification Code</label>
                        <input type="text" required maxLength="6" value={otp} onChange={e => setOtp(e.target.value)}
                               className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-5 outline-none focus:border-blue-500 font-black text-center tracking-[0.5em] text-3xl text-blue-700" placeholder="000000" />
                        <button type="submit" disabled={loading} className="w-full py-5 bg-blue-700 text-white rounded-2xl font-black uppercase tracking-widest text-xs shadow-lg">
                            {loading ? 'Verifying...' : 'Verify & Continue'}
                        </button>
                        <div className="text-center pt-2">
                            {resendTimer > 0 ? (
                                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Resend in {resendTimer}s</span>
                            ) : (
                                <button type="button" onClick={handleRequestOtp} className="text-[10px] font-black text-blue-600 hover:text-blue-800 flex items-center gap-2 mx-auto uppercase tracking-widest">
                                    <RefreshCcw size={12}/> Request New Code
                                </button>
                            )}
                        </div>
                    </form>
                )}

                {step === 3 && (
                    <form onSubmit={handleResetPassword} className="space-y-5 text-left">
                        {/* NEW PASSWORD */}
                        <div>
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">New Password</label>
                            <div className="relative mt-2">
                                <Lock className="absolute left-4 top-4 text-slate-300" size={20}/>
                                <input type={showPassword ? "text" : "password"} required value={newPassword} onChange={e => setNewPassword(e.target.value)}
                                       className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 pr-12 outline-none focus:border-blue-500 font-bold" placeholder="••••••••" />
                                <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-4 top-4 text-slate-300">
                                    {showPassword ? <EyeOff size={18}/> : <Eye size={18}/>}
                                </button>
                            </div>
                        </div>

                        {/* CONFIRM PASSWORD */}
                        <div>
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Confirm Password</label>
                            <div className="relative mt-2">
                                <ShieldCheck className="absolute left-4 top-4 text-slate-300" size={20}/>
                                <input type={showPassword ? "text" : "password"} required value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)}
                                       className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 outline-none focus:border-blue-500 font-bold" placeholder="••••••••" />
                            </div>
                        </div>

                        <button type="submit" disabled={loading} className="w-full py-5 bg-emerald-600 text-white rounded-2xl font-black uppercase tracking-widest text-xs shadow-lg shadow-emerald-100">
                            {loading ? 'Saving...' : 'Finalize Update'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
};

export default ForgotPassword;