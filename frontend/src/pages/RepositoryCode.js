import { Box, Button, Divider, FormControl, IconButton, List, ListItem, ListItemText, MenuItem, Paper, Select, Typography } from "@mui/material";
import { Code, GitBranch, GitFork, Star, Trash } from "lucide-react";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { showNotification } from "../redux/notificationSlice";
import apiService from "../services/apiService";
import { useConfirmationDialog } from "../globalComponents/ConfirmationDialogContext";
import ForkRepositoryForm from "../globalComponents/forms/ForkRepositoryForm";
import AddFileForm from "../globalComponents/forms/AddFileForm";

const RepositoryCode = ({propRepo}) => {
    
    const [repo, setRepo] = useState(propRepo);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [branches, setBranches] = useState([]);
    const [files, setFiles] = useState([]);
    const userData = useSelector(state => state.user);
    const location = useLocation();
    const [username, setUsername] = useState("");
    const [repoName, setRepoName] = useState("");
    const [branchName, setBranchName] = useState("main");
    const [isStarred, setIsStarred] = useState(false);
    const { showDialog, hideDialog } = useConfirmationDialog();

    const handleToggleStar = async () => {
        try {
            const response = await apiService.toggleStar(username, repoName);
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
            await apiService.forkRepository(repo.id, forkedRepoName);
            setRepo(prev => ({ ...prev, forksCount: prev.forksCount + 1 }));
            dispatch(showNotification({ type: 'success', message: 'Repository forked successfully!' }));
            hideDialog()
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
    
        showDialog({
            title: "Fork Repository",
            component: () => (
                <ForkRepositoryForm
                newRepoName={newName}
                setNewRepoName={handleNameChange}
                />
            ),
            confirmText: "Fork",
            onConfirm: () => handleFork(newName),
        })
    };
    
    useEffect(() => {
        const path = location.pathname

        if (path.includes("blob")) {
            const first = path.split("/blob/");
            const values = first[0].split("/");
            setUsername(values[1]);
            setRepoName(values[2]);
            const suffix = first[1].split("/");
            setBranchName(suffix[0]);
        }
        else if (!(path.includes("tree"))) {
            const values = path.split("/");
            setUsername(values[1]);
            setRepoName(values[2])
        }
        else if (path.includes("tree")) {
            const first = path.split("/tree/");
            const values = first[0].split("/");
            setUsername(values[1]);
            setRepoName(values[2]);
            const suffix = first[1].split("/");
            setBranchName(suffix[0]);
        }
    }, [location.pathname])

    useEffect(() => {
        const fetchRepo = async () => {
            try {
                const [branchesResponse, filesResponse, starResponse] = await Promise.all([
                    apiService.fetchBranches(username, repoName),
                    apiService.fetchFilesFromBranch(username, repoName, branchName),
                    apiService.fetchStarStatus(username, repoName),
                ]);
                setBranches(branchesResponse.data);
                setFiles(filesResponse.data);
                setIsStarred(starResponse.data.isActive)
            } catch (err) {
                navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
            }
        };
        if (username && repoName && branchName) {
            fetchRepo();
        }
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
            hideDialog()
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
        showDialog({
            title: "Add a new file",
            component: () => (
                <AddFileForm
                onFileNameChange={handleFileNameChange}
                onCommitMessageChange={handleCommitMessageChange}
                onFileChange={handleFileChange}
                />
            ),
            confirmText: "Add File",
            onConfirm: () => handleFileUpload(fileName, commitMessage, selectedFile),
        });
      }
    
    const setCurrentBranch = (event) => {
        const newBranch = event.target.value;
        if (newBranch !== branchName) {
            navigate(`/${username}/${repoName}/tree/${newBranch}`);
        }
    }
        
    return (
        <>
        {repo && branches.length > 0 && 
            <Paper sx={{ px: 2, py: 1 }}>
            <Box sx={{ mb: 2 }}>


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
                    {isStarred ? 'Unstar' : 'Star'}
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

            <Divider sx={{mb:2}}/>
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
                    <Link to={`/${username}/${repoName}/branches`}
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
                    <Link to={`/${username}/${repoName}/blob/${branchName}${file.path}${file.name}`}
                        style={{ textDecoration: 'none', color: 'inherit' }}>
                        <ListItemText primary={file.name} />
                    </Link>
                    </ListItem>
                ))}
                </List>}
            </Paper>}
        </>
    );
};

export default RepositoryCode;
