import React, { useEffect, useState } from "react";
import { TextField, Button, Container, Box, Typography, CircularProgress } from "@mui/material";
import instance from "../services/axiosConfig";
import { showNotification } from "../redux/notificationSlice";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";

const ReactivateAccount = () => {
  const [form, setForm] = useState({ username: "", email: "", password: "" });
  const [loading, setLoading] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const userData = useSelector(state => state.user);

  useEffect(() => {
    if (userData?.username && userData?.token) {
        dispatch(showNotification({
            message: "Account is already active",
            type: "error",
        }));
        setTimeout(() => {
            navigate("/");
        }, 3000);
    }
  }, [userData, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Send reactivation request with username, email, and password
      await instance.post("/api/auth/reactivate", form);
      
      dispatch(showNotification({
        message: "Account reactivated successfully. You can now log in.",
        type: "success",
      }));

      // Redirect to login after reactivation
      setTimeout(() => {
        navigate("/login");
      }, 1500);
      
    } catch (error) {
      dispatch(showNotification({
        message: error.response?.data?.message || "Reactivation failed. Please check your details.",
        type: "error",
      }));
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
          Reactivate Your Account
        </Typography>
        <Typography variant="body1" gutterBottom>
          Enter your details to reactivate your account.
        </Typography>
        <Box component="form" onSubmit={handleSubmit} sx={{ width: "100%", mt: 2 }}>
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
            {loading ? <CircularProgress size={24} /> : "Reactivate Account"}
          </Button>
        </Box>
      </Box>
    </Container>
  );
};

export default ReactivateAccount;
