import axios from "axios";
import { store } from "../redux/store";
import { showNotification } from "../redux/notificationSlice";
import { logout } from "../redux/userSlice";

const instance = axios.create({
  baseURL: "http://localhost:8080/collabu",
});

// Request Interceptor (Attach Token)
instance.interceptors.request.use((config) => {
  const user = store.getState().user;
  if (user.token) {
    config.headers.Authorization = `Bearer ${user.token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Response Interceptor (Handle 401 and 403 Errors)
instance.interceptors.response.use((response) => {
  return response;
}, (error) => {
  const { response } = error;

  if (response) {
    const status = response.status;

    // Handle 401 Unauthorized
    if (status === 401) {
      if (response.data === "Invalid credentials") {
        store.dispatch(showNotification({
          message: "Invalid credentials. Please login again.",
          type: "error",
        }));
      } else {
        store.dispatch(showNotification({
          message: "Session expired. Please login again.",
          type: "error",
        }));
        store.dispatch(logout())
        setTimeout(() => {
          window.location.href = "/login";  // Redirect to login page        
        }, 3000);
      }
    }

    // Handle 403 Forbidden (Inactive Account)
    if (status === 403) {
      store.dispatch(showNotification({
        message: "Your account is inactive. Reactivation required.",
        type: "error",
      }));
      setTimeout(() => {
        window.location.href = "/reactivate";  // Redirect to reactivation page        
      }, 3000);
    }
  } else {
    // Network Error or No Response
    store.dispatch(showNotification({
      message: "Network error. Please check your connection.",
      type: "error",
    }));
  }

  return Promise.reject(error);
});

export default instance;
