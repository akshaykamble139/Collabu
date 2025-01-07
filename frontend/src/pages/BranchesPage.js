import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { Container, Box, Typography, TextField, Button, Paper, InputAdornment } from '@mui/material';
import { Search, GitBranch } from 'lucide-react';
import { showNotification } from "../redux/notificationSlice";
import apiService from '../services/apiService';
import CreateBranchForm from '../forms/CreateBranchForm';
import { hideConfirmationDialog, showConfirmationDialog } from '../redux/confirmationDialogSlice';

const BranchesPage = () => {
  const { username, repoName } = useParams();
  const [branches, setBranches] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [newBranch, setNewBranch] = useState({ name: '', sourceBranch: 'main' });
  const navigate = useNavigate();
  const dispatch = useDispatch();

   useEffect(() => {
      const fetchBranches = async () => {
        try {
          const response = await apiService.fetchBranches(username,repoName);
          setBranches(response.data);
        } catch (err) {
            navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
        }
      };
      fetchBranches();
    }, [username, repoName, navigate]);

  const validateBranchForm = (newBranch) => {
    let error = "";

    if (!newBranch.name) {
      error = "Branch name can't be empty";
    } else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(newBranch.name)) {
      error = "Branch name must start with an alphabet and contain only alphanumeric characters or underscores";
    }

    return error;
  };

  const handleCreateBranch = async (newBranch) => {
    try {
      const error = validateBranchForm(newBranch);

      if (error !== "") {
        dispatch(showNotification({ message: error, type: "error" }));
        return;
      }
      const response = await apiService.createBranch(newBranch.name,newBranch.sourceBranch, repoName);
      setBranches([...branches, response.data]);
      dispatch(hideConfirmationDialog());
      setNewBranch({ name: '', sourceBranch: 'main' });
      dispatch(showNotification({ message: "Branch created successfully!", type: "success" }));
    } catch (err) {
      dispatch(showNotification({ message: err.response?.data?.message || 'Failed to create branch.', type: "error" }));
    }
  };

  const openCreateBranchDialog = () => {
    let newBranchName = newBranch.name;
    let sourceBranchName = newBranch.sourceBranch;

    const handleBranchNameChange = (name) => {
      newBranchName = name
    };

    const handleSourceBranchChange = (sourceBranch) => {
      sourceBranchName = sourceBranch;
    };

    dispatch(
      showConfirmationDialog({
        title: "Create a new branch",
        message: (
          <CreateBranchForm
            branches={branches}
            newBranchName={newBranchName}
            sourceBranchName={sourceBranchName}
            onBranchNameChange={handleBranchNameChange}
            onSourceBranchChange={handleSourceBranchChange}
          />
        ),
        confirmText: "Create branch",
        onConfirm: () => handleCreateBranch({name: newBranchName, sourceBranch: sourceBranchName}),
  
      })
    );
  }

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
          onClick={openCreateBranchDialog}
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
              <Link
                to={`/${username}/${repoName}/tree/${branch.name}`}
                style={{ textDecoration: 'none', color: 'inherit' }}
              >
                <Typography variant="h6">
                  {branch.name}
                </Typography>
              </Link>
            </Box>
          </Paper>
        ))}
      </Box>
    </Container>
  );
};

export default BranchesPage;
