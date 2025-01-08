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
  FormControl,
  MenuItem,
  Select,
} from '@mui/material';
import {
  Star,
  GitFork,
  Code,
  GitBranch,
  Clock,
  Trash,
  Settings,
} from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import { showNotification } from '../redux/notificationSlice';
import apiService from '../services/apiService';
import { hideConfirmationDialog, showConfirmationDialog } from '../redux/confirmationDialogSlice';
import AddFileForm from '../globalComponents/forms/AddFileForm';
import ForkRepositoryForm from '../globalComponents/forms/ForkRepositoryForm';
const RepositoryPage = () => {
  const { username, repoName, branchName = 'main' } = useParams(); // Default to 'main'
  const [repo, setRepo] = useState(null);
  const [currentTab, setCurrentTab] = useState(0);
  const [branches, setBranches] = useState([]);
  const [files, setFiles] = useState([]);
  const [isStarred, setIsStarred] = useState(false);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);

  useEffect(() => {
    const fetchRepo = async () => {
      try {
        const [repoResponse, branchesResponse, filesResponse, starResponse] = await Promise.all([
          apiService.fetchRepositoryData(username,repoName),
          apiService.fetchBranches(username,repoName),
          apiService.fetchFilesFromBranch(username,repoName,branchName),
          apiService.fetchStarStatus(username,repoName),
        ]);
        setRepo(repoResponse.data);
        setBranches(branchesResponse.data);
        setFiles(filesResponse.data);
        setIsStarred(starResponse.data.isActive)
      } catch (err) {
        navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
      }
    };
    fetchRepo();
  }, [username, repoName, branchName]);

  const handleDeleteBranch = async (branchId) => {
    try {
      await apiService.deleteBranch(branchId);
      setBranches((prev) => prev.filter((branch) => branch.id !== branchId));
      dispatch(showNotification({ type: 'success', text: 'Branch deleted successfully!' }));
    } catch (err) {
      dispatch(showNotification({ type: 'error', text: 'Failed to delete branch.' }));
    }
  };

  const validateFileForm = (fileName, selectedFile) => {
    if (!fileName) return "File name can't be empty.";
    if (fileName.length > 100) return "File name should not exceed 100 characters";
    if (!/^[a-zA-Z][a-zA-Z0-9_.]*$/.test(fileName))
      return "File name must start with an alphabet and contain only alphanumeric characters, underscores, or dots.";
    if (!selectedFile) return "Please select a file.";
    return "";
  };

