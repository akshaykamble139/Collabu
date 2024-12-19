// src/pages/ProfilePage.js
import React, { useEffect, useState } from "react";
import { Container, Box, Typography, TextField, Button, Avatar } from "@mui/material";
import axios from "axios";

const ProfilePage = () => {
  const [user, setUser] = useState({ username: "", email: "" });
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        const token = localStorage.getItem("token");
        const response = await axios.get("http://localhost:8080/collabu/api/users/profile", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUser(response.data);
      } catch (error) {
        console.error("Error fetching profile:", error);
      }
    };
    fetchUserProfile();
  }, []);

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    try {
      const token = localStorage.getItem("token");
      await axios.put(
        "http://localhost:8080/collabu/api/users/password",
        { password },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setMessage("Password updated successfully!");
      setPassword("");
    } catch (error) {
      setMessage("Failed to update password. Please try again.");
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
        }}
      >
        <Avatar sx={{ width: 100, height: 100, mb: 2 }}>
          {user.username.charAt(0).toUpperCase()}
        </Avatar>
        <Typography variant="h4" gutterBottom>
          {user.username}
        </Typography>
        <Typography variant="body1" gutterBottom>
          {user.email}
        </Typography>

        <Box component="form" onSubmit={handlePasswordChange} sx={{ mt: 4, width: "100%" }}>
          <Typography variant="h6" gutterBottom>
            Change Password
          </Typography>
          <TextField
            label="New Password"
            type="password"
            fullWidth
            margin="normal"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <Button type="submit" variant="contained" color="primary" fullWidth sx={{ mt: 2 }}>
            Update Password
          </Button>
        </Box>
        {message && (
          <Typography color="success.main" sx={{ mt: 2 }}>
            {message}
          </Typography>
        )}
      </Box>
    </Container>
  );
};

export default ProfilePage;
