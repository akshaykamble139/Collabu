import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Typography,
  Box,
  Paper,
  Breadcrumbs,
  Link,
  Button,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Code,
  Clock,
  Trash,
  Settings,
} from 'lucide-react';
import { useDispatch, useSelector } from 'react-redux';
import { showNotification } from '../redux/notificationSlice';
import apiService from '../services/apiService';
import { useConfirmationDialog } from '../globalComponents/ConfirmationDialogContext';
import RepositoryCode from './RepositoryCode';
const RepositoryPage = () => {
  const { username, repoName, branchName = 'main' } = useParams(); // Default to 'main'
  const filePath = window.location.pathname.split('/tree/')[1]?.split('/')?.slice(2).join('/');
  const { showDialog, hideDialog } = useConfirmationDialog();
  const [repo, setRepo] = useState(null);
  const [currentTab, setCurrentTab] = useState(0);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);

  useEffect(() => {
    const fetchRepo = async () => {
      try {
        const [repoResponse] = await Promise.all([
          apiService.fetchRepositoryData(username, repoName),
        ]);
        setRepo(repoResponse.data);
      } catch (err) {
        navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
      }
    };
    fetchRepo();
  }, [username, repoName, branchName]);

const openDeleteDialog = () => {
    if (userData?.username === username) {
        showDialog({
            title: "Delete Repository",
            component: () => (
            <Typography>
                {`Are you sure you want to delete the repository "${repo?.name}"? This action cannot be undone.`}
            </Typography>),
            confirmText: "Delete",
            onConfirm: handleDeleteRepo,
        });
    }
  };
      
  const handleDeleteRepo = async () => {
      if (userData?.username === username) {
          try {
              await apiService.deleteRepository(repo.id);
              dispatch(showNotification({ message: "Repository deleted successfully.", type: "success" }));
              hideDialog();
              navigate(`/${username}/repositories`);
          } catch (err) {
              dispatch(showNotification({ message: "Failed to delete repository.", type: "error" }));
          }
      }
  };
  
  if (!repo) return null;


  return (
    <Container maxWidth="lg" sx={{ py: 2 }}>
      <Breadcrumbs>
        <Link href={`/${username}/repositories`} underline="hover" color="inherit">
          {username}
        </Link>
        <Typography color="text.primary">{repoName}</Typography>
      </Breadcrumbs>
      <Tabs
        value={currentTab}
        onChange={(e, newValue) => setCurrentTab(newValue)}
        sx={{ mb: 3 }}
      >
        <Tab icon={<Code size={16} />} label="Code" iconPosition="start" />
        <Tab icon={<Clock size={16} />} label="Commits" iconPosition="start" />
        {userData?.username === username && <Tab icon={<Settings size={16} />} label="Settings" iconPosition="start" />}
      </Tabs>

      {currentTab === 0 && repo !== null && (
          <RepositoryCode 
            propRepo={repo} 
            propFilePath={filePath === null || filePath === undefined ? "/" : filePath[filePath.length - 1] !== '/' ? filePath+"/" : filePath}
          />
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