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
  TextField,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
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
  const [currentBranch, setCurrentBranch] = useState('main');
  const [showFileCreateDialog, setShowFileCreateDialog] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [newFileName, setNewFileName] = useState('');
  const [commitMessage, setCommitMessage] = useState('Create ' + newFileName);
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
        const filesResponse = await instance.get(`/files/${username}/${repoName}/${currentBranch}`);
        setBranches(branchesResponse.data);
        setFiles(filesResponse.data);

        const starResponse = await instance.post('/stars/status', { repositoryId: response.data.id });  
        setIsStarred(starResponse.data.isActive)
      } catch (err) {
        navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
      }
    };
    fetchRepo();
  }, [username, repoName, currentBranch]);

  const handleDeleteBranch = async (branchId) => {
    try {
      await instance.delete(`/branches/${branchId}`);
      setBranches((prev) => prev.filter((branch) => branch.id !== branchId));
      dispatch(showNotification({ type: 'success', text: 'Branch deleted successfully!' }));
    } catch (err) {
      dispatch(showNotification({ type: 'error', text: 'Failed to delete branch.' }));
    }
  };

  const handleFileChange = (e) => {
    setSelectedFile(e.target.files[0]);
  };

  const validateFileForm = () => {
    let error = "";
  
    if (!newFileName) {
      error = "File name can't be empty";
    } else if (newFileName.length > 100) {
      error = "File name should not exceed 100 characters";
    } else if (!/^[a-zA-Z][a-zA-Z0-9_.]*$/.test(newFileName)) {
      error = "File name must start with an alphabet and contain only alphanumeric characters or underscores";
    }

    if (selectedFile == null) {
      error = "Please select a file."
    }

    return error;
  };

  const handleFileUpload = async () => {
    const error = validateFileForm();
    if (error !== "") {
      dispatch(showNotification({ message: error, type: "error" }));
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);
    const fileDTO = {
      'name': newFileName,
      'repositoryName': repoName,
      'branchName': currentBranch,
      'commitMessage': commitMessage,
      'path': '/',
    }
    formData.append('fileDTO', JSON.stringify(fileDTO));  // Serialize the fileDTO object

    try {
      await instance.post("/files", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      dispatch(showNotification({ type: 'success', text: 'File uploaded successfully!' }));
      setShowFileCreateDialog(files)
      window.location.reload();
    } catch (err) {
      dispatch(showNotification({ type: 'error', text: 'Failed to upload file.' }));
    }
  };

  const handleDeleteFile = async (fileId) => {
    try {
      await instance.delete(`/files/${fileId}`);
      setFiles((prev) => prev.filter((file) => file.id !== fileId));
      dispatch(showNotification({ type: 'success', text: 'File deleted successfully!' }));
    } catch (err) {
      dispatch(showNotification({ type: 'error', text: 'Failed to delete file.' }));
    }
  };

  const handleToggleStar = async () => {
    try {
      const response = await instance.post('/stars/toggle', { repositoryId: repo.id });
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
                value={currentBranch}
                onChange={(event) => {setCurrentBranch(event.target.value);}}
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
            <Button variant="contained" component="label" onClick={() => {setShowFileCreateDialog(true)}}>
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
                <ListItemText primary={file.name} />
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
      <ConfirmationDialog
          open={forkDialogOpen}
          onClose={() => setForkDialogOpen(false)}
          onConfirm={handleFork}
          title="Fork Repository"
          confirmText="Fork"
        >
          <TextField
            label="Repository Name"
            value={repoName}
            onChange={(e) => setForkRepoName(e.target.value)}
            fullWidth
            margin="dense"
          />
      </ConfirmationDialog>

      <ConfirmationDialog
          open={showFileCreateDialog}
          onClose={() => setShowFileCreateDialog(false)}
          onConfirm={handleFileUpload}
          title="Add a new file"
          confirmText="Add File"
        >
          <Box sx={{ mt: 2 }}>
            <TextField
              label="File name"
              fullWidth
              value={newFileName}
              onChange={(e) => {setNewFileName(e.target.value ); setCommitMessage("Create " + e.target.value)}}
              sx={{ mb: 2 }}
            />
            <TextField
              label="Commit Message"
              fullWidth
              multiline
              rows={3}
              value={commitMessage}
              onChange={(e) => setCommitMessage(e.target.value)}
              sx={{ mb: 2 }}
            />
            
              
              <input type="file" onChange={handleFileChange} />
          </Box>
      </ConfirmationDialog>
    {userData?.username === username &&
      <ConfirmationDialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeleteRepo}
        title="Delete Repository"
        confirmText="Delete"
      >
        <Typography>
          {`Are you sure you want to delete the repository "${repo?.name}"? This action cannot be undone.`}
        </Typography>
      </ConfirmationDialog>}
    </Container>
  );
};

export default RepositoryPage;