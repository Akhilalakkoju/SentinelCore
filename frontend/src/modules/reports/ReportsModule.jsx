import React, { useState, useEffect } from "react";
import { FaFilePdf, FaChartBar, FaPlusCircle, FaHistory } from "react-icons/fa";
import ReportDashboard from "./components/ReportDashboard";
import ReportForm from "./components/ReportForm";
import ReportHistory from "./components/ReportHistory";
import reportService from "./services/reportService";
import Navbar from "../../components/Navbar";
import Sidebar from "../../components/Sidebar";

function ReportsModule() {
  const [activeTab, setActiveTab] = useState("dashboard");
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const response = await reportService.getHistory();
      setReports(response.data);
    } catch (error) {
      console.error("Failed to retrieve report history", error);
    } finally {
      setLoading(false);
    }
  };

  const handleReportSubmitted = (successMessage) => {
    alert(successMessage);
    fetchHistory();
    setActiveTab("history"); // automatically switch to history tab
  };

  return (
    <>
      <Navbar />
      <Sidebar />
      <main className="ml-64 mt-16 relative min-h-[calc(100vh-4rem)] overflow-hidden bg-slate-50 dark:bg-[#081225] font-sans antialiased text-slate-900 dark:text-white select-none">
      
      {/* Premium Background Grid & Glows (Dark Mode Only) */}
      <div className="absolute inset-0 bg-gradient-to-b from-[#081225] to-[#050B18] pointer-events-none z-0 hidden dark:block" />
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_left_top,rgba(0,212,255,0.10),transparent_50%)] pointer-events-none z-0 hidden dark:block" />
      <div className="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-[size:32px_32px] pointer-events-none z-0 hidden dark:block" />
      
      {/* Subtle glowing dots (Dark Mode Only) */}
      <div className="absolute top-1/4 left-1/3 w-1.5 h-1.5 bg-[#00D4FF] rounded-full filter blur-[2px] opacity-40 animate-pulse pointer-events-none z-0 hidden dark:block" />
      <div className="absolute top-2/3 right-1/4 w-1 h-1 bg-[#00D4FF] rounded-full filter blur-[1px] opacity-30 animate-pulse pointer-events-none z-0 hidden dark:block" />

      {/* Main Content Area */}
      <div className="relative z-10 p-12 max-w-7xl mx-auto space-y-8 animate-fade-in">
        
        {/* Title Header */}
        <div className="space-y-2">
          <h1
            className="text-[56px] font-extrabold tracking-tight leading-none flex items-center gap-3"
            style={{ color: "var(--sc-text-primary)" }}
          >
            <FaFilePdf className="text-sky-500 dark:text-[#00D4FF]" /> Enterprise Reports
          </h1>
          <p
            className="text-[20px] font-normal"
            style={{ color: "var(--sc-text-secondary)" }}
          >
            Generate and schedule high-fidelity compliance, alert, and incident reports.
          </p>
        </div>

        {/* Tabs Menu */}
        <div className="flex border-b border-slate-200 dark:border-white/10 gap-2 pb-1">
          <button
            onClick={() => setActiveTab("dashboard")}
            className={`flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 transition-all duration-300 ${
              activeTab === "dashboard"
                ? "border-sky-500 text-sky-500 dark:border-[#00D4FF] dark:text-[#00D4FF] bg-sky-500/10 dark:bg-white/5"
                : "border-transparent text-slate-600 dark:text-[#94A3B8] hover:text-sky-600 dark:hover:text-white hover:bg-slate-200/50 dark:hover:bg-white/5"
            }`}
          >
            <FaChartBar size={14} /> Dashboard
          </button>

          <button
            onClick={() => setActiveTab("generate")}
            className={`flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 transition-all duration-300 ${
              activeTab === "generate"
                ? "border-sky-500 text-sky-500 dark:border-[#00D4FF] dark:text-[#00D4FF] bg-sky-500/10 dark:bg-white/5"
                : "border-transparent text-slate-600 dark:text-[#94A3B8] hover:text-sky-600 dark:hover:text-white hover:bg-slate-200/50 dark:hover:bg-white/5"
            }`}
          >
            <FaPlusCircle size={14} /> Generate Report
          </button>

          <button
            onClick={() => setActiveTab("history")}
            className={`flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 transition-all duration-300 ${
              activeTab === "history"
                ? "border-sky-500 text-sky-500 dark:border-[#00D4FF] dark:text-[#00D4FF] bg-sky-500/10 dark:bg-white/5"
                : "border-transparent text-slate-600 dark:text-[#94A3B8] hover:text-sky-600 dark:hover:text-white hover:bg-slate-200/50 dark:hover:bg-white/5"
            }`}
          >
            <FaHistory size={14} /> Report History
          </button>
        </div>

        {/* Render Active Tab */}
        <div className="pt-2">
          {activeTab === "dashboard" && (
            <ReportDashboard
              recentReports={reports}
              onTriggerTab={(tab) => setActiveTab(tab)}
            />
          )}
          
          {activeTab === "generate" && (
            <ReportForm onReportSubmitted={handleReportSubmitted} />
          )}

          {activeTab === "history" && (
            <ReportHistory
              reports={reports}
              onRefreshHistory={fetchHistory}
            />
          )}
        </div>

      </div>

    </main>
    </>
  );
}

export default ReportsModule;
