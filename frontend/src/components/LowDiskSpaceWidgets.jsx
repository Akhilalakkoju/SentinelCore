import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { motion } from "framer-motion";
import {
  FaDatabase,
  FaExclamationTriangle,
  FaCheckCircle,
  FaServer,
  FaHistory,
  FaSpinner,
  FaEye
} from "react-icons/fa";

import api from "../services/api";
import GlassCard from "./ui/GlassCard";

function LowDiskSpaceWidgets() {
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchStats = async () => {
    try {
      const response = await api.get("/dashboard/low-disk-stats");
      setStats(response.data);
      setLoading(false);
    } catch (error) {
      console.error("Failed to load low disk space stats", error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 5000);
    return () => clearInterval(interval);
  }, []);

  const getStatusBadge = (status) => {
    switch (status) {
      case "SUCCESS":
        return <span className="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2.5 py-0.5 rounded-full text-[10px] font-bold">Success</span>;
      case "FAILED":
        return <span className="bg-rose-500/10 text-rose-400 border border-rose-500/20 px-2.5 py-0.5 rounded-full text-[10px] font-bold">Failed</span>;
      case "RUNNING":
        return <span className="bg-sky-500/10 text-sky-400 border border-sky-500/20 px-2.5 py-0.5 rounded-full text-[10px] font-bold flex items-center gap-1"><FaSpinner className="animate-spin" /> Running</span>;
      default:
        return <span className="bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2.5 py-0.5 rounded-full text-[10px] font-bold">Pending</span>;
    }
  };

  if (loading || !stats) {
    return (
      <GlassCard className="p-8 text-center border border-slate-900">
        <div className="flex justify-center items-center gap-2">
          <FaSpinner className="animate-spin text-cyan-400 text-2xl" />
          <span className="text-slate-400 text-sm font-semibold">Loading Low Disk metrics...</span>
        </div>
      </GlassCard>
    );
  }

  const cards = [
    {
      title: "Total Low Disk Incidents",
      value: stats.totalLowDiskIncidents,
      icon: <FaDatabase />,
      iconBg: "bg-cyan-500/20",
      iconColor: "text-cyan-400",
    },
    {
      title: "Active Disk Incidents",
      value: stats.activeLowDiskIncidents,
      icon: <FaExclamationTriangle />,
      iconBg: "bg-rose-500/20",
      iconColor: "text-rose-400",
    },
    {
      title: "Resolved Disk Incidents",
      value: stats.resolvedLowDiskIncidents,
      icon: <FaCheckCircle />,
      iconBg: "bg-emerald-500/20",
      iconColor: "text-emerald-400",
    },
    {
      title: "Assets with Critical Usage",
      value: stats.criticalDiskAssetsCount,
      icon: <FaServer />,
      iconBg: "bg-amber-500/20",
      iconColor: "text-amber-400",
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div className="border-b border-slate-800/80 pb-3">
        <h3 className="text-xl font-bold text-white flex items-center gap-2">
          <FaDatabase className="text-cyan-400" /> Low Disk Space Playbook Metrics
        </h3>
        <p className="text-slate-500 text-xs mt-1">Real-time disk partition monitoring and auto-orchestration stats.</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
        {cards.map((card, index) => (
          <div
            key={index}
            className="relative overflow-hidden rounded-3xl p-5 bg-white/5 border border-white/10 shadow-2xl transition duration-300"
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-slate-400 text-xs font-semibold uppercase tracking-wider">{card.title}</p>
                <h1 className="mt-2 text-3xl font-bold text-white font-mono">{card.value}</h1>
              </div>
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-xl ${card.iconBg} ${card.iconColor}`}>
                {card.icon}
              </div>
            </div>
            <div className="mt-4 flex items-center gap-1.5 text-[10px] text-emerald-400 font-bold bg-emerald-500/5 px-2 py-1 rounded-lg border border-emerald-500/10 w-max">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" /> LIVE TELEMETRY
            </div>
          </div>
        ))}
      </div>

      {/* Recent Playbook Executions (Low Disk Space) */}
      <GlassCard className="p-6">
        <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-2">
          <FaHistory className="text-cyan-500" /> Recent Low Disk Space Executions
        </h4>
        {stats.recentExecutions && stats.recentExecutions.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead>
                <tr className="border-b border-slate-800 text-slate-500 font-bold uppercase tracking-wider">
                  <th className="py-3 px-4">Run ID</th>
                  <th className="py-3 px-4">Playbook</th>
                  <th className="py-3 px-4">Incident</th>
                  <th className="py-3 px-4 text-center">Status</th>
                  <th className="py-3 px-4 text-center">Progress</th>
                  <th className="py-3 px-4 text-center">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-900">
                {stats.recentExecutions.map((exec) => (
                  <tr key={exec.id} className="text-slate-300 hover:bg-slate-900/20 transition">
                    <td className="py-3 px-4 font-mono text-slate-500">#{exec.id}</td>
                    <td className="py-3 px-4 font-semibold text-white">{exec.playbookName}</td>
                    <td className="py-3 px-4">
                      {exec.incidentId ? (
                        <span
                          onClick={() => navigate(`/incidents/${exec.incidentId}`)}
                          className="cursor-pointer text-cyan-400 hover:text-cyan-300 underline font-bold"
                        >
                          {exec.incidentTitle || `Incident #${exec.incidentId}`}
                        </span>
                      ) : (
                        <span className="text-slate-500 italic">None</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-center">{getStatusBadge(exec.status)}</td>
                    <td className="py-3 px-4 text-center">
                      <div className="flex items-center justify-center gap-2">
                        <div className="w-16 bg-slate-800 h-1.5 rounded-full overflow-hidden border border-slate-900">
                          <div className="bg-cyan-500 h-full" style={{ width: `${exec.progress}%` }} />
                        </div>
                        <span className="font-mono text-[10px] text-slate-400">{exec.progress}%</span>
                      </div>
                    </td>
                    <td className="py-3 px-4 text-center">
                      <button
                        onClick={() => navigate(`/playbooks/executions/${exec.id}`)}
                        className="bg-cyan-500/10 hover:bg-cyan-500 text-cyan-400 hover:text-white border border-cyan-500/20 px-2.5 py-1 rounded-lg font-bold transition flex items-center gap-1 mx-auto"
                      >
                        <FaEye /> Logs
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-slate-500 text-xs italic py-4 text-center">No execution runs logged for low disk space playbooks.</p>
        )}
      </GlassCard>
    </div>
  );
}

export default LowDiskSpaceWidgets;
