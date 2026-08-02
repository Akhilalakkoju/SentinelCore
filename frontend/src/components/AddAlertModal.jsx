import { useState, useEffect } from "react";
import { X, Bell } from "lucide-react";
import api from "../services/api";
import ModernSelect from "./ui/ModernSelect";
import PrimaryButton from "./ui/PrimaryButton";

export default function AddAlertModal({ open, onClose, onSuccess }) {
    const [alertData, setAlertData] = useState({
        title: "Unauthorized Login",
        severity: "Medium",
        source: "Firewall",
        status: "Open",
        assetId: "",
        description: "",
    });

    const [assets, setAssets] = useState([]);

    useEffect(() => {
        const fetchAssets = async () => {
            try {
                const response = await api.get("/assets");
                setAssets(response.data);
            } catch (error) {
                console.error("Failed to load assets", error);
            }
        };
        if (open) {
            fetchAssets();
        }
    }, [open]);

    if (!open) return null;

    const handleSave = async () => {
        if (!alertData.title) {
            alert("Please select or enter an Alert Title");
            return;
        }

        try {
            const payload = {
                title: alertData.title,
                severity: alertData.severity,
                source: alertData.source,
                status: alertData.status,
                description: alertData.description,
                asset: alertData.assetId ? { id: parseInt(alertData.assetId) } : null,
            };

            await api.post("/alerts", payload);
            alert("Alert Created Successfully!");
            setAlertData({
                title: "Unauthorized Login",
                severity: "Medium",
                source: "Firewall",
                status: "Open",
                assetId: "",
                description: "",
            });
            onSuccess();
            onClose();
        } catch (error) {
            console.error("Failed to save alert", error);
            alert("Failed to save alert");
        }
    };

    const assetOptions = [
        { value: "", label: "None (General Alert)" },
        ...assets.map((asset) => ({
            value: String(asset.id),
            label: `${asset.assetName || asset.hostname} (${asset.ipAddress})`,
        })),
    ];

    return (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="bg-slate-900 border border-cyan-500/30 rounded-2xl shadow-2xl w-[650px] max-h-[90vh] flex flex-col overflow-hidden">
                {/* Header */}
                <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/40">
                    <div className="flex items-center gap-3">
                        <Bell className="text-cyan-400" size={24} />
                        <h2 className="text-xl font-bold text-white">Create Security Alert</h2>
                    </div>
                    <button onClick={onClose} className="text-slate-400 hover:text-red-400 transition">
                        <X size={22} />
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 overflow-y-auto space-y-5 flex-1 scrollbar-thin scrollbar-thumb-slate-800">
                    <div className="grid grid-cols-2 gap-4">
                        <ModernSelect
                            label="Alert Title"
                            value={alertData.title}
                            onChange={(e) => setAlertData({ ...alertData, title: e.target.value })}
                            options={[
                                "Unauthorized Login",
                                "Malware Detected",
                                "SQL Injection",
                                "XSS Attack",
                                "DDoS Attack",
                                "Phishing Attempt",
                                "Privilege Escalation",
                                "Suspicious File",
                                "Port Scan",
                                "Brute Force Attack",
                            ]}
                        />

                        <ModernSelect
                            label="Severity"
                            value={alertData.severity}
                            onChange={(e) => setAlertData({ ...alertData, severity: e.target.value })}
                            options={["Critical", "High", "Medium", "Low"]}
                        />

                        <ModernSelect
                            label="Source"
                            value={alertData.source}
                            onChange={(e) => setAlertData({ ...alertData, source: e.target.value })}
                            options={[
                                "Firewall",
                                "IDS",
                                "IPS",
                                "SIEM",
                                "EDR",
                                "XDR",
                                "Antivirus",
                                "Threat Intelligence Feed",
                                "Cloud Security",
                                "Manual Investigation",
                            ]}
                        />

                        <ModernSelect
                            label="Status"
                            value={alertData.status}
                            onChange={(e) => setAlertData({ ...alertData, status: e.target.value })}
                            options={["Open", "Investigating", "Resolved"]}
                        />
                    </div>

                    <div className="w-full">
                        <ModernSelect
                            label="Target Asset"
                            value={alertData.assetId}
                            onChange={(e) => setAlertData({ ...alertData, assetId: e.target.value })}
                            options={assetOptions}
                        />
                    </div>

                    <div>
                        <label className="block text-cyan-400 text-xs font-semibold mb-2">Description</label>
                        <textarea
                            rows={4}
                            value={alertData.description}
                            onChange={(e) => setAlertData({ ...alertData, description: e.target.value })}
                            placeholder="Enter alert description and trace information..."
                            className="w-full rounded-xl bg-slate-800 border border-slate-700 px-4 py-3 text-white placeholder:text-slate-500 focus:border-cyan-400 focus:ring-2 focus:ring-cyan-500/30 outline-none resize-none"
                        />
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
                        🚨 Create Alert
                    </PrimaryButton>
                </div>
            </div>
        </div>
    );
}
