// src/pages/RegisterPage.js
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { TextField, Button, Typography, Container, Box, CircularProgress } from "@mui/material";
import instance from "../services/axiosConfig";
import { useDispatch, useSelector } from "react-redux";
import { showNotification } from "../redux/notificationSlice";

const RegisterPage = () => {
  const [form, setForm] = useState({ username: "", email: "", password: "" });
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const dispatch = useDispatch();

  const userData = useSelector(state => state.user);

  useEffect(() => {
    if (userData?.username && userData?.token) {
      dispatch(showNotification({
        message: "You are already logged in!",
        type: "error",
      }));
      setTimeout(() => {
          navigate("/");
      }, 1500);
    }
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const validateForm = () => {
    const { username, email, password } = form;

    // Basic validation checks
    if (!username || !email || !password) {
      dispatch(showNotification({
        message: "All fields are required.",
        type: "error",
      }));
      return false;
    }

    // Email validation
    const emailRegex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
      dispatch(showNotification({
        message: "Invalid email format.",
        type: "error",
      }));
      return false;
    }

    // Password strength check (at least 8 characters, 1 special character)
    const passwordRegex = /^(?=.*[!@#$%^&*])(?=.*[A-Za-z0-9]).{8,}$/;
    if (!passwordRegex.test(password)) {
      dispatch(showNotification({
        message: "Password must be at least 8 characters and contain at least one special character.",
        type: "error",
      }));
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setLoading(true);
    setTimeout(async () => {

    try {
      await instance.post("/api/auth/register", form);
      
      dispatch(showNotification({
        message: "Registration successful! Redirecting to login...",
        type: "success",
      }));

      // Redirect to login page after a short delay
      setTimeout(() => {
        navigate("/login");
      }, 1500);
      
    } catch (err) {
      const errorMessage = err.response?.data?.message || "Registration failed. Please try again.";
      dispatch(showNotification({
        message: errorMessage,
        type: "error",
      }));
    } finally {
      setLoading(false);
    }
  },1000);
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
          Create an Account
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
            label="Email"
            name="email"
            type="email"
            fullWidth
            margin="normal"
            value={form.email}
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
            {loading ? <CircularProgress size={24} /> : "Register"}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default RegisterPage;
