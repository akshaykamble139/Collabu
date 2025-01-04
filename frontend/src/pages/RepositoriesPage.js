import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Container,
  Typography,
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControlLabel,
  Switch,
  Paper,
  Grid2,
  InputAdornment,
  Chip,
} from '@mui/material';
import { Search, Star, GitFork, Code } from 'lucide-react';
import instance from '../services/axiosConfig';
import { showNotification } from '../redux/notificationSlice';

const RepositoriesPage = () => {
  const { username } = useParams();
  const [repos, setRepos] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [openNewRepo, setOpenNewRepo] = useState(false);
  const [newRepo, setNewRepo] = useState({ name: '', description: '', publicRepositoryOrNot: true });
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);

  const fetchRepos = async (userName) => {
    try {
      const response = await instance.get(`/repositories/user/${userName}`);
      setRepos(response.data);
    } catch (err) {
      dispatch(showNotification({ message: "Failed to fetch repositories.", type: "error" }));
    }
  };

  useEffect(() => {
    let userName = username;
    if (!userName && userData?.username) {
      userName = userData.username;
    }
    if (userName) {
      fetchRepos(userName);
    }
  }, [username]);

  const validateRepositoryForm = () => {
    let error = "";
  
    if (!newRepo.name) {
      error = "Repository name can't be empty";
    } else if (newRepo.name.length > 100) {
      error = "Repository name should not exceed 100 characters";
    } else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(newRepo.name)) {
      error = "Repository name must start with an alphabet and contain only alphanumeric characters or underscores";
    }

    return error;
  };

  const handleCreateRepo = async () => {
    if (userData?.username === username) {
      try {
        const error = validateRepositoryForm();

        if (error !== "") {
          dispatch(showNotification({ message: error, type: "error" }));
          return;
        }
        const response = await instance.post('/repositories', {
          name: newRepo.name,
          description: newRepo.description,
          ownerUsername: userData.username,
          publicRepositoryOrNot: newRepo.publicRepositoryOrNot,
        });
        setRepos([...repos, response.data]);
        setOpenNewRepo(false);
        setNewRepo({ name: '', description: '', publicRepositoryOrNot: true });
        dispatch(showNotification({ message: "Repository created successfully!", type: "success" }));
        navigate(`/${userData.username}/${response.data.name}`);
      } catch (err) {
        dispatch(showNotification({ message: err.response?.data?.message || 'Failed to create repository.', type: "error" }));
      }
    }
  };

  const filteredRepos = repos.filter(repo =>
    repo.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4">
          {username ? `${username}'s Repositories` : 'Your Repositories'}
        </Typography>
        {(username === userData?.username) && (
          <Button
            variant="contained"
            color="primary"
            onClick={() => setOpenNewRepo(true)}
          >
            New Repository
          </Button>
        )}
      </Box>

      <TextField
        fullWidth
        variant="outlined"
        placeholder="Find a repository..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        sx={{ mb: 4 }}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <Search size={20} />
            </InputAdornment>
          ),
        }}
      />

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {filteredRepos.map((repo) => (
          <Paper key={repo.id} sx={{ p: 3 }}>
            <Grid2 container spacing={2}>
              <Grid2 item xs={12}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Code size={20} />
                  <Link
                    to={`/${repo.ownerUsername}/${repo.name}`}
                    style={{ textDecoration: 'none', color: 'inherit' }}
                  >
                    <Typography variant="h6" color="primary">
                      {repo.name}
                    </Typography>
                  </Link>
                  <Chip
                    label={repo.publicRepositoryOrNot ? 'Public' : 'Private'}
                    size="small"
                    sx={{ ml: 1 }}
                  />
                </Box>
              </Grid2>
              {repo.description && (
                <Grid2 item xs={12}>
                  <Typography variant="body2" color="text.secondary">
                    {repo.description}
                  </Typography>
                </Grid2>
              )}
              <Grid2 item xs={12}>
                <Box sx={{ display: 'flex', gap: 2 }}>
                  {repo.starCount > 0 && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <Star size={16} />
                      <Typography variant="body2">{repo.starCount}</Typography>
                    </Box>
                  )}
                  {repo.forkCount > 0 && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <GitFork size={16} />
                      <Typography variant="body2">{repo.forkCount}</Typography>
                    </Box>
                  )}
                </Box>
              </Grid2>
            </Grid2>
          </Paper>
        ))}
      </Box>

      {userData?.username === username &&
      <Dialog open={openNewRepo} onClose={() => setOpenNewRepo(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create a new repository</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <TextField
              label="Repository name"
              fullWidth
              value={newRepo.name}
              onChange={(e) => setNewRepo({ ...newRepo, name: e.target.value })}
              sx={{ mb: 2 }}
            />
            <TextField
              label="Description (optional)"
              fullWidth
              multiline
              rows={3}
              value={newRepo.description}
              onChange={(e) => setNewRepo({ ...newRepo, description: e.target.value })}
              sx={{ mb: 2 }}
            />
            <FormControlLabel
              control={
                <Switch
                  checked={newRepo.publicRepositoryOrNot}
                  onChange={(e) => setNewRepo({ ...newRepo, publicRepositoryOrNot: e.target.checked })}
                />
              }
              label={newRepo.publicRepositoryOrNot ? 'Public' : 'Private'}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenNewRepo(false)}>Cancel</Button>
          <Button onClick={handleCreateRepo} variant="contained" disabled={!newRepo.name}>
            Create repository
          </Button>
        </DialogActions>
      </Dialog>}
    </Container>
  );
};

export default RepositoriesPage;