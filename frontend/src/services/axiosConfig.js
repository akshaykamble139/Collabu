import axios from "axios";

const instance = axios.create({
  baseURL: "http://localhost:8080/collabu",
});

// Request Interceptor (Attach Token)
instance.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Response Interceptor (Handle 401 Errors)
instance.interceptors.response.use((response) => {
  return response;
}, (error) => {
  if (error.response && error.response.status === 401) {
    // Token expired or invalid
    alert("Session expired. Please login again.");
    localStorage.removeItem("token");
    localStorage.removeItem("username")
    window.location.href = "/login";
  }
  return Promise.reject(error);
});

export default instance;
