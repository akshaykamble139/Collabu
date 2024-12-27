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
  Alert,
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
} from 'lucide-react';
import instance from '../services/axiosConfig';
import { useSelector } from 'react-redux';

const RepositoryPage = () => {
  const { username, repoName } = useParams();
  const [repo, setRepo] = useState(null);
  const [currentTab, setCurrentTab] = useState(0);
  const [branches, setBranches] = useState([]);
  const [files, setFiles] = useState([]);
  const [showBranchDialog, setShowBranchDialog] = useState(false);
  const [newBranchName, setNewBranchName] = useState('');
  const [alertMessage, setAlertMessage] = useState(null);
  const [isStarred, setIsStarred] = useState(false);
  const navigate = useNavigate();
  const userData = useSelector(state => state.user);

  useEffect(() => {
    const fetchRepo = async () => {
      try {
        const response = await instance.get(`/repositories/${username}/${repoName}`);
        const modifiedData = {...response.data, isPublic: response.data.public};
        setRepo(modifiedData);
        console.log(modifiedData)

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
      await instance.post('/stars/toggle', { repositoryId: repo.id });
      setIsStarred((prev) => (!prev.isStarred));
      setAlertMessage({
        type: 'success',
        text: isStarred ? 'Repository unstarred!' : 'Repository starred!',
      });
    } catch (err) {
      setAlertMessage({ type: 'error', text: 'Failed to toggle star.' });
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
              {repo.isPublic ? 'Public' : 'Private'}
            </Typography>
          </Box>
          
          <Box sx={{ display: 'flex', gap: 1 }}>
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
    </Container>
  );
};

export default RepositoryPage;