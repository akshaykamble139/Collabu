import React, { useEffect, useState } from "react";
import { Container, Box, Typography, TextField, Button, Avatar, List, ListItem, ListItemText, Divider } from "@mui/material";
import instance from "../services/axiosConfig";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "../redux/userSlice";
import { showNotification } from "../redux/notificationSlice";
import ConfirmationDialog from "./ConfirmationDialog";

const ProfilePage = () => {
  const { username } = useParams();
  const navigate = useNavigate();
  const [user, setUser] = useState({ username: "", email: "", bio: "", location: "", website: "", profilePicture: "" });
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const userData = useSelector(state => state.user);
  const dispatch = useDispatch();
  const [selectedFile, setSelectedFile] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogAction, setDialogAction] = useState(null);

  useEffect(() => {
    if (!username && userData.username) {
      navigate(`/profile/${userData.username}`);
    }
  }, [username, userData, navigate]);

  useEffect(() => {
    const fetchUserProfile = async () => {
      setLoading(true);
      try {
        const response = await instance.get("/users/profile/" + username);
        setUser(response.data);
      } catch (error) {
        dispatch(showNotification({ message: "Failed to load profile.", type: "error" }));
        navigate("/error");
      } finally {
        setLoading(false);
      }
    };
    fetchUserProfile();
  }, [username, dispatch, navigate]);

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    try {
      await instance.post("/users/password", { password });
      dispatch(showNotification({ message: "Password updated. Please login again.", type: "success" }));
      localStorage.removeItem("token");
      localStorage.removeItem("username");
      setTimeout(() => {
        dispatch(logout());
        navigate("/login");
      }, 2000);
    } catch (error) {
      dispatch(showNotification({ message: "Failed to update password.", type: "error" }));
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
        dispatch(showNotification({ message: "Profile updated successfully!", type: "success" }));
      } catch (error) {
        dispatch(showNotification({ message: "Failed to update profile.", type: "error" }));
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
        headers: { "Content-Type": "multipart/form-data" },
      });
      dispatch(showNotification({ message: "Profile picture updated!", type: "success" }));
      window.location.reload();
    } catch (error) {
      dispatch(showNotification({ message: "Failed to upload picture.", type: "error" }));
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    dispatch(logout());
    navigate("/login");
  };

  const openDialog = (action) => {
    setDialogAction(action);
    setDialogOpen(true);
  };

  const handleConfirmDialog = async () => {
    setDialogOpen(false);
    if (dialogAction === "deactivate") {
      await instance.post("/users/deactivate");
      dispatch(showNotification({ message: "Account deactivated.", type: "success" }));
      setTimeout(() => {
        handleLogout();
      },1500);
    } else if (dialogAction === "delete") {
      await instance.delete("/users");
      dispatch(showNotification({ message: "Account deleted.", type: "success" }));
      setTimeout(() => {
        handleLogout();
      },1500);
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

        {userData !== null && userData.username === username &&
          <Box sx={{ mt: 5, width: "100%" }}>
            <Button variant="outlined" fullWidth onClick={handleLogout} sx={{ mt: 2 }}>Logout</Button>
            <Button variant="outlined" fullWidth onClick={() => openDialog("deactivate")} sx={{ mt: 2 }}>Deactivate Account</Button>
            <Button variant="contained" color="error" fullWidth onClick={() => openDialog("delete")} sx={{ mt: 2 }}>Delete Account</Button>
          </Box>}
      </Box>

      <ConfirmationDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onConfirm={handleConfirmDialog}
        title={dialogAction === "delete" ? "Delete Account" : "Deactivate Account"}
        message={
          dialogAction === "delete"
            ? "Are you sure you want to delete your account? This action is irreversible."
            : "Are you sure you want to deactivate your account?"
        }
      />
    </Container>
  );
};

export default ProfilePage;
