import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { TextField, Button, Typography, Container, Box, CircularProgress } from "@mui/material";
import instance from "../services/axiosConfig";
import { useDispatch, useSelector } from "react-redux";
import { setUser } from "../redux/userSlice";
import { showNotification } from "../redux/notificationSlice";  // Import notification action

const LoginPage = () => {
  const [form, setForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);

  useEffect(() => {
    if (userData?.username && userData?.token) {
      navigate("/");
    }
  }, [userData, navigate]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validateForm = () => {
    if (!form.username || !form.password) {
      dispatch(showNotification({ message: "Username and Password are required.", type: "error" }));
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    try {
      const response = await instance.post("/api/auth/login", form);
      const token = response.data;

      localStorage.setItem("token", token);  
      localStorage.setItem("username", form.username); 

      dispatch(setUser({ username: form.username, token }));
      dispatch(showNotification({ message: "Login successful!", type: "success" }));

      setTimeout(() => {
        navigate("/");
      }, 1000);

    } catch (err) {
      console.log("login failed", err.response?.data?.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          minHeight: "100vh",
          textAlign: "center",
        }}
      >
        <Typography variant="h4" component="h1" gutterBottom>
          Login to Your Account
        </Typography>
        <Box component="form" onSubmit={handleSubmit} sx={{ width: "100%" }}>
          <TextField
            label="Username"
            name="username"
            fullWidth
            margin="normal"
            value={form.username}
            onChange={handleChange}
            required
          />
          <TextField
            label="Password"
            name="password"
            type="password"
            fullWidth
            margin="normal"
            value={form.password}
            onChange={handleChange}
            required
          />
          <Button
            type="submit"
            variant="contained"
            color="primary"
            fullWidth
            sx={{ mt: 2 }}
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : "Login"}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default LoginPage;
