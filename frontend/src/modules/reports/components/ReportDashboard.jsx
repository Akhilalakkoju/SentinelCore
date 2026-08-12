import React, { useEffect, useState } from "react";
import { FaFilePdf, FaDownload, FaDatabase, FaHistory, FaCheckCircle, FaSpinner } from "react-icons/fa";
import reportService from "../services/reportService";

function ReportDashboard({ recentReports, onTriggerTab }) {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, [recentReports]);

  const fetchStats = async () => {
    try {
      const response = await reportService.getStats();
      setStats(response.data);
    } catch (error) {
      console.error("Failed to load dashboard statistics", error);
    } finally {
      setLoading(false);
    }
  };

  const formatBytes = (bytes) => {
    if (!bytes) return "0 Bytes";
    const k = 1024;
    const sizes = ["Bytes", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i];
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <FaSpinner className="animate-spin text-sky-400 text-3xl" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Metrics Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* Metric 1 */}
        <div className="p-8 bg-slate-900 rounded-[24px] border border-slate-800 shadow-2xl flex items-center justify-between transition-transform duration-300 hover:-translate-y-1">
          <div className="space-y-2">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Generated Reports</span>
            <h3 className="text-4xl font-extrabold text-white">
              {stats?.generatedReportsCount || 0}
            </h3>
          </div>
          <div className="p-4 bg-white/5 text-sky-400 rounded-2xl border border-sky-400/20 shadow-lg shadow-sky-400/10">
            <FaFilePdf size={24} />
          </div>
        </div>

        {/* Metric 2 */}
        <div className="p-8 bg-slate-900 rounded-[24px] border border-slate-800 shadow-2xl flex items-center justify-between transition-transform duration-300 hover:-translate-y-1">
          <div className="space-y-2">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Total Downloads</span>
            <h3 className="text-4xl font-extrabold text-white">
              {stats?.downloadCount || 0}
            </h3>
          </div>
          <div className="p-4 bg-white/5 text-emerald-400 rounded-2xl border border-emerald-400/20 shadow-lg shadow-emerald-400/10">
            <FaDownload size={24} />
          </div>
        </div>

        {/* Metric 3 */}
        <div className="p-8 bg-slate-900 rounded-[24px] border border-slate-800 shadow-2xl flex items-center justify-between transition-transform duration-300 hover:-translate-y-1">
          <div className="space-y-2">
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Storage Occupied</span>
            <h3 className="text-4xl font-extrabold text-white">
              {formatBytes(stats?.storageUsageBytes)}
            </h3>
          </div>
          <div className="p-4 bg-white/5 text-cyan-400 rounded-2xl border border-cyan-400/20 shadow-lg shadow-cyan-400/10">
            <FaDatabase size={24} />
          </div>
        </div>

      </div>

      {/* Recent Activity Card */}
      <div className="bg-slate-900 rounded-[24px] border border-slate-800 shadow-[0_20px_50px_rgba(0,0,0,0.45)] overflow-hidden">
        <div className="px-8 py-5 border-b border-slate-800 flex justify-between items-center bg-slate-950/40">
          <h3 className="text-lg font-bold text-white flex items-center gap-2">
            <FaHistory className="text-sky-400" /> Recent Report Generations
          </h3>
          <button
            onClick={() => onTriggerTab("history")}
            className="text-sm font-semibold text-sky-400 hover:underline"
          >
            View All History
          </button>
        </div>

        <div className="divide-y divide-slate-800">
          {recentReports.slice(0, 5).map((report) => (
            <div key={report.id} className="px-8 py-5 flex items-center justify-between hover:bg-slate-800/40 transition-colors duration-200">
              <div className="flex items-center gap-4">
                <div className="p-2 bg-white/5 rounded-xl border border-slate-800">
                  <FaFilePdf size={18} className="text-sky-400" />
                </div>
                <div>
                  <h4 className="text-base font-bold text-white">{report.name}</h4>
                  <p className="text-xs text-slate-400 mt-0.5">
                    Type: <span className="capitalize">{report.reportType}</span> | Size: {formatBytes(report.fileSize)}
                  </p>
                </div>
              </div>
              <span className="text-sm text-slate-400">
                {new Date(report.generatedTime).toLocaleDateString()}
              </span>
            </div>
          ))}
          {recentReports.length === 0 && (
            <div className="p-8 text-center text-slate-400">
              No reports compiled yet. Go to "Generate Report" tab to start.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ReportDashboard;
