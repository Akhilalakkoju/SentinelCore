import { useState } from "react";
import { X, ShieldAlert } from "lucide-react";
import api from "../services/api";
import ModernSelect from "./ui/ModernSelect";
import PrimaryButton from "./ui/PrimaryButton";

export default function AddThreatModal({ open, onClose, onSuccess }) {
    const [threatData, setThreatData] = useState({
        title: "SQL Injection",
        severity: "Medium",
        source: "Firewall",
        status: "Open",
    });

    if (!open) return null;

    const handleSave = async () => {
        try {
            await api.post("/threats", threatData);
            alert("Threat Added Successfully!");
            setThreatData({
                title: "SQL Injection",
                severity: "Medium",
                source: "Firewall",
                status: "Open",
            });
            onSuccess();
            onClose();
        } catch (error) {
            console.error("Failed to save threat", error);
            alert("Failed to save threat");
        }
    };

    return (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="bg-slate-900 border border-cyan-500/30 rounded-2xl shadow-2xl w-[600px] flex flex-col overflow-hidden">
                {/* Header */}
                <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/40">
                    <div className="flex items-center gap-3">
                        <ShieldAlert className="text-cyan-400" size={24} />
                        <h2 className="text-xl font-bold text-white">Add Cyber Threat</h2>
                    </div>
                    <button onClick={onClose} className="text-slate-400 hover:text-red-400 transition">
                        <X size={22} />
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 space-y-5">
                    <div className="grid grid-cols-2 gap-4">
                        <div className="col-span-2">
                            <ModernSelect
                                label="Threat Title"
                                value={threatData.title}
                                onChange={(e) => setThreatData({ ...threatData, title: e.target.value })}
                                options={[
                                    "SQL Injection",
                                    "XSS Attack",
                                    "Ransomware",
                                    "DDoS Attack",
                                    "Malware",
                                    "Phishing",
                                    "Brute Force",
                                    "Zero-Day Exploit",
                                    "Privilege Escalation",
                                ]}
                            />
                        </div>

                        <ModernSelect
                            label="Severity"
                            value={threatData.severity}
                            onChange={(e) => setThreatData({ ...threatData, severity: e.target.value })}
                            options={["Critical", "High", "Medium", "Low"]}
                        />

                        <ModernSelect
                            label="Status"
                            value={threatData.status}
                            onChange={(e) => setThreatData({ ...threatData, status: e.target.value })}
                            options={["Open", "In Progress", "Resolved"]}
                        />

                        <div className="col-span-2">
                            <ModernSelect
                                label="Threat Source"
                                value={threatData.source}
                                onChange={(e) => setThreatData({ ...threatData, source: e.target.value })}
                                options={[
                                    "Firewall",
                                    "IDS",
                                    "IPS",
                                    "SIEM",
                                    "Antivirus",
                                    "Endpoint Security",
                                    "Email Gateway",
                                    "Threat Intelligence Feed",
                                    "Cloud Security",
                                    "Manual Investigation",
                                ]}
                            />
                        </div>
                    </div>
                </div>

                {/* Footer */}
                <div className="border-t border-slate-800 px-6 py-4 flex justify-end gap-3 bg-slate-950/20">
                    <button
                        onClick={onClose}
                        className="px-5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium transition"
                    >
                        Cancel
                    </button>
                    <PrimaryButton
                        onClick={handleSave}
                        className="bg-gradient-to-r from-cyan-600 to-blue-600 hover:from-cyan-500 hover:to-blue-500 text-white shadow-xl px-6"
                    >
                        🚀 Add Threat
                    </PrimaryButton>
                </div>
            </div>
        </div>
    );
}
