import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Breadcrumbs,
  Link,
  IconButton,
  Button,
  Divider,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  Dialog,
  DialogTitle,
  DialogContent,
  TextField,
  DialogActions,
} from '@mui/material';
import {
  Star,
  GitFork,
  Eye,
  Code,
  GitBranch,
  Clock,
  FileText,
  Trash,
  Upload,
  Settings,
} from 'lucide-react';
import instance from '../services/axiosConfig';
import { useDispatch, useSelector } from 'react-redux';
import { showNotification } from '../redux/notificationSlice';
import ConfirmationDialog from './ConfirmationDialog';

const RepositoryPage = () => {
  const { username, repoName } = useParams();
  const [repo, setRepo] = useState(null);
  const [currentTab, setCurrentTab] = useState(0);
  const [branches, setBranches] = useState([]);
  const [files, setFiles] = useState([]);
  const [showBranchDialog, setShowBranchDialog] = useState(false);
  const [newBranchName, setNewBranchName] = useState('');
  const [alertMessage, setAlertMessage] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [forkDialogOpen, setForkDialogOpen] = useState(false);
  const [forkedRepoName, setForkRepoName] = useState(repoName);
  const [isStarred, setIsStarred] = useState(false);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);

  useEffect(() => {
    const fetchRepo = async () => {
      try {
        const response = await instance.get(`/repositories/${username}/${repoName}`);
        setRepo(response.data);

        // Fetch branches and files
        const branchesResponse = await instance.get(`/branches/repository/${response.data.id}`);
        const filesResponse = await instance.get(`/files/repository/${response.data.id}`);
        setBranches(branchesResponse.data);
        setFiles(filesResponse.data);

        const starResponse = await instance.post('/stars/status', { repositoryId: response.data.id });  
        setIsStarred(starResponse.data.isActive)
      } catch (err) {
        navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
      }
    };
    fetchRepo();
  }, [username, repoName]);

  const handleCreateBranch = async () => {
    try {
      const response = await instance.post('/branches', {
        name: newBranchName,
        repositoryId: repo.id,
      });
      setBranches((prev) => [...prev, response.data]);
      setShowBranchDialog(false);
      setNewBranchName('');
      setAlertMessage({ type: 'success', text: 'Branch created successfully!' });
    } catch (err) {
      setAlertMessage({ type: 'error', text: 'Failed to create branch.' });
    }
  };

  const handleDeleteBranch = async (branchId) => {
    try {
      await instance.delete(`/branches/${branchId}`);
      setBranches((prev) => prev.filter((branch) => branch.id !== branchId));
      setAlertMessage({ type: 'success', text: 'Branch deleted successfully!' });
    } catch (err) {
      setAlertMessage({ type: 'error', text: 'Failed to delete branch.' });
    }
  };

  const handleFileUpload = async (event) => {
    const formData = new FormData();
    formData.append('file', event.target.files[0]);
    formData.append('repositoryId', repo.id);
    try {
      const response = await instance.post('/files', formData);
      setFiles((prev) => [...prev, response.data]);
      setAlertMessage({ type: 'success', text: 'File uploaded successfully!' });
    } catch (err) {
      setAlertMessage({ type: 'error', text: 'Failed to upload file.' });
    }
  };

  const handleDeleteFile = async (fileId) => {
    try {
      await instance.delete(`/files/${fileId}`);
      setFiles((prev) => prev.filter((file) => file.id !== fileId));
      setAlertMessage({ type: 'success', text: 'File deleted successfully!' });
    } catch (err) {
      setAlertMessage({ type: 'error', text: 'Failed to delete file.' });
    }
  };

  const handleToggleStar = async () => {
    try {
      const response = await instance.post('/stars/toggle', { repositoryId: repo.id });
      setIsStarred(response.data === "Starred");
      setRepo(prev => ({ ...prev, starsCount: isStarred ? prev.starsCount - 1 : prev.starsCount + 1 }));
      setAlertMessage({
        type: 'success',
        text: isStarred ? 'Repository unstarred!' : 'Repository starred!',
      });
      dispatch(showNotification({ message: isStarred ? 'Repository unstarred!' : 'Repository starred!', type: "success" }));

    } catch (err) {
      dispatch(showNotification({ message: err?.response?.data?.message || 'Failed to toggle star.', type: "error" }));
    }
  };

  const handleFork = async () => {
    try {
      const response = await instance.post('/repositories/fork', { repositoryId: repo.id, name: forkedRepoName });
      setRepo(prev => ({ ...prev, forksCount: prev.forksCount + 1 }));
      dispatch(showNotification({ type: 'success', message: 'Repository forked successfully!' }));
      setForkDialogOpen(false);
      navigate(`/${username}/repositories`);
    } catch (err) {
      dispatch(showNotification({ type: 'error', message: err?.response?.data?.message || 'Failed to fork repository.' }));
    }
  };

  const openForkDialog = () => {
    setForkDialogOpen(true);
  };

   const openDeleteDialog = () => {
      setDeleteDialogOpen(true);
    };
  
    const handleDeleteRepo = async () => {
      if (userData?.username === username) {
        try {
          await instance.delete(`/repositories/${repo.id}`);
          dispatch(showNotification({ message: "Repository deleted successfully.", type: "success" }));
          setDeleteDialogOpen(false);
          navigate(`/${username}/repositories`);
        } catch (err) {
          dispatch(showNotification({ message: "Failed to delete repository.", type: "error" }));
        }
      }
    };

  if (!repo) return null;


  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Breadcrumbs>
          <Link href={`/${username}/repositories`} underline="hover" color="inherit">
            {username}
          </Link>
          <Typography color="text.primary">{repoName}</Typography>
        </Breadcrumbs>

        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Code size={24} />
            <Typography variant="h4">{repo.name}</Typography>
            <Typography
              variant="caption"
              sx={{
                px: 1,
                py: 0.5,
                border: 1,
                borderRadius: 1,
                borderColor: 'divider',
              }}
            >
              {repo.publicRepositoryOrNot ? 'Public' : 'Private'}
            </Typography>
          </Box>
          
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
            <Typography variant="body2"><Star size={16} /> {repo.starCount} Stars</Typography>
            <Typography variant="body2"><GitFork size={16} /> {repo.forkCount} Forks</Typography>
            <Button
              variant={isStarred ? 'contained' : 'outlined'}
              startIcon={<Star />}
              size="small"
              onClick={handleToggleStar}
            >
              { isStarred ? 'Unstar' : 'Star'}
            </Button>
            <Button
              variant={repo.isForked ? 'contained' : 'outlined'}
              disabled={userData?.username === username}
              startIcon={<GitFork />}
              size="small"
              onClick={openForkDialog}
            >
              Fork
            </Button>
          </Box>
        </Box>

        {repo.description && (
          <Typography variant="body1" color="text.secondary" sx={{ mt: 2 }}>
            {repo.description}
          </Typography>
        )}
      </Box>

      <Divider />

      <Tabs
        value={currentTab}
        onChange={(e, newValue) => setCurrentTab(newValue)}
        sx={{ mb: 3 }}
      >
        <Tab icon={<Code size={16} />} label="Code" iconPosition="start" />
        <Tab icon={<GitBranch size={16} />} label="Branches" iconPosition="start" />
        <Tab icon={<Clock size={16} />} label="Commits" iconPosition="start" />
        {userData?.username === username && <Tab icon={<Settings size={16} />} label="Settings" iconPosition="start" />}
      </Tabs>

      {currentTab === 0 && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Button
                variant="outlined"
                startIcon={<GitBranch />}
                size="small"
              >
                main
              </Button>
            </Box>
            {userData?.username === username &&
            <Button variant="contained" component="label">
              Add File
              <input hidden type="file" onChange={handleFileUpload} />
            </Button>}
          </Box>
          {files.length > 0 &&
          <List>
            {files.map((file) => (
              <ListItem
                key={file.id}
                secondaryAction={
                   userData?.username === username ?
                  <IconButton
                    edge="end"
                    aria-label="delete"
                    onClick={() => handleDeleteFile(file.id)}
                  >
                    <Trash size={16} />
                  </IconButton> : null
                }
              >
                <ListItemText primary={file.name} />
              </ListItem>
            ))}
          </List>}
        </Paper>
      )}

      {userData?.username === username && currentTab === 3 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" color="error" sx={{ mb: 2 }}>
            Danger Zone
          </Typography>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography>Delete this repository</Typography>
            <Button
              variant="contained"
              color="error"
              onClick={openDeleteDialog}
              startIcon={<Trash />}
            >
              Delete
            </Button>
          </Box>
        </Paper>
      )}

    <Dialog open={forkDialogOpen} onClose={() => setForkDialogOpen(false)}>
      <DialogTitle>Fork Repository</DialogTitle>
      <DialogContent>
        <TextField
          label="Repository Name"
          value={repoName}
          onChange={(e) => setForkRepoName(e.target.value)}
          fullWidth
          margin="dense"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={() => setForkDialogOpen(false)} color="inherit">Cancel</Button>
        <Button onClick={handleFork} variant="contained">Fork</Button>
      </DialogActions>
    </Dialog>

    {userData?.username === username &&
      <ConfirmationDialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeleteRepo}
        title="Delete Repository"
        message={`Are you sure you want to delete the repository "${repo?.name}"? This action cannot be undone.`}
      />}
    </Container>
  );
};

export default RepositoryPage;