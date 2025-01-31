import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import {
  Container,
  Typography,
  Box,
  Button,
  TextField,
  Paper,
  Grid2,
  InputAdornment,
  Chip,
} from '@mui/material';
import { Search, Star, GitFork, Code } from 'lucide-react';
import { showNotification } from '../../redux/notificationSlice';
import apiService from '../../services/apiService';
import CreateRepositoryForm from '../../globalComponents/forms/CreateRepositoryForm';
import { useConfirmationDialog } from '../../globalComponents/ConfirmationDialogContext';

const RepositoriesPage = () => {
  const { showDialog, hideDialog } = useConfirmationDialog();
  const [repos, setRepos] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [newRepo, setNewRepo] = useState({ name: '', description: '', publicRepositoryOrNot: true });
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);
  const [loading, setLoading] = useState(false);  // Track loading state
  const navigation = useSelector(state => state.navigation)
  const fetchRepos = async () => {
    try {
      const response = await apiService.fetchAllRepositories(navigation.repoUsername);
      setRepos(response.data);
    } catch (err) {
      dispatch(showNotification({ message: "Failed to fetch repositories.", type: "error" }));
    }
  };

  useEffect(() => {
    // Fetch repositories only if not already loading or fetched
    if (navigation.repoUsername && !loading) {
      setLoading(true);  // Set loading to true before fetch
      fetchRepos().finally(() => {
        setLoading(false);  // Reset loading after fetch
      });
    }
  }, [navigation.repoUsername]);

  const validateRepositoryForm = (newRepo) => {
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

  const openCreateRepositoryDialog = () => {
    let currentNewRepo = newRepo;

    const handleCurrentNewRepoChange = (repo) => {
      currentNewRepo = repo;
    }
    if (userData?.username === navigation.repoUsername){
      showDialog({
          title: "Create a new repository",
          message: (
            <CreateRepositoryForm
              newRepo={currentNewRepo}
              setNewRepo={handleCurrentNewRepoChange}
            />
          ),
          confirmText: "Create repository",
          onConfirm: ()  => handleCreateRepo(currentNewRepo),
        });
    }
  }

  const handleCreateRepo = async (newRepo) => {
    if (userData?.username === navigation.repoUsername) {
      try {
        const error = validateRepositoryForm(newRepo);

        if (error !== "") {
          dispatch(showNotification({ message: error, type: "error" }));
          return;
        }
        const response = await apiService.createRepository(
          newRepo.name, newRepo.description, userData.username, newRepo.publicRepositoryOrNot,
        );
        setRepos([...repos, response.data]);
        hideDialog();
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
          {navigation.repoUsername ? `${navigation.repoUsername}'s Repositories` : 'Your Repositories'}
        </Typography>
        {((navigation.repoUsername && userData?.username === navigation.repoUsername) || (!navigation.repoUsername)) && (
          <Button
            variant="contained"
            color="primary"
            onClick={openCreateRepositoryDialog}
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
    </Container>
  );
};

export default RepositoriesPage;