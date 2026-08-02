import { useState } from "react";
import { X, Settings } from "lucide-react";
import ModernSelect from "./ui/ModernSelect";
import ModernInput from "./ui/ModernInput";
import PrimaryButton from "./ui/PrimaryButton";
import { createAlertRule } from "../services/alertRuleService";

export default function AddAlertRuleModal({ open, onClose, onSuccess }) {
    const [rule, setRule] = useState({
        name: "",
        description: "",
        eventType: "FAILED_LOGIN",
        conditionType: "GREATER_THAN",
        threshold: 5,
        severity: "High",
        enabled: true,
    });

    if (!open) return null;

    const handleSave = async () => {
        if (!rule.name.trim()) {
            alert("Please enter a Rule Name");
            return;
        }

        try {
            await createAlertRule(rule);
            alert("Alert Rule Created Successfully!");
            setRule({
                name: "",
                description: "",
                eventType: "FAILED_LOGIN",
                conditionType: "GREATER_THAN",
                threshold: 5,
                severity: "High",
                enabled: true,
            });
            onSuccess();
            onClose();
        } catch (error) {
            console.error("Failed to save alert rule", error);
            alert("Failed to save alert rule");
        }
    };

    return (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50">
            <div className="bg-slate-900 border border-cyan-500/30 rounded-2xl shadow-2xl w-[650px] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-150">
                {/* Header */}
                <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/40">
                    <div className="flex items-center gap-3">
                        <Settings className="text-cyan-400 animate-spin-slow" size={24} />
                        <h2 className="text-xl font-bold text-white">Create Alert Rule</h2>
                    </div>
                    <button onClick={onClose} className="text-slate-400 hover:text-red-400 transition">
                        <X size={22} />
                    </button>
                </div>

                {/* Body */}
                <div className="p-6 space-y-5">
                    <div className="grid grid-cols-2 gap-4">
                        <div className="col-span-2">
                            <ModernInput
                                label="Rule Name"
                                value={rule.name}
                                onChange={(e) => setRule({ ...rule, name: e.target.value })}
                                placeholder="Enter rule name..."
                            />
                        </div>

                        <ModernSelect
                            label="Event Type"
                            value={rule.eventType}
                            onChange={(e) => setRule({ ...rule, eventType: e.target.value })}
                            options={[
                                "FAILED_LOGIN",
                                "PORT_SCAN",
                                "MALWARE",
                                "DDOS",
                                "SQL_INJECTION",
                                "XSS",
                                "BRUTE_FORCE",
                            ]}
                        />

                        <ModernSelect
                            label="Condition"
                            value={rule.conditionType}
                            onChange={(e) => setRule({ ...rule, conditionType: e.target.value })}
                            options={[
                                { value: "GREATER_THAN", label: "Greater Than (>)" },
                                { value: "LESS_THAN", label: "Less Than (<)" },
                                { value: "EQUAL", label: "Equal (=)" },
                            ]}
                        />

                        <ModernInput
                            label="Threshold Value"
                            type="number"
                            value={rule.threshold}
                            onChange={(e) => setRule({ ...rule, threshold: Number(e.target.value) })}
                        />

                        <ModernSelect
                            label="Severity"
                            value={rule.severity}
                            onChange={(e) => setRule({ ...rule, severity: e.target.value })}
                            options={["Critical", "High", "Medium", "Low"]}
                        />

                        <div className="col-span-2">
                            <ModernSelect
                                label="Status"
                                value={rule.enabled.toString()}
                                onChange={(e) => setRule({ ...rule, enabled: e.target.value === "true" })}
                                options={[
                                    { value: "true", label: "Enabled" },
                                    { value: "false", label: "Disabled" },
                                ]}
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-cyan-400 text-xs font-semibold mb-2">Description</label>
                        <textarea
                            rows={3}
                            value={rule.description}
                            onChange={(e) => setRule({ ...rule, description: e.target.value })}
                            placeholder="Enter description explaining this detection logic..."
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
                        Save Alert Rule
                    </PrimaryButton>
                </div>
            </div>
        </div>
    );
}
