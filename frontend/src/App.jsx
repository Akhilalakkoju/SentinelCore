import AppRoutes from "./routes/AppRoutes";
import { ToastContainer } from "react-toastify";
import { Toaster } from "react-hot-toast";
import { NotificationProvider } from "./context/NotificationContext";
import { SettingsProvider } from "./context/SettingsContext";

import "react-toastify/dist/ReactToastify.css";

function App() {
    return (
        <SettingsProvider>
            <NotificationProvider>

                <AppRoutes />

                <ToastContainer
                    position="top-right"
                    autoClose={5000}
                    newestOnTop
                    theme="dark"
                />

                <Toaster
                    position="top-right"
                    toastOptions={{
                        duration: 4000,
                        style: {
                            background: '#1e293b',
                            color: '#e2e8f0',
                            border: '1px solid #334155',
                        },
                        success: {
                            iconTheme: { primary: '#38bdf8', secondary: '#0f172a' },
                        },
                        error: {
                            iconTheme: { primary: '#f43f5e', secondary: '#0f172a' },
                        },
                    }}
                />

            </NotificationProvider>
        </SettingsProvider>
    );
}

export default App;