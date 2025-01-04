import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Container, Box, Typography, TextField, Button, Paper, InputAdornment } from '@mui/material';
import { Search, GitBranch } from 'lucide-react';
import instance from '../services/axiosConfig';
import { showNotification } from "../redux/notificationSlice";
import ConfirmationDialog from './ConfirmationDialog';

const BranchesPage = () => {
  const { username, repoName } = useParams();
  const [branches, setBranches] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [openNewBranch, setOpenNewBranch] = useState(false);
  const [newBranch, setNewBranch] = useState({ name: '', sourceBranch: 'main' });
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const userData = useSelector(state => state.user);

  const fetchBranches = async () => {
    try {
      const response = await instance.get(`/branches/${username}/${repoName}`);
      setBranches(response.data);
    } catch (err) {
        navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
    }
  };

   useEffect(() => {
      fetchBranches();
    }, []);

  const validateBranchForm = () => {
    let error = "";

    if (!newBranch.name) {
      error = "Branch name can't be empty";
    } else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(newBranch.name)) {
      error = "Branch name must start with an alphabet and contain only alphanumeric characters or underscores";
    }

    return error;
  };

  const handleCreateBranch = async () => {
    try {
      const error = validateBranchForm();

      if (error !== "") {
        dispatch(showNotification({ message: error, type: "error" }));
        return;
      }
      const response = await instance.post(`/branches`, {
        name: newBranch.name,
        parentBranchName: newBranch.sourceBranch,
        repositoryName: repoName,
      });
      setBranches([...branches, response.data]);
      setOpenNewBranch(false);
      setNewBranch({ name: '', sourceBranch: 'main' });
      dispatch(showNotification({ message: "Branch created successfully!", type: "success" }));
    } catch (err) {
      dispatch(showNotification({ message: err.response?.data?.message || 'Failed to create branch.', type: "error" }));
    }
  };

  const filteredBranches = branches.filter(branch =>
    branch.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4">
          {repoName} Branches
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={() => setOpenNewBranch(true)}
        >
          New Branch
        </Button>
      </Box>

      <TextField
        fullWidth
        variant="outlined"
        placeholder="Find a branch..."
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
        {filteredBranches.map((branch) => (
          <Paper key={branch.id} sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <GitBranch size={20} />
              <Typography variant="h6">
                {branch.name}
              </Typography>
            </Box>
          </Paper>
        ))}
      </Box>

      <ConfirmationDialog
        open={openNewBranch}
        onClose={() => setOpenNewBranch(false)}
        onConfirm={handleCreateBranch}
        title="Create a new branch"
        confirmText="Create branch"
      >
        <Box sx={{ mt: 2 }}>
          <TextField
            label="Branch name"
            fullWidth
            value={newBranch.name}
            onChange={(e) => setNewBranch({ ...newBranch, name: e.target.value })}
            sx={{ mb: 2 }}
          />
          <TextField
            label="Source branch"
            fullWidth
            value={newBranch.sourceBranch}
            onChange={(e) => setNewBranch({ ...newBranch, sourceBranch: e.target.value })}
            sx={{ mb: 2 }}
          />
        </Box>
      </ConfirmationDialog>
    </Container>
  );
};

export default BranchesPage;
