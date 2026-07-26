import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080/api",
});

// Automatically attach JWT token to every request
api.interceptors.request.use(

    (config) => {

        const token = localStorage.getItem("token");

        if (token && !config.url.includes("/auth/")) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },

    (error) => {
        return Promise.reject(error);
    }

);

// Automatically handle expired tokens with a refresh token
api.interceptors.response.use(
    (response) => {
        return response;
    },
    async (error) => {
        const originalRequest = error.config;

        // If the request fails with 401 (Unauthorized) and hasn't been retried yet
        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const refreshToken = localStorage.getItem("refreshToken");

                if (refreshToken) {
                    // Call the refresh token endpoint using raw axios to avoid interceptor loop
                    const res = await axios.post("http://localhost:8080/api/auth/refreshtoken", {
                        refreshToken: refreshToken,
                    });

                    if (res.status === 200 && res.data.accessToken) {
                        const newAccessToken = res.data.accessToken;
                        localStorage.setItem("token", newAccessToken);

                        // Retry original request with the new token
                        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                        return api(originalRequest);
                    }
                }
            } catch (refreshError) {
                console.error("Session expired. Logging out...", refreshError);
                // Clear user session data on failure
                localStorage.removeItem("token");
                localStorage.removeItem("refreshToken");
                localStorage.removeItem("email");
                localStorage.removeItem("role");
                localStorage.removeItem("isLoggedIn");
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;