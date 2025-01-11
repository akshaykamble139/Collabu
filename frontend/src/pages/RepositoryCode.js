import { Box, Breadcrumbs, Button, Divider, FormControl, IconButton, List, ListItem, ListItemText, MenuItem, Paper, Select, Typography } from "@mui/material";
import { Code, GitBranch, GitFork, Star, Trash, File, Folder, ListIcon } from "lucide-react";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { showNotification } from "../redux/notificationSlice";
import apiService from "../services/apiService";
import { useConfirmationDialog } from "../globalComponents/ConfirmationDialogContext";
import ForkRepositoryForm from "../globalComponents/forms/ForkRepositoryForm";
import AddFileForm from "../globalComponents/forms/AddFileForm";
import FileViewerPage from "./FileViewerPage";
import TreeStructure from "./TreeStructure";

const RepositoryCode = ({propRepo, propFilePath}) => {
    
    const [repo, setRepo] = useState(propRepo);
    const [filePath, setFilePath] = useState(propFilePath);
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

    useEffect(() => {
        const path = location.pathname

        if (path.includes("blob")) {
            const first = path.split("/blob/");
            const values = first[0].split("/");
            setUsername(values[1]);
            setRepoName(values[2]);
            const suffix = first[1].split("/");
            setBranchName(suffix[0]);
            setFilePath("/" + suffix.slice(1,suffix.length).join("/"));
        }
        else if (!(path.includes("tree"))) {
            const values = path.split("/");
            setUsername(values[1]);
            setRepoName(values[2]);
            setFilePath("/");
        }
        else if (path.includes("tree")) {
            const first = path.split("/tree/");
            const values = first[0].split("/");
            setUsername(values[1]);
            setRepoName(values[2]);
            const suffix = first[1].split("/");
            setBranchName(suffix[0]);
            setFilePath("/" + suffix.slice(1,suffix.length).join("/"));
        }
    }, [location.pathname])

    useEffect(() => {
        const fetchRepo = async () => {
            try {
                    const branchesResponse = await apiService.fetchBranches(username, repoName);
                    setBranches(branchesResponse.data);

    
                    if (!location.pathname.includes("blob")) {
                        const filesResponse = await apiService.fetchFilesFromBranch(username, repoName, branchName, filePath[0] !== "/" ? "/" + filePath : filePath );
                        setFiles(filesResponse.data);

                        if (location.pathname.includes("blob") && filesResponse.data.type !== "file") {
                            dispatch(showNotification({message: "Invalid path for blob route. Expected a file.", type: "error"}));
                            navigate('/error', { state: { code: 404 } });
                        }  

                        const starResponse = await apiService.fetchStarStatus(username, repoName);
                        setIsStarred(starResponse.data.isActive)
                    }
            
            } catch (err) {
                navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
            }
        };
        if (username && repoName && branchName) {
            fetchRepo();
        }
    }, [username, repoName, branchName, filePath, location.pathname]);

    const handleToggleStar = async () => {
        try {
            const response = await apiService.toggleStar(username, repoName);
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
    


    const handleDeleteBranch = async (branchId) => {
        try {
            await apiService.deleteBranch(branchId);
            setBranches((prev) => prev.filter((branch) => branch.id !== branchId));
            dispatch(showNotification({ type: 'success', message: 'Branch deleted successfully!' }));
        } catch (err) {
            dispatch(showNotification({ type: 'error', message: 'Failed to delete branch.' }));
        }
    };

    const validateFileForm = (fileName, selectedFile) => {
        if (!fileName) return "File name can't be empty.";
        if (fileName.length > 200) return "File path should not exceed 200 characters.";
        if (!/^[a-zA-Z][a-zA-Z0-9_/.-]*$/.test(fileName))
            return "File path must start with an alphabet and contain only alphanumeric characters, underscores, dots, or slashes.";
        if (!selectedFile) return "Please select a file.";
        if (fileName[fileName.length-1] === '/') return "File name can't be empty.";

        const arr = fileName.split('/');

        for (let index = 0; index < arr.length; index++) {
            const element = arr[index];
            if ((!/^[a-zA-Z][a-zA-Z0-9_.-]*$/.test(element))) {
                return "Folder name must start with an alphabet and contain only alphanumeric characters, underscores, dots, or slashes.";
            }            
        }
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

        const arr = fileName.split('/');
        const filePath = arr.slice(0,arr.length-1).join('/');
        const fileDTO = {
            'name': arr[arr.length-1],
            'repositoryName': repoName,
            'branchName': branchName,
            'commitMessage': commitMessage,
            'path': propFilePath + filePath,
        }
        formData.append('fileDTO', JSON.stringify(fileDTO));  // Serialize the fileDTO object
    
        try {
            await apiService.uploadFile(formData);
            dispatch(showNotification({ type: 'success', message: 'File uploaded successfully!' }));
            hideDialog()
            window.location.reload();
        } catch (err) {
            dispatch(showNotification({ type: 'error', message: 'Failed to upload file.' }));
        }
      };
    
    const handleDeleteFile = async (fileId) => {
        try {
            await apiService.deleteFile(fileId);
            setFiles((prev) => prev.filter((file) => file.id !== fileId));
            dispatch(showNotification({ type: 'success', message: 'File deleted successfully!' }));
        } catch (err) {
            dispatch(showNotification({ type: 'error', message: 'Failed to delete file.' }));
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
            {filePath === "/" && !location.pathname.includes("blob") && repo && branches.length > 0 && 
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
            {files != null && files.length > 0 &&
                <List>
                {files.map((file) => (
                    <ListItem key={file.id}>
                        <Link to={file.type === "folder" ? 
                        `/${username}/${repoName}/tree/${branchName}${file.path}${file.name}`
                        : `/${username}/${repoName}/blob/${branchName}${file.path}${file.name}`}
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
            {(filePath !== "/" || location.pathname.includes("blob")) && repo && branches.length > 0 && (
            <Box sx={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
                <Box sx={{ flexGrow: 1 }}>
                    <Breadcrumbs>
                        <Link to={`/${username}/repositories`} 
                        style={{ textDecoration: 'none', color: 'inherit' }}>
                        {username}
                        </Link>
                        <Typography color="text.primary">{repoName}</Typography>
                    </Breadcrumbs>     
                </Box>
                <Box sx={{ display: 'flex', flexGrow: 1 }}>
                <TreeStructure files={files} username={username} repoName={repoName} branchName={branchName} />

                {location.pathname.includes("tree") && filePath !== "/" ? 
                <Paper>
                    <Box>
                                
                    </Box>
                    <Divider sx={{mb:2}}/>
                    {files.length > 0 &&
                    <List>
                    {files.map((file) => (
                        <ListItem key={file.id}>
                            <Link to={file.type === "folder" ? 
                            `/${username}/${repoName}/tree/${branchName}${file.path}${file.name}`
                            : `/${username}/${repoName}/blob/${branchName}${file.path}${file.name}`}
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

                </Paper> :
                location.pathname.includes("blob") && 
                <>
                    <FileViewerPage 
                        username={username}
                        repoName={repoName}
                        branchName={branchName}
                    />
                </> }
                </Box>
            </Box>
            )}
        </>
    );
};

export default RepositoryCode;
