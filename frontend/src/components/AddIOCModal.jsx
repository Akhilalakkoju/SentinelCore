import { useState } from "react";
import { X, Search } from "lucide-react";
import api from "../services/api";
import ModernSelect from "./ui/ModernSelect";
import ModernInput from "./ui/ModernInput";
import PrimaryButton from "./ui/PrimaryButton";

export default function AddIOCModal({ open, onClose, onSuccess }) {
    const [iocData, setIocData] = useState({
        type: "IP Address",
        value: "",
        riskLevel: "Medium",
        source: "Firewall",
        status: "Active",
    });

    if (!open) return null;

    const handleSave = async () => {
        if (!iocData.value.trim()) {
            alert("Please enter an IOC value");
            return;
        }

        try {
            await api.post("/ioc", iocData);
            alert("IOC Added Successfully!");
            setIocData({
                type: "IP Address",
                value: "",
                riskLevel: "Medium",
                source: "Firewall",
                status: "Active",
            });
            onSuccess();
            onClose();
        } catch (error) {
            console.error("Failed to save IOC", error);
            alert("Failed to save IOC");
        }
    };

    return (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="bg-slate-900 border border-cyan-500/30 rounded-2xl shadow-2xl w-[600px] flex flex-col overflow-hidden">
                {/* Header */}
                <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/40">
                    <div className="flex items-center gap-3">
                        <Search className="text-cyan-400" size={24} />
                        <h2 className="text-xl font-bold text-white">Add IOC (Indicator of Compromise)</h2>
                    </div>
                    <button onClick={onClose} className="text-slate-400 hover:text-red-400 transition">
                        <X size={22} />
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 space-y-5">
                    <div className="grid grid-cols-2 gap-4">
                        <ModernSelect
                            label="IOC Type"
                            value={iocData.type}
                            onChange={(e) => setIocData({ ...iocData, type: e.target.value })}
                            options={[
                                "IP Address",
                                "Domain",
                                "URL",
                                "Email Address",
                                "MD5 Hash",
                                "SHA1 Hash",
                                "SHA256 Hash",
                                "Registry Key",
                                "Mutex",
                                "File Path",
                                "Process Name",
                            ]}
                        />

                        <ModernSelect
                            label="Risk Level"
                            value={iocData.riskLevel}
                            onChange={(e) => setIocData({ ...iocData, riskLevel: e.target.value })}
                            options={["Critical", "High", "Medium", "Low"]}
                        />

                        <div className="col-span-2">
                            <ModernInput
                                label="IOC Value"
                                value={iocData.value}
                                onChange={(e) => setIocData({ ...iocData, value: e.target.value })}
                                placeholder="Enter value (e.g. 192.168.1.100, badsite.com, etc.)"
                            />
                        </div>

                        <ModernSelect
                            label="Source"
                            value={iocData.source}
                            onChange={(e) => setIocData({ ...iocData, source: e.target.value })}
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
                            value={iocData.status}
                            onChange={(e) => setIocData({ ...iocData, status: e.target.value })}
                            options={["Active", "Blocked", "Investigating"]}
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
                        🚀 Save IOC
                    </PrimaryButton>
                </div>
            </div>
        </div>
    );
}
