// src/pages/ProfilePage.js
import React, { useEffect, useState } from "react";
import { Container, Box, Typography, TextField, Button, Avatar, List, ListItem, ListItemText, Divider } from "@mui/material";
import axios from "axios";
import instance from "../services/axiosConfig";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";

const ProfilePage = () => {
  const { username } = useParams();
  const navigate = useNavigate();
  const [user, setUser] = useState({ username: "", email: "", bio: "", location: "", website: "", profilePicture: "" });
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [repositories, setRepositories] = useState([]);
  const userData = useSelector(state => state.user);
  const dispatch = useDispatch();
  const [selectedFile, setSelectedFile] = useState(null);

  useEffect(() => {
    console.log("inside useeffect profilepage",username)
    let userName = username;
    if (userName === null || userName === undefined || typeof(userName) === undefined || userName === "") {
      if (userData !== null && userData.username !== "" && userData.token !== "") {
        userName = userData.username;
        navigate("/profile/"+userName);
      }
      else {

        const token = localStorage.getItem("token");
        const username1 = localStorage.getItem("username");
      
        console.log("inside else profilepage",username)

        if (typeof(token) !== undefined && typeof(username1) != undefined && token && username1) {
          dispatch(setUser({username: username1, token: token}))
          userName = username1;
          navigate("/profile/"+userName);
        }
        else {
          navigate("/login");
        }
      }
    }
  },[])
  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        const response = await instance.get("/users/profile/" + username);
        setUser(response.data);
        const repoResponse = await instance.get("/repositories/user/" + username);
        setRepositories(repoResponse.data);
      } catch (error) {
        console.error("Error fetching profile:", error);
        navigate("/error", { state: { code: 401 } });
      }
    };
    fetchUserProfile();
  }, [userData]);

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

  const handleFieldUpdate = async (field, value) => {
      setUser((prev) => ({ ...prev, [field]: value }));
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();
    if (userData !== null && userData.username === username) {
      try {
        await instance.put(
          `/users/profile/update`,
          { bio: user.bio, location: user.location, website: user.website }
        );
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
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      alert("Profile picture updated!");
      window.location.reload();
    } catch (error) {
      console.error("Error uploading file:", error);
      alert("Failed to upload file.");
    }
  };


  return (
    <Container maxWidth="md">
      <Box sx={{ display: "flex", flexDirection: "column", alignItems: "center", mt: 5 }}>
        <Avatar
          sx={{ width: 100, height: 100, mb: 2 }}
          src={user.profilePicture ? `${user.profilePicture}` :  "/default-avatar.png"}
        />
        <Typography variant="h4">{user.username}</Typography>
        <Typography variant="body1">{user.email}</Typography>
        <Box mt={3}>
          <input type="file" accept="image/*" onChange={handleFileChange} />
          <Button variant="contained" onClick={handleUpload} sx={{ mt: 2 }}>
            Upload
          </Button>
        </Box>

        <Box component="form" onSubmit={handleProfileUpdate} sx={{ mt: 3, width: "100%" }}>
          <Typography variant="h6">Edit Profile</Typography>
          <TextField
            label="Bio"
            fullWidth
            margin="normal"
            disabled = {!(userData !== null && userData.username === username)}
            value={user.bio}
            onChange={(e) => handleFieldUpdate("bio", e.target.value)}
          />
          <TextField
            label="Location"
            fullWidth
            margin="normal"
            disabled = {!(userData !== null && userData.username === username)}
            value={user.location}
            onChange={(e) => handleFieldUpdate("location", e.target.value)}
          />
          <TextField
            label="Website"
            fullWidth
            margin="normal"
            disabled = {!(userData !== null && userData.username === username)}
            value={user.website}
            onChange={(e) => handleFieldUpdate("website", e.target.value)}
          />
          {userData !== null && userData.username === username &&
          <Button type="submit" variant="contained" color="primary" fullWidth sx={{ mt: 2 }}>
            Update Details
          </Button>}
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

        <Box sx={{ mt: 5, width: "100%" }}>
          <Typography variant="h6">Repositories</Typography>
          <List>
            {repositories.map((repo) => (
              <ListItem key={repo.id} button>
                <ListItemText primary={repo.name} secondary={repo.description} />
              </ListItem>
            ))}
          </List>
        </Box>

        <Divider sx={{ mt: 5, mb: 3 }} />
        <Typography variant="body2">Member since {new Date(user.createdAt).toLocaleDateString()}</Typography>
      </Box>
    </Container>
  );
};

export default ProfilePage;
