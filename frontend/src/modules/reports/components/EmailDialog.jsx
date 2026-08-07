import React, { useState } from "react";
import { FaTimes, FaEnvelope, FaSpinner } from "react-icons/fa";
import reportService from "../services/reportService";

function EmailDialog({ isOpen, onClose, reportId, reportName, onEmailSent }) {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSend = async (e) => {
    e.preventDefault();
    if (!email.trim()) return;

    setLoading(true);
    try {
      await reportService.email(reportId, email);
      onEmailSent("Report email sent successfully!");
      onClose();
      setEmail("");
    } catch (error) {
      alert("Failed to send email. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm p-4 animate-fade-in">
      <div className="w-full max-w-md bg-slate-900 rounded-[24px] shadow-[0_20px_50px_rgba(0,0,0,0.6)] overflow-hidden p-8 border border-slate-800 space-y-6">
        
        <div className="flex justify-between items-center pb-3 border-b border-slate-800">
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <FaEnvelope className="text-sky-400" /> Email Security Report
          </h3>
          <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors duration-200">
            <FaTimes size={18} />
          </button>
        </div>

        <form onSubmit={handleSend} className="space-y-6">
          <p className="text-sm text-slate-400 leading-relaxed">
            Send report <strong className="text-white">{reportName}</strong> to the designated security team or analyst.
          </p>

          <div>
            <label className="block text-[15px] font-semibold text-sky-400 mb-2">
              Recipient Email Address
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g., analyst@company.com"
              className="w-full h-16 px-5 py-4 border border-slate-800 rounded-2xl bg-slate-950 hover:bg-slate-900/60 text-white text-base placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-500/10 transition-all duration-200"
            />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="h-[52px] px-5 border border-slate-700 rounded-[14px] text-sm font-semibold text-slate-400 hover:bg-slate-800 hover:text-white transition-all duration-200"
            >
              Cancel
            </button>
            
            <button
              type="submit"
              disabled={loading}
              className="h-[52px] px-6 bg-gradient-to-r from-sky-500 to-blue-600 text-white font-bold rounded-[14px] shadow-[0_8px_20px_rgba(14,165,233,0.25)] hover:brightness-110 active:scale-[0.98] transition-all duration-200 flex items-center gap-2 disabled:opacity-50"
            >
              {loading ? (
                <>
                  <FaSpinner className="animate-spin" /> Sending...
                </>
              ) : (
                "Send Email"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default EmailDialog;
