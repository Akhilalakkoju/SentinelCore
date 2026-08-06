import axios from "axios";
import { createContext, useContext, useEffect, useState } from "react";
import { toast } from "react-toastify";
import {
    connectWebSocket,
    disconnectWebSocket
} from "../services/websocket";
import { useSettings } from "./SettingsContext";

const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {

    const { settings } = useSettings() || {};
    const [notifications, setNotifications] = useState([]);

    // Load notifications from database
    const loadNotifications = async () => {

        try {

            const token = localStorage.getItem("token");
            if (!token) return;

            const response = await axios.get(
                "http://localhost:8080/api/notifications",
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            setNotifications(response.data);

        } catch (error) {

            console.error("Error loading notifications:", error);

        }

    };

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (token) {
            loadNotifications();

            // Polling every 3 seconds for new playbook/incident notifications
            const interval = setInterval(loadNotifications, 3000);

            // Connect WebSocket
            connectWebSocket((alert) => {
                if (settings?.inAppNotificationsEnabled === false) {
                    loadNotifications();
                    return;
                }

                toast.success(
                    <>
                        <div className="font-bold text-base">
                            🚨 {alert.title}
                        </div>

                        <div className="mt-1">
                            <strong>Severity:</strong> {alert.severity}
                        </div>

                        <div className="mt-1">
                            <strong>Status:</strong> {alert.status}
                        </div>

                        <div className="mt-2 text-sm">
                            {alert.message}
                        </div>
                    </>,
                    {
                        position: "top-right",
                        autoClose: 5000,
                        hideProgressBar: false,
                        closeOnClick: true,
                        pauseOnHover: true,
                        draggable: true,
                        theme: "dark"
                    }
                );

                // Refresh notifications
                loadNotifications();
            });

            return () => {
                clearInterval(interval);
                disconnectWebSocket();
            };
        }
    }, [settings?.inAppNotificationsEnabled]);


    const unreadCount =
        notifications.filter(n => !n.readStatus).length;

    const markAllRead = async () => {

        try {

            const token = localStorage.getItem("token");
            if (!token) return;

            await axios.put(
                "http://localhost:8080/api/notifications/read-all",
                {},
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            loadNotifications();

        } catch (error) {

            console.error(error);

        }

    };

    const clearNotifications = async () => {

        try {

            const token = localStorage.getItem("token");
            if (!token) return;

            await axios.delete(
                "http://localhost:8080/api/notifications",
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            setNotifications([]);

        } catch (error) {

            console.error(error);

        }

    };

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

export const useNotifications = () =>
    useContext(NotificationContext);