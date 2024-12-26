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
} from '@mui/material';
import {
  Star,
  GitFork,
  Eye,
  Code,
  GitBranch,
  Clock,
  FileText,
} from 'lucide-react';
import instance from '../services/axiosConfig';

const RepositoryPage = () => {
  const { username, repoName } = useParams();
  const [repo, setRepo] = useState(null);
  const [currentTab, setCurrentTab] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRepo = async () => {
      try {
        const response = await instance.get(`/repositories/${username}/${repoName}`);
        const modifiedData = {...response.data, isPublic: response.data.public};
        setRepo(modifiedData);
        console.log(modifiedData)
      } catch (err) {
        navigate('/error', { state: { code: 404 } });
      }
    };
    fetchRepo();
  }, [username, repoName]);

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
              variant="outlined"
              startIcon={<Star />}
              size="small"
            >
              Star
            </Button>
            <Button
              variant="outlined"
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
            <Button variant="contained" size="small">
              Add File
            </Button>
          </Box>
          
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            <Typography variant="body2" color="text.secondary">
              No files in this repository
            </Typography>
          </Box>
        </Paper>
      )}
    </Container>
  );
};

export default RepositoryPage;