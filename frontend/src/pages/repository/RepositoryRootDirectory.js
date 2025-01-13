import { Box, Button, Divider, FormControl, IconButton, List, ListItem, ListItemText, MenuItem, Paper, Select, Typography } from "@mui/material";
import { Code, GitBranch, GitFork, Star, File, Folder } from "lucide-react";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useConfirmationDialog } from "../../globalComponents/ConfirmationDialogContext";
import { showNotification } from "../../redux/notificationSlice";
import apiService from "../../services/apiService";
import ForkRepositoryForm from "../../globalComponents/forms/ForkRepositoryForm";

const RepositoryRootDirectory = ({propRepo, setCurrentBranch, openCreateFileDialog, branches}) => {
    const [repo, setRepo] = useState(propRepo);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [files, setFiles] = useState([]);
    const userData = useSelector(state => state.user);
    const location = useLocation();
    const navigation = useSelector(state => state.navigation)
    const [isStarred, setIsStarred] = useState(false);
    const { showDialog, hideDialog } = useConfirmationDialog();

    useEffect(() => {
        const fetchRepo = async () => {
            try {   
                    if (navigation.repoUsername && navigation.repoName && navigation.repoBranchName && !location.pathname.includes("blob") && navigation.currentPath === "/") {
                        const filesResponse = await apiService.fetchTreeStructureOfFiles(navigation.repoUsername, navigation.repoName, navigation.repoBranchName, navigation.currentPath);
                        setFiles(filesResponse.data?.children);

                        const starResponse = await apiService.fetchStarStatus(navigation.repoUsername, navigation.repoName);
                        setIsStarred(starResponse.data.isActive)
                    }
            
            } catch (err) {
                navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
            }
        };
        if (navigation.repoUsername && navigation.repoName && navigation.repoBranchName) {
            fetchRepo();
        }
    }, [navigation.repoUsername, navigation.repoName, navigation.repoBranchName, navigation.currentPath, location.pathname]);


    const handleToggleStar = async () => {
        try {
            const response = await apiService.toggleStar(navigation.repoUsername, navigation.repoName);
            setIsStarred(response.data === "Starred");
            setRepo(prev => ({ ...prev, starsCount: isStarred ? prev.starsCount - 1 : prev.starsCount + 1 }));
            dispatch(showNotification({
                type: 'success',
                message: isStarred ? 'Repository unstarred!' : 'Repository starred!',
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
            navigate(`/${navigation.repoUsername}/repositories`);
        } catch (err) {
            dispatch(showNotification({ type: 'error', message: err?.response?.data?.message || 'Failed to fork repository.' }));
        }
    };

    const openForkDialog = () => {
        let newName = navigation.repoName;
    
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
    
    return (
        <>
        {navigation.currentPath === "/" && !location.pathname.includes("blob") && repo && branches.length > 0 && 

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
                    disabled={userData?.username === navigation.repoUsername}
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
                    value={navigation.repoBranchName}
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
                    <Link to={`/${navigation.repoUsername}/${navigation.repoName}/branches`}
                        style={{ textDecoration: 'none', color: 'inherit' }}>
                        <GitBranch size={24} />
                        <Typography variant="caption" sx={{ ml: 0.5 }}>
                            {branches.length}
                        </Typography>
                    </Link>
                </IconButton>
                </Box>

                {userData?.username === navigation.repoUsername &&
                <Button variant="contained" component="label" onClick={openCreateFileDialog}>
                    Add File
                </Button>}
            </Box>
                {files != null && files.length > 0 &&
                <List>
                {files.map((file) => (
                    <ListItem key={file.id} >
                        <Link to={file.type === "folder" ? 
                        `/${navigation.repoUsername}/${navigation.repoName}/tree/${navigation.repoBranchName}${file.path}${file.name}`
                        : `/${navigation.repoUsername}/${navigation.repoName}/blob/${navigation.repoBranchName}${file.path}${file.name}`}
                            style={{ textDecoration: 'none', color: 'inherit' }}>
                            <ListItemText
                                primary={
                                <span style={{ display: 'flex', alignItems: 'center' }}>
                                    {file.type === "folder" ? 
                                    <Folder size={20} style={{ marginRight: 8 }} /> :
                                    <File size={20} style={{ marginRight: 8 }} /> } {file.name}
                                </span>
                                }
                            />
                        </Link>
                    </ListItem>
                ))}
                </List>}

            </Paper>}
        </>
    );
}

export default RepositoryRootDirectory;