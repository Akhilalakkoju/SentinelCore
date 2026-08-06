import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaPlus, FaFlask } from "react-icons/fa";

import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import AnimatedBackground from "../components/AnimatedBackground";

import GlassCard from "../components/ui/GlassCard";
import PageHeader from "../components/ui/PageHeader";
import PrimaryButton from "../components/ui/PrimaryButton";
import ModernInput from "../components/ui/ModernInput";
import ModernSelect from "../components/ui/ModernSelect";

import {
    getAlertRules,
    deleteAlertRule,
} from "../services/alertRuleService";
import AddAlertRuleModal from "../components/AddAlertRuleModal";
import TestAlertEngineModal from "../components/TestAlertEngineModal";

function AlertRuleList() {

    const navigate = useNavigate();

    const role = localStorage.getItem("role");
    const canEdit = role === "ADMIN" || role === "ANALYST";

    const [rules, setRules] = useState([]);
    const [search, setSearch] = useState("");
    const [eventFilter, setEventFilter] = useState("");
    const [severityFilter, setSeverityFilter] = useState("");
    const [statusFilter, setStatusFilter] = useState("");
    const [addRuleModalOpen, setAddRuleModalOpen] = useState(false);
    const [testModalOpen, setTestModalOpen] = useState(false);

    useEffect(() => {
        loadRules();
    }, []);

    const loadRules = async () => {

        try {

            const res = await getAlertRules();

            setRules(res.data);

        } catch (err) {

            console.error(err);
            alert("Failed to Load Alert Rules");

        }

    };

    const handleDelete = async (id) => {

        const confirmDelete = window.confirm(
            "Are you sure you want to delete this Alert Rule?"
        );

        if (!confirmDelete) return;

        try {

            await deleteAlertRule(id);

            alert("Alert Rule Deleted Successfully");

            loadRules();

        } catch (error) {

            console.error(error);

            alert("Failed to Delete Alert Rule");

        }

    };

    const filteredRules = rules.filter((rule) => {

    const matchesSearch =
        rule.name.toLowerCase().includes(search.toLowerCase());

    const matchesEvent =
        eventFilter === "" || rule.eventType === eventFilter;

    const matchesSeverity =
        severityFilter === "" || rule.severity === severityFilter;

    const matchesStatus =
        statusFilter === "" ||
        String(rule.enabled) === statusFilter;

    return (
        matchesSearch &&
        matchesEvent &&
        matchesSeverity &&
        matchesStatus
    );

});

    return (
        <>
            <Navbar />
            <Sidebar />

            <main className="ml-64 mt-16 min-h-screen bg-slate-950 relative overflow-hidden">

                <AnimatedBackground />

                <div className="relative z-10 p-8">

                    <PageHeader
                        title="Alert Rules"
                        subtitle="Manage automatic alert generation rules"
                    >
                        {canEdit && (
                            <div className="flex gap-4">
                                <PrimaryButton
                                    onClick={() => setTestModalOpen(true)}
                                    className="bg-gradient-to-r from-purple-600 to-indigo-600 text-white flex items-center gap-2 hover:scale-105 transition"
                                >
                                    <FaFlask className="text-sm" /> Test Alert Engine
                                </PrimaryButton>

                                <PrimaryButton
                                    onClick={() => setAddRuleModalOpen(true)}
                                    className="bg-sky-600 hover:bg-sky-500 text-white flex items-center gap-2 shadow-lg shadow-sky-500/20"
                                >
                                    <FaPlus className="text-sm" /> Add Alert Rule
                                </PrimaryButton>
                            </div>
                        )}
                    </PageHeader>

                    <div className="flex flex-wrap items-end gap-4 mb-6">

                        <div className="flex-1 min-w-[250px]">
                            <ModernInput
                                label="Search Rule"
                                placeholder="Search by Rule Name..."
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                            />
                        </div>

                        <div className="w-52">
                            <ModernSelect
                                label="Event"
                                value={eventFilter}
                                onChange={(e) => setEventFilter(e.target.value)}
                                options={[
                                    { value: "", label: "All Events" },
                                    { value: "FAILED_LOGIN", label: "FAILED_LOGIN" },
                                    { value: "PORT_SCAN", label: "PORT_SCAN" },
                                    { value: "MALWARE", label: "MALWARE" },
                                    { value: "DDOS", label: "DDOS" },
                                    { value: "SQL_INJECTION", label: "SQL_INJECTION" },
                                    { value: "XSS", label: "XSS" },
                                    { value: "BRUTE_FORCE", label: "BRUTE_FORCE" },
                                ]}
                            />
                        </div>

                        <div className="w-44">
                            <ModernSelect
                                label="Severity"
                                value={severityFilter}
                                onChange={(e) => setSeverityFilter(e.target.value)}
                                options={[
                                    { value: "", label: "All" },
                                    { value: "Critical", label: "Critical" },
                                    { value: "High", label: "High" },
                                    { value: "Medium", label: "Medium" },
                                    { value: "Low", label: "Low" },
                                ]}
                            />
                        </div>

                        <div className="w-44">
                            <ModernSelect
                                label="Status"
                                value={statusFilter}
                                onChange={(e) => setStatusFilter(e.target.value)}
                                options={[
                                    { value: "", label: "All" },
                                    { value: "true", label: "Enabled" },
                                    { value: "false", label: "Disabled" },
                                ]}
                            />
                        </div>

                        <PrimaryButton
                            onClick={() => navigate("/add-alert-rule")}
                            className="
                                bg-gradient-to-r
                                from-cyan-600
                                to-blue-600
                                hover:from-cyan-500
                                hover:to-blue-500
                                text-white
                                h-12
                            "
                        >
                            + Add Rule
                        </PrimaryButton>

                    </div>

                    <GlassCard className="p-6 overflow-x-auto">

                        <table className="w-full text-white">

                            <thead>

                                <tr className="border-b border-slate-700 text-cyan-400">

                                    <th className="text-left p-4">Rule Name</th>
                                    <th className="text-left p-4">Event</th>
                                    <th className="text-left p-4">Condition</th>
                                    <th className="text-left p-4">Threshold</th>
                                    <th className="text-left p-4">Severity</th>
                                    <th className="text-left p-4">Enabled</th>
                                    <th className="text-center p-4">Actions</th>

                                </tr>

                            </thead>

                            <tbody>

                                {filteredRules.length === 0 ? (

                                    <tr>

                                        <td
                                            colSpan="7"
                                            className="text-center p-8 text-slate-400"
                                        >
                                            No Alert Rules Found
                                        </td>

                                    </tr>

                                ) : (

                                    filteredRules.map((rule) => (

                                        <tr
                                            key={rule.id}
                                            className="border-b border-slate-800 hover:bg-slate-800/40 transition"
                                        >

                                            <td className="p-4">{rule.name}</td>

                                            <td className="p-4">
                                                {rule.eventType}
                                            </td>

                                            <td className="p-4">
                                                {rule.conditionType}
                                            </td>

                                            <td className="p-4">
                                                {rule.threshold}
                                            </td>

                                            <td className="p-4">
                                                <span
                                                    className={`
                                                        px-3 py-1 rounded-full text-sm font-semibold
                                                        ${
                                                            rule.severity === "Critical"
                                                                ? "bg-red-600"
                                                                : rule.severity === "High"
                                                                ? "bg-orange-500"
                                                                : rule.severity === "Medium"
                                                                ? "bg-yellow-500 text-black"
                                                                : "bg-green-600"
                                                        }
                                                    `}
                                                >
                                                    {rule.severity}
                                                </span>
                                            </td>

                                            <td className="p-4">
                                                {rule.enabled ? (
                                                    <span className="text-green-400 font-semibold">
                                                        Enabled
                                                    </span>
                                                ) : (
                                                    <span className="text-red-400 font-semibold">
                                                        Disabled
                                                    </span>
                                                )}
                                            </td>

                                            <td className="p-4">

                                                <div className="flex justify-center gap-2">

                                                    <button
                                                        onClick={() =>
                                                            navigate(`/edit-alert-rule/${rule.id}`)
                                                        }
                                                        className="
                                                            px-4
                                                            py-2
                                                            rounded-lg
                                                            bg-yellow-500
                                                            hover:bg-yellow-400
                                                            text-black
                                                            font-semibold
                                                            transition
                                                        "
                                                    >
                                                        ✏ Edit
                                                    </button>

                                                    <button
                                                        onClick={() => handleDelete(rule.id)}
                                                        className="
                                                            px-4
                                                            py-2
                                                            rounded-lg
                                                            bg-red-600
                                                            hover:bg-red-500
                                                            text-white
                                                            font-semibold
                                                            transition
                                                        "
                                                    >
                                                        🗑 Delete
                                                    </button>

                                                </div>

                                            </td>

                                        </tr>

                                    ))

                                )}

                            </tbody>

                        </table>

                    </GlassCard>

            <AddAlertRuleModal
                open={addRuleModalOpen}
                onClose={() => setAddRuleModalOpen(false)}
                onSuccess={loadRules}
            />
            <TestAlertEngineModal
                open={testModalOpen}
                onClose={() => setTestModalOpen(false)}
            />
        </div>

    </main>

        </>
    );
}

export default AlertRuleList;