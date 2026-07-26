import api from "./api";

// ==========================================
// ALERT SETTINGS
// ==========================================
export const getAlertSettings = async () => {
    const response = await api.get("/settings/alerts");
    return response.data;
};

export const updateAlertSettings = async (data) => {
    const response = await api.put("/settings/alerts", data);
    return response.data;
};

// ==========================================
// NOTIFICATION SETTINGS
// ==========================================
export const getNotificationSettings = async () => {
    const response = await api.get("/settings/notifications");
    return response.data;
};

export const updateNotificationSettings = async (data) => {
    const response = await api.put("/settings/notifications", data);
    return response.data;
};

// ==========================================
// DATA RETENTION SETTINGS
// ==========================================
export const getRetentionSettings = async () => {
    const response = await api.get("/settings/retention");
    return response.data;
};

export const updateRetentionSettings = async (data) => {
    const response = await api.put("/settings/retention", data);
    return response.data;
};

// ==========================================
// SYSTEM SETTINGS
// ==========================================
export const getSystemSettings = async () => {
    const response = await api.get("/settings/system");
    return response.data;
};

export const updateSystemSettings = async (data) => {
    const response = await api.put("/settings/system", data);
    return response.data;
};
