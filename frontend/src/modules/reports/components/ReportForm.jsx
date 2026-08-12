import React, { useState } from "react";
import { FaFilePdf, FaCog, FaSpinner } from "react-icons/fa";
import reportService from "../services/reportService";

function ReportForm({ onReportSubmitted }) {
  const [reportType, setReportType] = useState("incident");
  const [name, setName] = useState("");
  
  // Filters
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [severity, setSeverity] = useState("");
  const [status, setStatus] = useState("");
  const [incidentType, setIncidentType] = useState("");
  const [department, setDepartment] = useState("");
  const [analyst, setAnalyst] = useState("");

  // Styling & Security
  const [watermarkText, setWatermarkText] = useState("");
  const [password, setPassword] = useState("");
  const [includeLogo, setIncludeLogo] = useState(true);
  const [includeSignature, setIncludeSignature] = useState(true);
  const [includeQrCode, setIncludeQrCode] = useState(true);

  const [generating, setGenerating] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setGenerating(true);

    const payload = {
      reportType,
      name: name.trim() || undefined,
      startDate: startDate ? new Date(startDate).toISOString() : undefined,
      endDate: endDate ? new Date(endDate).toISOString() : undefined,
      severity: severity || undefined,
      status: status || undefined,
      incidentType: incidentType || undefined,
      department: department || undefined,
      analyst: analyst || undefined,
      watermarkText: watermarkText.trim() || undefined,
      password: password || undefined,
      includeLogo,
      includeSignaturePlaceholder: includeSignature,
      includeQrCode,
    };

    try {
      await reportService.generate(payload);
      onReportSubmitted("Report compilation queued successfully!");
      setName("");
      setWatermarkText("");
      setPassword("");
    } catch (error) {
      alert("Failed to queue report generation: " + (error.response?.data?.message || error.message));
    } finally {
      setGenerating(false);
    }
  };

  const inputClass = "w-full h-16 px-5 py-4 border border-slate-800 rounded-2xl bg-slate-950 hover:bg-slate-900/60 text-white text-base placeholder-slate-500 focus:outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-500/10 transition-all duration-200 select-dropdown-white";
  const labelClass = "block text-[15px] font-semibold text-sky-400 mb-2";

  return (
    <form onSubmit={handleSubmit} className="space-y-8 animate-fade-in">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        
        {/* Left Card: Report Setup & Filters */}
        <div className="bg-slate-900 p-10 rounded-[24px] border border-slate-800 shadow-[0_20px_50px_rgba(0,0,0,0.45)] space-y-6">
          <h3 className="text-lg font-bold text-white flex items-center gap-2 mb-2 pb-3 border-b border-slate-800">
            <FaFilePdf className="text-sky-400" /> Report Configuration
          </h3>

          <div className="space-y-6">
            <div>
              <label className={labelClass}>Report Name (Optional)</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g., Q2 Compliance Audit Report"
                className={inputClass}
              />
            </div>

            <div>
              <label className={labelClass}>Report Type</label>
              <select
                value={reportType}
                onChange={(e) => setReportType(e.target.value)}
                className={inputClass}
              >
                <option value="incident">Incidents Report</option>
                <option value="alert">Alerts Report</option>
                <option value="audit">System Audit Logs Report</option>
                <option value="compliance">Compliance Report</option>
                <option value="dashboard">Dashboard Summary Report</option>
              </select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className={labelClass}>Start Date</label>
                <input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full h-16 px-5 py-4 border border-slate-800 rounded-2xl bg-slate-950 hover:bg-slate-900/60 text-white text-sm focus:outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-500/10 transition-all duration-200"
                />
              </div>
              <div>
                <label className={labelClass}>End Date</label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="w-full h-16 px-5 py-4 border border-slate-800 rounded-2xl bg-slate-950 hover:bg-slate-900/60 text-white text-sm focus:outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-500/10 transition-all duration-200"
                />
              </div>
            </div>

            {reportType === "incident" && (
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className={labelClass}>Severity</label>
                  <select
                    value={severity}
                    onChange={(e) => setSeverity(e.target.value)}
                    className={inputClass}
                  >
                    <option value="">All Severities</option>
                    <option value="Low">Low</option>
                    <option value="Medium">Medium</option>
                    <option value="High">High</option>
                    <option value="Critical">Critical</option>
                  </select>
                </div>
                <div>
                  <label className={labelClass}>Status</label>
                  <select
                    value={status}
                    onChange={(e) => setStatus(e.target.value)}
                    className={inputClass}
                  >
                    <option value="">All Statuses</option>
                    <option value="Open">Open</option>
                    <option value="Investigating">Investigating</option>
                    <option value="Resolved">Resolved</option>
                    <option value="Closed">Closed</option>
                  </select>
                </div>
              </div>
            )}

            {reportType === "alert" && (
              <div>
                <label className={labelClass}>Alert Severity</label>
                <select
                  value={severity}
                  onChange={(e) => setSeverity(e.target.value)}
                  className={inputClass}
                >
                  <option value="">All Severities</option>
                  <option value="Low">Low</option>
                  <option value="Medium">Medium</option>
                  <option value="High">High</option>
                </select>
              </div>
            )}
          </div>
        </div>

        {/* Right Card: Customization, Formatting, Security */}
        <div className="bg-slate-900 p-10 rounded-[24px] border border-slate-800 shadow-[0_20px_50px_rgba(0,0,0,0.45)] space-y-6">
          <h3 className="text-lg font-bold text-white flex items-center gap-2 mb-2 pb-3 border-b border-slate-800">
            <FaCog className="text-sky-400" /> Security & PDF Customization
          </h3>

          <div className="space-y-6">
            <div>
              <label className={labelClass}>Watermark Text</label>
              <input
                type="text"
                value={watermarkText}
                onChange={(e) => setWatermarkText(e.target.value)}
                placeholder="e.g., CONFIDENTIAL, DRAFT, INTERNAL ONLY"
                className={inputClass}
              />
            </div>

            <div>
              <label className={labelClass}>PDF Password (For Encryption)</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Leave blank for no password"
                className={inputClass}
              />
            </div>

            <div className="space-y-4 pt-4 border-t border-slate-800">
              <label className="flex items-center gap-3 text-sm text-slate-400 font-semibold cursor-pointer hover:text-white transition-colors duration-200">
                <input
                  type="checkbox"
                  checked={includeLogo}
                  onChange={(e) => setIncludeLogo(e.target.checked)}
                  className="w-5 h-5 text-sky-500 bg-slate-950 border-slate-800 rounded focus:ring-0 focus:ring-offset-0 checked:bg-sky-500"
                />
                Include Company Logo in Header
              </label>

              <label className="flex items-center gap-3 text-sm text-slate-400 font-semibold cursor-pointer hover:text-white transition-colors duration-200">
                <input
                  type="checkbox"
                  checked={includeQrCode}
                  onChange={(e) => setIncludeQrCode(e.target.checked)}
                  className="w-5 h-5 text-sky-500 bg-slate-950 border-slate-800 rounded focus:ring-0 focus:ring-offset-0 checked:bg-sky-500"
                />
                Include QR Code Verification Placeholder
              </label>

              <label className="flex items-center gap-3 text-sm text-slate-400 font-semibold cursor-pointer hover:text-white transition-colors duration-200">
                <input
                  type="checkbox"
                  checked={includeSignature}
                  onChange={(e) => setIncludeSignature(e.target.checked)}
                  className="w-5 h-5 text-sky-500 bg-slate-950 border-slate-800 rounded focus:ring-0 focus:ring-offset-0 checked:bg-sky-500"
                />
                Include Digital Signature Placeholder
              </label>
            </div>
          </div>
        </div>

      </div>

      {/* Trigger Button */}
      <div className="flex justify-end pt-4 gap-4">
        {/* Cancel Button */}
        <button
          type="button"
          onClick={() => window.history.back()}
          className="h-[52px] px-6 border border-slate-700 rounded-[14px] text-sm font-semibold text-slate-400 hover:bg-slate-800 hover:text-white transition-all duration-200"
        >
          Cancel
        </button>

        {/* Primary Button */}
        <button
          type="submit"
          disabled={generating}
          className="h-[52px] px-8 bg-gradient-to-r from-sky-500 to-blue-600 text-white font-bold rounded-[14px] shadow-[0_8px_20px_rgba(14,165,233,0.25)] hover:brightness-110 active:scale-[0.98] transition-all duration-200 flex items-center gap-2 disabled:opacity-50"
        >
          {generating ? (
            <>
              <FaSpinner className="animate-spin" /> Compiling PDF...
            </>
          ) : (
            <>
              <FaFilePdf /> Queue PDF Generation
            </>
          )}
        </button>
      </div>

    </form>
  );
}

export default ReportForm;