const handleFileUpload = async (fileName, commitMessage, file) => {
  const error = validateFileForm(fileName, file);
      if (error) {
        dispatch(showNotification({ message: error, type: "error" }));
        return;
      }
    const formData = new FormData();
    formData.append('file', file);
    const fileDTO = {
      'name': fileName,
      'repositoryName': repoName,
      'branchName': branchName,
      'commitMessage': commitMessage,
      'path': '/',
    }
    formData.append('fileDTO', JSON.stringify(fileDTO));  // Serialize the fileDTO object

    try {
      await apiService.uploadFile(formData);
      dispatch(showNotification({ type: 'success', text: 'File uploaded successfully!' }));
      dispatch(hideConfirmationDialog())
      window.location.reload();
    } catch (err) {
      dispatch(showNotification({ type: 'error', text: 'Failed to upload file.' }));
    }
  };

  const handleDeleteFile = async (fileId) => {
    try {
      await apiService.deleteFile(fileId);
      setFiles((prev) => prev.filter((file) => file.id !== fileId));
      dispatch(showNotification({ type: 'success', text: 'File deleted successfully!' }));
    } catch (err) {
      dispatch(showNotification({ type: 'error', text: 'Failed to delete file.' }));
    }
  };

  const handleToggleStar = async () => {
    try {
      const response = await apiService.toggleStar(username,repoName);
      setIsStarred(response.data === "Starred");
      setRepo(prev => ({ ...prev, starsCount: isStarred ? prev.starsCount - 1 : prev.starsCount + 1 }));
      dispatch(showNotification({
        type: 'success',
        text: isStarred ? 'Repository unstarred!' : 'Repository starred!',
      }));
      dispatch(showNotification({ message: isStarred ? 'Repository unstarred!' : 'Repository starred!', type: "success" }));

    } catch (err) {
      dispatch(showNotification({ message: err?.response?.data?.message || 'Failed to toggle star.', type: "error" }));
    }
  };

  const handleFork = async (forkedRepoName) => {
    try {
      let error = "";
  
      if (!forkedRepoName) {
        error = "Repository name can't be empty";
      } else if (forkedRepoName.length > 100) {
        error = "Repository name should not exceed 100 characters";
      } else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(forkedRepoName)) {
        error = "Repository name must start with an alphabet and contain only alphanumeric characters or underscores";
      }

      if (error) {
        dispatch(showNotification({ message: error, type: "error" }));
        return;
      }
      await apiService.forkRepository(repo.id,forkedRepoName);
      setRepo(prev => ({ ...prev, forksCount: prev.forksCount + 1 }));
      dispatch(showNotification({ type: 'success', message: 'Repository forked successfully!' }));
      dispatch(hideConfirmationDialog())
      navigate(`/${username}/repositories`);
    } catch (err) {
      dispatch(showNotification({ type: 'error', message: err?.response?.data?.message || 'Failed to fork repository.' }));
    }
  };

  const openForkDialog = () => {
    let newName = repoName;

    const handleNameChange = (name) => {
      newName = name;
    }

    dispatch(
      showConfirmationDialog({
        title: "Fork Repository",
        message: (
          <ForkRepositoryForm
              newRepoName={newName}
              setNewRepoName={handleNameChange}
            />
        ),
        confirmText: "Fork",
        onConfirm: () => handleFork(newName),
      })
    );
  };

  const openCreateFileDialog = () => {
    let fileName = "";
    let commitMessage = "Create ";
    let selectedFile = null;

    const handleFileChange = (file) => {
      selectedFile = file;
    };

    const handleFileNameChange = (name) => {
      fileName = name;
      commitMessage = `Create ${name}`;
    };

    const handleCommitMessageChange = (message) => {
      commitMessage = message;
    };
    dispatch(
      showConfirmationDialog({
        title: "Add a new file",
        message: (
          <AddFileForm
            onFileNameChange={handleFileNameChange}
            onCommitMessageChange={handleCommitMessageChange}
            onFileChange={handleFileChange}
          />
        ),
        confirmText: "Add File",
        onConfirm: () => handleFileUpload(fileName, commitMessage, selectedFile),
  
      })
    );
  }

  const setCurrentBranch = (event) => {
    const newBranch = event.target.value;
    if (newBranch !== branchName) {
      navigate(`/${username}/${repoName}/tree/${newBranch}`);
    }
  }

   const openDeleteDialog = () => {
    if (userData?.username === username) {
      dispatch(showConfirmationDialog({
        title: "Delete Repository",
        message: (
        <Typography>
          {`Are you sure you want to delete the repository "${repo?.name}"? This action cannot be undone.`}
        </Typography>),
        confirmText: "Delete",
        onConfirm: handleDeleteRepo,
      }));
    }
  };
  
    const handleDeleteRepo = async () => {
      if (userData?.username === username) {
        try {
          await apiService.deleteRepository(repo.id);
          dispatch(showNotification({ message: "Repository deleted successfully.", type: "success" }));
          dispatch(hideConfirmationDialog());
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
            <Button
              variant={isStarred ? 'contained' : 'outlined'}
              startIcon={<Star />}
              size="small"
              onClick={handleToggleStar}
            >
              { isStarred ? 'Unstar' : 'Star'}
            </Button>
            <Typography variant="body2"><GitFork size={16} /> {repo.forkCount} Forks</Typography>
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
        <Tab icon={<Clock size={16} />} label="Commits" iconPosition="start" />
        {userData?.username === username && <Tab icon={<Settings size={16} />} label="Settings" iconPosition="start" />}
      </Tabs>

      {currentTab === 0 && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <FormControl sx={{ minWidth: 120 }}>
              <Select
                value={branchName}
                onChange={setCurrentBranch}
              >
                {branches.map((branch) => (
                  <MenuItem key={branch.id} value={branch.name}>{branch.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <IconButton
                size="small"
              >
                <Link href={`/${username}/${repoName}/branches`}
                style={{ textDecoration: 'none', color: 'inherit' }}>
                <GitBranch size={24} />
                <Typography variant="caption" sx={{ ml: 0.5 }}>
                  {branches.length}
                </Typography>
                </Link>
              </IconButton>
            </Box>

            {userData?.username === username &&
            <Button variant="contained" component="label" onClick={openCreateFileDialog}>
              Add File
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
                <Link href={`/${username}/${repoName}/blob/${branchName}${file.path}${file.name}`} 
                    style={{ textDecoration: 'none', color: 'inherit' }}>
                    <ListItemText primary={file.name} />
                </Link>
              </ListItem>
            ))}
          </List>}
        </Paper>
      )}

      {userData?.username === username && currentTab === 2 && (
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
    </Container>
  );
};

export default RepositoryPage;