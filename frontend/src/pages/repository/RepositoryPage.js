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
import { showNotification } from '../../redux/notificationSlice';
import apiService from '../../services/apiService';
import { useConfirmationDialog } from '../../globalComponents/ConfirmationDialogContext';
import RepositoryCode from './RepositoryCode';
const RepositoryPage = () => {

  const navigation = useSelector(state => state.navigation)
  const { showDialog, hideDialog } = useConfirmationDialog();
  const [repo, setRepo] = useState(null);
  const [currentTab, setCurrentTab] = useState(0);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);

  useEffect(() => {
    console.log("RepositoryPage",navigation.repoUsername, navigation.repoName);
    const fetchRepo = async () => {
      try {
        const [repoResponse] = await Promise.all([
          apiService.fetchRepositoryData(navigation.repoUsername, navigation.repoName),
        ]);
        setRepo(repoResponse.data);
      } catch (err) {
        navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
      }
    }
    if (navigation.repoUsername && navigation.repoName) {
      fetchRepo();
    }
    
  }, [navigation.repoUsername, navigation.repoName]);

const openDeleteDialog = () => {
    if (userData?.username === navigation.repoUsername) {
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
      if (userData?.username === navigation.repoUsername) {
          try {
              await apiService.deleteRepository(repo.id);
              dispatch(showNotification({ message: "Repository deleted successfully.", type: "success" }));
              hideDialog();
              navigate(`/${navigation.repoUsername}/repositories`);
          } catch (err) {
              dispatch(showNotification({ message: "Failed to delete repository.", type: "error" }));
          }
      }
  };
  
  if (!repo) return null;


  return (
    <Container maxWidth="lg" sx={{ py: 4, px: 3 }}>
      <Breadcrumbs sx={{ mb: 3 }}>
        <Link href={`/${navigation.repoUsername}/repositories`} underline="hover" color="inherit">
          {navigation.repoUsername}
        </Link>
        <Typography color="text.primary" fontWeight="500">{navigation.repoName}</Typography>
      </Breadcrumbs>
      <Tabs
        value={currentTab}
        onChange={(e, newValue) => setCurrentTab(newValue)}
        sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
      >
        <Tab icon={<Code size={16} />} label="Code" iconPosition="start" />
        <Tab icon={<Clock size={16} />} label="Commits" iconPosition="start" />
        {userData?.username === navigation.repoUsername && <Tab icon={<Settings size={16} />} label="Settings" iconPosition="start" />}
      </Tabs>

      {currentTab === 0 && repo !== null && (
          <RepositoryCode 
            propRepo={repo} 
          />
      )}

      {userData?.username === navigation.repoUsername && currentTab === 2 && (
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