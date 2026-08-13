import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Lock, Mail, ShieldCheck, Eye, EyeOff } from 'lucide-react';
import API from '../api';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false); // New state
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        // 🔥 1. CLEAR OLD DATA FIRST (Preventing the 401 lock)
        localStorage.clear();

        try {
            const response = await API.post('/auth/login', { email, password });

            // 2. Extract clean strings
            const { token, role } = response.data;

            // 3. Save to storage
            localStorage.setItem('token', token);
            localStorage.setItem('role', role);
            localStorage.setItem('userEmail', email);

            if (role === 'ADMIN') navigate('/admin-dashboard');
            else navigate('/dashboard');

        } catch (err) {
            // This will now show the REAL error (e.g. Password Mismatch)
            setError(err.response?.data?.message || "Login failed. Check credentials.");
        } finally {
            setLoading(false);
        }
    };
    return (
        <div className="flex min-h-screen w-full bg-white">
            {/* Left Side: Hero (Stay as is) */}
            <div className="hidden w-1/2 flex-col justify-between bg-blue-700 p-12 text-white lg:flex">
                <div className="flex items-center gap-2 text-2xl font-bold italic tracking-tighter">
                    <ShieldCheck size={32} /> FORTIS TRUST
                </div>
                <h2 className="text-5xl font-bold leading-tight">Banking that <br /> moves with you.</h2>
                <div className="text-sm text-blue-200">© 2026 Fortis Trust Bank.</div>
            </div>

            {/* Right Side: Form */}
            <div className="flex w-full items-center justify-center px-8 lg:w-1/2">
                <div className="w-full max-w-md text-left">
                    <h2 className="text-3xl font-bold text-slate-800">Welcome Back</h2>
                    <p className="mt-2 text-slate-500 mb-8">Access your secure vault.</p>

                    {error && (
                        <div className="mb-6 rounded-lg bg-red-50 p-4 text-sm text-red-600 border border-red-100 font-medium">
                            ⚠️ {error}
                        </div>
                    )}

                    <form onSubmit={handleLogin} className="space-y-6">
                        {/* EMAIL FIELD */}
                        <div>
                            <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">
                                Email Address
                            </label>
                            <div className="relative mt-2">
                                <Mail className="absolute left-4 top-4 text-slate-300" size={20} />
                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 outline-none focus:border-blue-500 font-bold transition-all"
                                    placeholder="your@email.com"
                                    required
                                />
                            </div>
                        </div>

                        {/* SINGLE PASSWORD FIELD WITH FORGOT LINK */}
                        <div>
                            <div className="flex items-center justify-between mb-2">
                                <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">
                                    Password
                                </label>
                                <Link
                                    to="/forgot-password"
                                    className="text-[10px] font-black text-blue-600 uppercase tracking-widest hover:text-blue-800 transition-colors"
                                >
                                    Forgot Password?
                                </Link>
                            </div>
                            <div className="relative mt-1">
                                <Lock className="absolute left-4 top-4 text-slate-300" size={20} />
                                <input
                                    type={showPassword ? "text" : "password"}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 pr-12 outline-none focus:border-blue-500 font-bold transition-all"
                                    placeholder="••••••••"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-4 top-4 text-slate-300 hover:text-slate-600"
                                >
                                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                                </button>
                            </div>
                        </div>

                        {/* SIGN IN BUTTON */}
                        <button
                            type="submit"
                            disabled={loading}
                            className={`w-full py-5 rounded-2xl font-black uppercase tracking-widest text-xs text-white shadow-lg transition-all active:scale-95 ${
                                loading ? 'bg-slate-400 cursor-not-allowed' : 'bg-blue-700 hover:bg-blue-800 shadow-blue-200'
                            }`}
                        >
                            {loading ? 'Verifying...' : 'Sign In'}
                        </button>
                    </form>

                    <p className="mt-8 text-center text-sm text-slate-600">
                        New user? <Link to="/register" className="font-bold text-blue-600 hover:underline">Register here</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;
