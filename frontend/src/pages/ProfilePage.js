import React, { useEffect, useState } from "react";
import { Container, Box, Typography, TextField, Button, Avatar, List, ListItem, ListItemText, Divider } from "@mui/material";
import axios from "axios";
import instance from "../services/axiosConfig";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../redux/userSlice";

const ProfilePage = () => {
  const { username } = useParams();
  const navigate = useNavigate();
  const [user, setUser] = useState({ username: "", email: "", bio: "", location: "", website: "", profilePicture: "" });
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const userData = useSelector(state => state.user);
  const dispatch = useDispatch();
  const [selectedFile, setSelectedFile] = useState(null);

  useEffect(() => {
    let userName = username;
    if (!userName) {
      if (userData?.username && userData.token) {
        userName = userData.username;
        navigate("/profile/" + userName);
      } else {
        const token = localStorage.getItem("token");
        const username1 = localStorage.getItem("username");
        if (token && username1) {
          dispatch(setUser({ username: username1, token: token }));
          userName = username1;
          navigate("/profile/" + userName);
        } else {
          navigate("/login");
        }
      }
    }
  }, []);

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        if (!username) {
          const response = await instance.get("/users/profile/" + username);
          setUser(response.data);
        }
        else {
          const response = await instance.get("/users/profile/" + username);
          setUser(response.data);
        }
      } catch (error) {
        navigate("/error", { state: { code: 401 } });
      }
    };
    fetchUserProfile();
  }, [username]);

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    try {
      await instance.post("/users/password", { password });
      setMessage("Password updated successfully!");
      setPassword("");
      setTimeout(() => {
        alert("Password changed. Please login again.");
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        dispatch(logout());
        navigate("/login");
      }, 2000);
    } catch (error) {
      setMessage("Failed to update password. Please try again.");
    }
  };

  const handleFieldUpdate = (field, value) => {
    setUser((prev) => ({ ...prev, [field]: value }));
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    if (userData?.username === username) {
      try {
        await instance.put("/users/profile/update", {
          bio: user.bio,
          location: user.location,
          website: user.website
        });
        setMessage("Profile updated successfully!");
      } catch (error) {
        setMessage("Failed to update profile.");
      }
    }
  };

  const handleFileChange = (e) => {
    setSelectedFile(e.target.files[0]);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;
    const formData = new FormData();
    formData.append("file", selectedFile);
    try {
      await instance.post("/users/upload-profile-picture", formData, {
        headers: { "Content-Type": "multipart/form-data" }
      });
      alert("Profile picture updated!");
      window.location.reload();
    } catch (error) {
      alert("Failed to upload file.");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    dispatch(logout());
    navigate("/login");
  };

  const handleDeactivate = async () => {
    if (window.confirm("Are you sure you want to deactivate your account?")) {
      await instance.patch("/api/auth/deactivate");
      handleLogout();
    }
  };

  const handleDelete = async () => {
    if (window.confirm("Are you sure you want to delete your account? This action is irreversible.")) {
      await instance.delete("/users");
      handleLogout();
    }
  };

  return (
    <Container maxWidth="md">
      <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center", mt: 5 }}>
        <Avatar sx={{ width: 100, height: 100, mb: 2 }} src={user.profilePicture || "/default-avatar.png"} />
        <Typography variant="h4">{user.username}</Typography>
        <Typography variant="body1">{user.email}</Typography>
        <Typography variant="body1" sx={{ mt: 1, mb: 1 }}>Member since {new Date(user.createdAt).toLocaleDateString()}</Typography>
        
        {userData?.username === username && (
          <Box mt={2}>
            <Typography variant="body1" sx={{ mb: 1 }}>
              {selectedFile ? selectedFile.name : "No file selected"}
            </Typography>
            <Button variant="contained" component="label">
              Choose File
              <input type="file" hidden accept="image/*" onChange={handleFileChange} />
            </Button>
            <Button variant="contained" onClick={handleUpload} sx={{ ml: 1}} disabled={!selectedFile}>
              Upload
            </Button>
          </Box>
        )}

        <Box component="form" onSubmit={handleProfileUpdate} sx={{ mt: 3, width: "100%" }}>
          <Typography variant="h6">Edit Profile</Typography>
          <TextField 
            label="Bio" 
            fullWidth 
            margin="normal" 
            value={user.bio} 
            onChange={(e) => handleFieldUpdate("bio", e.target.value)} 
            disabled = {!(userData !== null && userData.username === username)}/>
          <TextField 
            label="Location" 
            fullWidth margin="normal" 
            value={user.location} 
            onChange={(e) => handleFieldUpdate("location", e.target.value)} 
            disabled = {!(userData !== null && userData.username === username)}/>
          <TextField 
            label="Website" 
            fullWidth 
            margin="normal" 
            value={user.website} 
            onChange={(e) => handleFieldUpdate("website", e.target.value)}  
            disabled = {!(userData !== null && userData.username === username)}/>
          {userData !== null && userData.username === username &&<Button type="submit" variant="contained" fullWidth sx={{ mt: 2 }}>Update Details</Button>}
        </Box>

        {userData !== null && userData.username === username &&
          <Box component="form" onSubmit={handlePasswordChange} sx={{ mt: 4, width: "100%" }}>
            <Typography variant="h6">Change Password</Typography>
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
          </Box>}

          {message && (
            <Typography color="success.main" sx={{ mt: 2 }}>
              {message}
            </Typography>
          )}

        {userData !== null && userData.username === username &&
          <Box sx={{ mt: 5, width: "100%" }}>
            <Button variant="outlined" fullWidth onClick={handleLogout} sx={{ mt: 2 }}>Logout</Button>
            <Button variant="outlined" fullWidth onClick={handleDeactivate} sx={{ mt: 2 }}>Deactivate Account</Button>
            <Button variant="contained" color="error" fullWidth onClick={handleDelete} sx={{ mt: 2 }}>Delete Account</Button>
          </Box>}
      </Box>
    </Container>
  );
};

export default ProfilePage;
