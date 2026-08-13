import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { CheckCircle, RefreshCcw, ArrowLeft } from 'lucide-react';
import API from '../api';

const VerifyEmail = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [otp, setOtp] = useState('');
    const [error, setError] = useState('');
    const [resendTimer, setResendTimer] = useState(0);
    const email = location.state?.email || "";

    useEffect(() => {
        if (resendTimer > 0) {
            const timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
            return () => clearTimeout(timer);
        }
    }, [resendTimer]);

    const handleVerify = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await API.post(`/users/verify-registration?email=${email}&otp=${otp}`);
            alert("Verification successful!");
            navigate('/login');
        } catch (err) {
            setError(err.response?.data || "Invalid or expired code.");
        }
    };

    const handleResend = async () => {
        try {
            await API.post(`/users/resend-verification?email=${email}`);
            setResendTimer(60);
            alert("New code sent!");
        } catch (err) {
            setError("Failed to resend code.");
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
            <div className="w-full max-w-md rounded-[2.5rem] bg-white p-10 shadow-xl text-center">
                <div className="mb-6 flex justify-center text-blue-600"><CheckCircle size={60} /></div>
                <h2 className="text-2xl font-black text-slate-800 uppercase italic">Verify Identity</h2>
                <p className="mt-2 text-xs font-bold text-slate-400 uppercase tracking-widest">Sent to: {email}</p>

                {error && <div className="mt-4 p-3 bg-rose-50 text-rose-600 text-[10px] font-black rounded-xl uppercase">{error}</div>}

                <form onSubmit={handleVerify} className="mt-8 space-y-6">
                    <input type="text" maxLength="6" value={otp} onChange={(e) => setOtp(e.target.value)}
                           className="w-full rounded-2xl border-2 border-slate-100 bg-slate-50 py-4 text-center text-3xl font-black tracking-[0.5em] outline-none focus:border-blue-500" placeholder="000000" required />
                    <button type="submit" className="w-full rounded-2xl bg-blue-700 py-4 text-xs font-black uppercase tracking-widest text-white shadow-lg">Activate Account</button>
                </form>

                <div className="mt-8">
                    {resendTimer > 0 ? (
                        <p className="text-[10px] font-bold text-slate-400 uppercase">Resend available in {resendTimer}s</p>
                    ) : (
                        <button onClick={handleResend} className="flex items-center gap-2 mx-auto text-[10px] font-black text-blue-600 uppercase hover:underline">
                            <RefreshCcw size={12}/> Didn't receive code? Resend
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default VerifyEmail;