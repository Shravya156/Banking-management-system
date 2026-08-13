import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { User, Mail, Lock, Phone, ArrowRight, Eye, EyeOff, ShieldCheck } from 'lucide-react';
import API from '../api';

const Register = () => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        mobileNumber: ''
    });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await API.post('/users/register', formData);
            // Pass email to verification page
            navigate('/verify-email', { state: { email: formData.email } });
        } catch (err) {
            // Check if backend returned specific validation errors (e.g., password length)
            if (typeof err.response?.data === 'object') {
                const messages = Object.values(err.response.data).join(", ");
                setError(messages);
            } else {
                setError(err.response?.data?.message || err.response?.data || "Registration failed.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-12">
            <div className="w-full max-w-md rounded-[2.5rem] bg-white p-10 shadow-xl">
                <div className="mb-8 text-center">
                    <div className="flex justify-center mb-4 text-blue-700">
                        <ShieldCheck size={48} />
                    </div>
                    <h1 className="text-3xl font-black text-slate-800 italic tracking-tighter uppercase">Join Fortis</h1>
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mt-1">Global Secure Banking</p>
                </div>

                {error && (
                    <div className="mb-6 rounded-xl bg-rose-50 p-4 text-[10px] font-black text-rose-600 border border-rose-100 uppercase animate-pulse">
                        ⚠️ {error}
                    </div>
                )}

                <form onSubmit={handleRegister} className="space-y-5 text-left">
                    <div>
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Full Name</label>
                        <div className="relative mt-1">
                            <User className="absolute left-4 top-4 text-slate-300" size={20} />
                            <input
                                type="text"
                                required
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                                className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 outline-none focus:border-blue-500 font-bold transition-all"
                                placeholder="Your Name"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Email Address</label>
                        <div className="relative mt-1">
                            <Mail className="absolute left-4 top-4 text-slate-300" size={20} />
                            <input
                                type="email"
                                required
                                onChange={(e) => setFormData({...formData, email: e.target.value})}
                                className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 outline-none focus:border-blue-500 font-bold transition-all"
                                placeholder="name@email.com"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Mobile Number</label>
                        <div className="relative mt-1">
                            <Phone className="absolute left-4 top-4 text-slate-300" size={20} />
                            <input
                                type="text"
                                required
                                onChange={(e) => setFormData({...formData, mobileNumber: e.target.value})}
                                className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 outline-none focus:border-blue-500 font-bold transition-all"
                                placeholder="9876543210"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="text-[10px] font-black text-slate-400 uppercase tracking-widest ml-1">Secure Password</label>
                        <div className="relative mt-1">
                            <Lock className="absolute left-4 top-4 text-slate-300" size={20} />
                            <input
                                type={showPassword ? "text" : "password"}
                                required
                                onChange={(e) => setFormData({...formData, password: e.target.value})}
                                className="w-full bg-slate-50 border-2 border-slate-100 rounded-2xl p-4 pl-12 pr-12 outline-none focus:border-blue-500 font-bold transition-all"
                                placeholder="••••••••"
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

                    <button
                        type="submit"
                        disabled={loading}
                        className={`w-full py-5 rounded-2xl font-black uppercase tracking-widest text-xs text-white shadow-lg transition-all active:scale-95 flex items-center justify-center gap-2 ${
                            loading ? 'bg-slate-300 cursor-not-allowed' : 'bg-blue-700 hover:bg-blue-800 shadow-blue-200'
                        }`}
                    >
                        {loading ? 'Initializing...' : 'Create Account'}
                        {!loading && <ArrowRight size={16} />}
                    </button>
                </form>

                <p className="mt-8 text-center text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                    Already have an account?{' '}
                    <Link to="/login" className="text-blue-600 hover:text-blue-800">
                        Sign In
                    </Link>
                </p>
            </div>
        </div>
    );
};

export default Register;