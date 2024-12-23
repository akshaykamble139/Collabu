import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import instance from "../services/axiosConfig";
import { Button, Typography, Container, List, ListItem, TextField, Box } from "@mui/material";
import { useDispatch, useSelector } from "react-redux";
import { setUser } from "../redux/userSlice";

const RepositoriesPage = () => {
  const { username } = useParams();
  const [repos, setRepos] = useState([]);
  const [repoName, setRepoName] = useState("");
  const navigate = useNavigate();
  const userData = useSelector(state => state.user);
  const dispatch = useDispatch();

  useEffect(() => {
    const fetchRepos = async () => {
      try {
        let userName = username;
        if (userName === null || userName === undefined || typeof(userName) === undefined || userName === "") {
          if (userData !== null && userData.username !== "" && userData.token !== "") {
            userName = userData.username;
          }
          else {

            const token = localStorage.getItem("token");
            const username1 = localStorage.getItem("username");
          
            if (typeof(token) !== undefined && typeof(username1) != undefined && token && username1) {
              dispatch(setUser({username: username1, token: token}))
              userName = username1
            }
            else {
              navigate("/login");
            }
          }
        }
        const response = await instance.get("/repositories/user/" + userName);
        setRepos(response.data);
      } catch (err) {
        console.log(err.response?.data?.message || "Fetching repositories. Please try again.");
        navigate("/error", { state: { code: 401 } });
      }
    };
    fetchRepos();
  }, []);

  const handleCreate = async () => {
    try {
      await instance.post("/repositories", { name: repoName, ownerUsername: userData.username });
      setRepoName("");
      window.location.reload();
    }catch (err) {
      console.log(err.response?.data?.message || "Error while creating repository. Please try again.");
      navigate("/error", { state: { code: 400 } });
    }
  };

  return (
    <Container>
      <Typography variant="h4">Repositories</Typography>
      <List>
        {repos.map((repo) => (
          <ListItem key={repo.id}>{repo.name}</ListItem>
        ))}
      </List>
      <Box mt={3}>
        <TextField
          label="New Repository"
          value={repoName}
          
          onChange={(e) => setRepoName(e.target.value)}
          fullWidth
        />
        <Button onClick={handleCreate} variant="contained" sx={{ mt: 2 }}>
          Create Repository
        </Button>
      </Box>
    </Container>
  );
};

export default RepositoriesPage;
