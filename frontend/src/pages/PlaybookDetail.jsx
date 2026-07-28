import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { motion } from "framer-motion";
import {
  FaToggleOn,
  FaToggleOff,
  FaArrowLeft,
  FaCheckCircle,
  FaExclamationCircle,
  FaSpinner,
  FaHistory,
  FaCalendarAlt,
  FaClock,
  FaListOl,
  FaInfoCircle
} from "react-icons/fa";

import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import AnimatedBackground from "../components/AnimatedBackground";
import GlassCard from "../components/ui/GlassCard";
import PageHeader from "../components/ui/PageHeader";
import TableContainer from "../components/ui/TableContainer";

import playbookService from "../services/playbookService";
import { getCurrentRole } from "../services/auth";

function PlaybookDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const role = getCurrentRole();
  const canWrite = ["ADMIN", "ANALYST"].includes(role);

  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchPlaybookDetails = async () => {
    try {
      const data = await playbookService.getPlaybookDetails(Number(id));
      setDetails(data);
      setLoading(false);
    } catch (err) {
      console.error(err);
      setError("Failed to load playbook details.");
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPlaybookDetails();
    const interval = setInterval(fetchPlaybookDetails, 4000);
    return () => clearInterval(interval);
  }, [id]);

  const handleToggleStatus = async () => {
    if (!canWrite || !details) return;
    try {
      await playbookService.togglePlaybookStatus(details.id);
      fetchPlaybookDetails();
    } catch (err) {
      console.error(err);
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case "SUCCESS":
        return <span className="flex items-center gap-1 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2.5 py-1 rounded-full text-xs font-bold"><FaCheckCircle /> Success</span>;
      case "FAILED":
        return <span className="flex items-center gap-1 bg-rose-500/10 text-rose-400 border border-rose-500/20 px-2.5 py-1 rounded-full text-xs font-bold"><FaExclamationCircle /> Failed</span>;
      case "RUNNING":
        return <span className="flex items-center gap-1 bg-sky-500/10 text-sky-400 border border-sky-500/20 px-2.5 py-1 rounded-full text-xs font-bold"><FaSpinner className="animate-spin" /> Running</span>;
      case "PENDING":
        return <span className="flex items-center gap-1 bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2.5 py-1 rounded-full text-xs font-bold">Pending</span>;
      default:
        return <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-slate-700 text-white">{status}</span>;
    }
  };

  if (loading) {
    return (
      <>
        <Navbar />
        <Sidebar />
        <main className="ml-64 mt-16 min-h-screen bg-slate-950 flex items-center justify-center">
          <div className="flex flex-col items-center gap-4">
            <div className="w-12 h-12 border-4 border-cyan-400 border-t-transparent rounded-full animate-spin"></div>
            <p className="text-slate-400 font-semibold">Loading playbook metrics...</p>
          </div>
        </main>
      </>
    );
  }

  if (error || !details) {
    return (
      <>
        <Navbar />
        <Sidebar />
        <main className="ml-64 mt-16 min-h-screen bg-slate-950 flex items-center justify-center p-8">
          <GlassCard className="p-8 text-center max-w-md">
            <FaExclamationCircle className="text-red-500 text-5xl mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-white mb-2">Error</h2>
            <p className="text-slate-400 mb-6">{error || "Playbook not found."}</p>
            <button onClick={() => navigate("/playbooks")} className="w-full bg-cyan-600 hover:bg-cyan-500 text-white py-2.5 rounded-xl font-bold transition">
              Back to Playbooks
            </button>
          </GlassCard>
        </main>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <Sidebar />

      <main className="ml-64 mt-16 min-h-screen bg-slate-950 relative overflow-hidden">
        <AnimatedBackground />

        <div className="relative z-10 p-8 flex flex-col gap-6">
          {/* Back Action */}
          <motion.button
            whileHover={{ x: -4 }}
            whileTap={{ scale: 0.97 }}
            onClick={() => navigate("/playbooks")}
            className="flex items-center gap-2 text-slate-300 hover:text-white mb-2 text-xs bg-slate-900/80 hover:bg-slate-800 border border-slate-800/80 hover:border-slate-700 rounded-xl px-4 py-2.5 w-max shadow-lg backdrop-blur-xl transition duration-300 outline-none cursor-pointer"
          >
            <FaArrowLeft className="text-cyan-400" /> Back to Playbooks
          </motion.button>

          {/* Header */}
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <PageHeader
              title={details.name}
              subtitle="Detailed performance analysis, triggers, and execution history log logs"
            />
            <div className="flex items-center gap-3 bg-slate-900 border border-slate-800 p-3 rounded-2xl shadow-lg backdrop-blur-xl">
              <span className="text-xs text-slate-400 font-semibold">Playbook Status:</span>
              <button
                onClick={handleToggleStatus}
                disabled={!canWrite}
                className={`text-3xl outline-none focus:outline-none transition-colors duration-300 flex items-center ${
                  details.isActive ? "text-cyan-400" : "text-slate-600"
                } ${!canWrite ? "opacity-50 cursor-not-allowed" : "cursor-pointer"}`}
              >
                {details.isActive ? <FaToggleOn /> : <FaToggleOff />}
              </button>
              <span className={`text-xs font-bold ${details.isActive ? "text-cyan-400" : "text-slate-500"}`}>
                {details.isActive ? "ENABLED" : "DISABLED"}
              </span>
            </div>
          </div>

          {/* Summary Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <GlassCard className="p-6 flex flex-col justify-between">
              <div>
                <span className="text-slate-500 text-xs font-bold uppercase tracking-wider">Total Executions</span>
                <h2 className="text-4xl font-extrabold text-white mt-2">{details.totalExecutions}</h2>
              </div>
              <div className="text-[10px] text-slate-500 mt-4 flex items-center gap-1.5 border-t border-slate-900 pt-3">
                <FaHistory className="text-slate-500" /> System tracked actions
              </div>
            </GlassCard>

            <GlassCard className="p-6 flex flex-col justify-between">
              <div>
                <span className="text-slate-500 text-xs font-bold uppercase tracking-wider">Last Execution</span>
                <h2 className="text-lg font-semibold text-slate-300 mt-3 font-mono">
                  {details.lastExecutionTime
                    ? new Date(details.lastExecutionTime).toLocaleString()
                    : "No runs logged"}
                </h2>
              </div>
              <div className="text-[10px] text-slate-500 mt-4 flex items-center gap-1.5 border-t border-slate-900 pt-3">
                <FaCalendarAlt className="text-slate-500" /> Recent trigger time
              </div>
            </GlassCard>

            <GlassCard className="p-6 flex flex-col justify-between">
              <div>
                <span className="text-slate-500 text-xs font-bold uppercase tracking-wider">Estimated Action Time</span>
                <h2 className="text-lg font-semibold text-cyan-400 mt-3 flex items-center gap-2">
                  <FaClock /> {details.estimatedTime || "5-10 minutes"}
                </h2>
              </div>
              <div className="text-[10px] text-slate-500 mt-4 flex items-center gap-1.5 border-t border-slate-900 pt-3">
                <FaInfoCircle className="text-slate-500" /> Orchestration SLA target
              </div>
            </GlassCard>
          </div>

          {/* Trigger Conditions details */}
          <GlassCard className="p-6">
            <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-2">
              <FaInfoCircle className="text-cyan-500" /> Playbook Trigger Configuration
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs bg-slate-950/60 border border-slate-900 rounded-2xl p-5 leading-relaxed font-mono">
              <div>
                <div className="text-slate-500 font-bold uppercase tracking-wider mb-1">Trigger Class</div>
                <span className="text-cyan-400 font-bold bg-cyan-500/10 border border-cyan-500/20 px-2 py-0.5 rounded">
                  {details.triggerType}
                </span>
              </div>
              <div>
                <div className="text-slate-500 font-bold uppercase tracking-wider mb-1">Trigger Value</div>
                <span className="text-emerald-400 font-bold bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded">
                  {details.triggerValue || "ANY"}
                </span>
              </div>
              {details.conditionsJson && details.conditionsJson !== "{}" && (
                <div className="md:col-span-2 border-t border-slate-900 pt-4">
                  <div className="text-slate-500 font-bold uppercase tracking-wider mb-1">JSON Operational Conditions</div>
                  <pre className="text-slate-300 font-mono text-[11px] whitespace-pre-wrap bg-slate-950 p-3 rounded-lg border border-slate-900 mt-1">{details.conditionsJson}</pre>
                </div>
              )}
            </div>
          </GlassCard>

          {/* Recent Incidents and Execution logs */}
          <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
            {/* Recent Incidents */}
            <GlassCard className="p-6 flex flex-col justify-between">
              <div>
                <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-2">
                  <FaListOl className="text-cyan-500" /> Recent Playbook Incidents
                </h4>
                {details.recentIncidents && details.recentIncidents.length > 0 ? (
                  <div className="divide-y divide-slate-900 space-y-3">
                    {details.recentIncidents.map((incident) => (
                      <div
                        key={incident.id}
                        onClick={() => navigate(`/incidents/${incident.id}`)}
                        className="pt-3 first:pt-0 cursor-pointer flex justify-between items-center group"
                      >
                        <div>
                          <div className="font-semibold text-slate-200 group-hover:text-cyan-400 transition text-xs">
                            Incident #{incident.id}: {incident.title}
                          </div>
                          <div className="text-[10px] text-slate-500 mt-0.5">
                            Status: <span className="font-bold text-slate-400">{incident.status}</span> | Severity: <span className="font-bold text-slate-400">{incident.severity}</span>
                          </div>
                        </div>
                        <span className="text-[10px] text-cyan-400 font-bold border border-cyan-500/20 px-2 py-1 rounded-lg group-hover:bg-cyan-500/10 transition">
                          View Ticket
                        </span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-slate-500 text-xs italic py-6">No incidents generated by this playbook yet.</p>
                )}
              </div>
            </GlassCard>

            {/* Execution History */}
            <GlassCard className="p-6">
              <h4 className="text-sm font-bold text-white uppercase tracking-wider mb-4 flex items-center gap-2">
                <FaHistory className="text-cyan-500" /> execution Run History
              </h4>
              {details.executionHistory && details.executionHistory.length > 0 ? (
                <div className="divide-y divide-slate-900 space-y-3">
                  {details.executionHistory.map((exec) => (
                    <div
                      key={exec.id}
                      onClick={() => navigate(`/playbooks/executions/${exec.id}`)}
                      className="pt-3 first:pt-0 cursor-pointer flex justify-between items-center group"
                    >
                      <div>
                        <div className="font-semibold text-slate-200 group-hover:text-cyan-400 transition text-xs">
                          Run #{exec.id} : {exec.playbookName}
                        </div>
                        <div className="text-[10px] text-slate-500 mt-0.5">
                          Progress: <span className="font-mono font-bold text-slate-400">{exec.progress}%</span> | Started: <span className="font-mono">{exec.startedAt ? new Date(exec.startedAt).toLocaleString() : "N/A"}</span>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        {getStatusBadge(exec.status)}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-slate-500 text-xs italic py-6">No execution logs tracked for this playbook.</p>
              )}
            </GlassCard>
          </div>
        </div>
      </main>
    </>
  );
}

export default PlaybookDetail;
