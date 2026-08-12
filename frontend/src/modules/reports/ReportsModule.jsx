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
      <main className="ml-64 mt-16 relative min-h-screen overflow-hidden bg-slate-950 font-sans antialiased text-white select-none">

      {/* Main Content Area */}
      <div className="relative z-10 p-12 max-w-7xl mx-auto space-y-8 animate-fade-in">
        
        {/* Title Header */}
        <div className="space-y-2">
          <h1
            className="text-[56px] font-extrabold tracking-tight leading-none flex items-center gap-3"
            style={{ color: "var(--sc-text-primary)" }}
          >
            <FaFilePdf className="text-sky-500 dark:text-sky-400" /> Enterprise Reports
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
                ? "border-sky-500 text-sky-500 dark:border-sky-400 dark:text-sky-400 bg-sky-500/10 dark:bg-white/5"
                : "border-transparent text-slate-600 dark:text-slate-400 hover:text-sky-600 dark:hover:text-white hover:bg-slate-200/50 dark:hover:bg-white/5"
            }`}
          >
            <FaChartBar size={14} /> Dashboard
          </button>

          <button
            onClick={() => setActiveTab("generate")}
            className={`flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 transition-all duration-300 ${
              activeTab === "generate"
                ? "border-sky-500 text-sky-500 dark:border-sky-400 dark:text-sky-400 bg-sky-500/10 dark:bg-white/5"
                : "border-transparent text-slate-600 dark:text-slate-400 hover:text-sky-600 dark:hover:text-white hover:bg-slate-200/50 dark:hover:bg-white/5"
            }`}
          >
            <FaPlusCircle size={14} /> Generate Report
          </button>

          <button
            onClick={() => setActiveTab("history")}
            className={`flex items-center gap-2 px-6 py-3 text-sm font-bold border-b-2 transition-all duration-300 ${
              activeTab === "history"
                ? "border-sky-500 text-sky-500 dark:border-sky-400 dark:text-sky-400 bg-sky-500/10 dark:bg-white/5"
                : "border-transparent text-slate-600 dark:text-slate-400 hover:text-sky-600 dark:hover:text-white hover:bg-slate-200/50 dark:hover:bg-white/5"
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
