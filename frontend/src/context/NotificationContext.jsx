import axios from "axios";
import {
    createContext,
    useContext,
    useEffect,
    useState
} from "react";

import { toast } from "react-toastify";

import {
    connectWebSocket,
    disconnectWebSocket
} from "../services/websocket";

import { useSettings } from "./SettingsContext";


const NotificationContext = createContext();

const API_URL = "http://localhost:8080/api/notifications";


export const NotificationProvider = ({ children }) => {

    const { settings } = useSettings() || {};

    const [notifications, setNotifications] = useState([]);


    // =========================================================
    // LOAD NOTIFICATIONS FROM DATABASE
    // =========================================================

    const loadNotifications = async () => {

        try {

            const token = localStorage.getItem("token");

            if (!token) {
                return;
            }

            const response = await axios.get(
                API_URL,
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            setNotifications(response.data);

        } catch (error) {

            console.error(
                "Error loading notifications:",
                error
            );
        }
    };


    // =========================================================
    // GET SEVERITY STYLE
    // =========================================================

    const getSeverityStyle = (severity) => {

        const level = severity?.toUpperCase();

        switch (level) {

            case "CRITICAL":
                return {
                    backgroundColor: "#7f1d1d",
                    color: "#fecaca"
                };

            case "HIGH":
                return {
                    backgroundColor: "#991b1b",
                    color: "#fee2e2"
                };

            case "MEDIUM":
                return {
                    backgroundColor: "#92400e",
                    color: "#fef3c7"
                };

            case "LOW":
                return {
                    backgroundColor: "#065f46",
                    color: "#d1fae5"
                };

            default:
                return {
                    backgroundColor: "#334155",
                    color: "#e2e8f0"
                };
        }
    };


    // =========================================================
    // SHOW REAL-TIME ALERT TOAST
    // =========================================================

    const showAlertToast = (alert) => {

        const severityStyle =
            getSeverityStyle(alert.severity);

        toast(
            <div
                style={{
                    width: "100%",
                    minWidth: 0
                }}
            >

                {/* Alert title */}

                <div
                    style={{
                        display: "flex",
                        alignItems: "flex-start",
                        gap: "10px",
                        marginBottom: "10px"
                    }}
                >

                    <span
                        style={{
                            fontSize: "21px",
                            flexShrink: 0
                        }}
                    >
                        🚨
                    </span>

                    <div
                        style={{
                            fontSize: "16px",
                            fontWeight: "700",
                            lineHeight: "1.4",
                            wordBreak: "break-word"
                        }}
                    >
                        {alert.title || "Security Alert"}
                    </div>

                </div>


                {/* Severity + Status */}

                <div
                    style={{
                        display: "flex",
                        alignItems: "center",
                        flexWrap: "wrap",
                        gap: "8px",
                        marginBottom: "9px"
                    }}
                >

                    <span
                        style={{
                            ...severityStyle,
                            padding: "4px 10px",
                            borderRadius: "20px",
                            fontSize: "12px",
                            fontWeight: "700"
                        }}
                    >
                        {alert.severity?.toUpperCase() || "UNKNOWN"}
                    </span>


                    <span
                        style={{
                            padding: "4px 10px",
                            borderRadius: "20px",
                            backgroundColor: "#1e3a5f",
                            color: "#bae6fd",
                            fontSize: "12px",
                            fontWeight: "600"
                        }}
                    >
                        {alert.status?.toUpperCase() || "OPEN"}
                    </span>

                </div>


                {/* Message */}

                <div
                    style={{
                        fontSize: "13px",
                        lineHeight: "1.5",
                        color: "#cbd5e1",
                        wordBreak: "break-word"
                    }}
                >
                    {alert.message ||
                        "New security alert detected."}
                </div>

            </div>,
            {
                position: "top-right",

                autoClose: 5000,

                hideProgressBar: false,

                closeOnClick: true,

                pauseOnHover: true,

                draggable: true,

                theme: "dark",

                style: {
                    width: "380px",
                    maxWidth: "calc(100vw - 30px)"
                }
            }
        );
    };


    // =========================================================
    // INITIAL LOAD + WEBSOCKET + POLLING
    // =========================================================

    useEffect(() => {

        const token = localStorage.getItem("token");

        if (!token) {
            return;
        }


        // Load existing notifications
        loadNotifications();


        // -----------------------------------------------------
        // Poll database every 3 seconds
        // Used for incident/playbook notifications
        // -----------------------------------------------------

        const interval = setInterval(
            loadNotifications,
            3000
        );


        // -----------------------------------------------------
        // Connect WebSocket
        // Used for real-time security alerts
        // -----------------------------------------------------

        connectWebSocket((alert) => {

            console.log(
                "🚨 Real-time alert received:",
                alert
            );


            // Show toast only if enabled in settings

            if (
                settings?.inAppNotificationsEnabled !== false
            ) {

                showAlertToast(alert);

            }


            // Refresh notification center

            loadNotifications();

        });


        // -----------------------------------------------------
        // CLEANUP
        // -----------------------------------------------------

        return () => {

            clearInterval(interval);

            disconnectWebSocket();

        };

    }, [settings?.inAppNotificationsEnabled]);


    // =========================================================
    // UNREAD COUNT
    // =========================================================

    const unreadCount =
        notifications.filter(
            notification =>
                !notification.readStatus
        ).length;


    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    const markAllRead = async () => {

        try {

            const token =
                localStorage.getItem("token");

            if (!token) {
                return;
            }


            await axios.put(
                `${API_URL}/read-all`,
                {},
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            await loadNotifications();

        } catch (error) {

            console.error(
                "Error marking notifications as read:",
                error
            );

        }
    };


    // =========================================================
    // CLEAR ALL NOTIFICATIONS
    // =========================================================

    const clearNotifications = async () => {

        try {

            const token =
                localStorage.getItem("token");

            if (!token) {
                return;
            }


            await axios.delete(
                API_URL,
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            setNotifications([]);

        } catch (error) {

            console.error(
                "Error clearing notifications:",
                error
            );

        }
    };


    // =========================================================
    // CONTEXT PROVIDER
    // =========================================================

    return (

        <NotificationContext.Provider
            value={{
                notifications,
                unreadCount,
                markAllRead,
                clearNotifications,
                loadNotifications
            }}
        >

            {children}

        </NotificationContext.Provider>

    );

};


// =============================================================
// CUSTOM HOOK
// =============================================================

export const useNotifications = () =>
    useContext(NotificationContext);