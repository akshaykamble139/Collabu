import { Box, Breadcrumbs, Button, Divider, FormControl, IconButton, List, ListItem, ListItemText, MenuItem, Paper, Select, Typography } from "@mui/material";
import { Code, GitBranch, GitFork, Star, Trash, File, Folder, ListIcon } from "lucide-react";
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { showNotification } from "../../redux/notificationSlice";
import apiService from "../../services/apiService";
import { useConfirmationDialog } from "../../globalComponents/ConfirmationDialogContext";
import AddFileForm from "../../globalComponents/forms/AddFileForm";
import FileViewerPage from "../FileViewerPage";
import RepositoryNavigator from "./RepositoryNavigator";
import RepositoryRootDirectory from "./RepositoryRootDirectory";
import { updateFolderContents } from "../../redux/repositoryStructureSlice";

const RepositoryCode = ({propRepo}) => {
    
    const navigation = useSelector(state => state.navigation)
    
    const repo = useState(propRepo);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [branches, setBranches] = useState([]);
    const [files, setFiles] = useState([]);
    const location = useLocation();
    const { showDialog, hideDialog } = useConfirmationDialog();
    const repositoryStructure = useSelector((state) => state.repositoryStructure[navigation.repoUsername]?.[navigation.repoName]?.[navigation.repoBranchName]);

    useEffect(() => {
        const fetchRepo = async () => {
            try {
                    const branchesResponse = await apiService.fetchBranches(navigation.repoUsername, navigation.repoName);
                    setBranches(branchesResponse.data);
            
            } catch (err) {
                navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
            }
        };
        if (navigation.repoUsername && navigation.repoName) {
            fetchRepo();
        }
    }, [navigation.repoUsername, navigation.repoName]);

    useEffect(() => {
        const fetchFiles = async () => {
            try {
                if (navigation.repoUsername && navigation.repoName && navigation.repoBranchName && repositoryStructure 
                    && !location.pathname.includes("blob") && navigation.currentPath !== "/" && repositoryStructure.name === "/") {
                    const pathArr = navigation.currentPath.split("/");
                    let hasData = false;
                    let data = repositoryStructure.children;

                    console.log(pathArr, repositoryStructure.children)


                    for (let index = 1; index < pathArr.length; index++) {
                        const folderName = pathArr[index];
                        for (let j = 0; j < data.length; j++) {
                            const element = data[j];
                            if (element.name === folderName && element.type === "folder") {
                                data = element.children;
                                if (index === pathArr.length-1) {
                                    hasData = true;
                                }
                            }
                        }                       
                    }

                    if (hasData && data && data.length > 0) {
                        console.log(data)
                        setFiles(data);
                    }
                    else {
                        try {
                                const response = await apiService.fetchTreeStructureOfCurrentFolder(
                                    navigation.repoUsername,
                                    navigation.repoName,
                                    navigation.repoBranchName,
                                    !navigation.currentPath.endsWith("/") ? navigation.currentPath + "/" : navigation.currentPath
                                );
                        
                                if (response?.data?.children) {
                                    setFiles(response.data.children)
                                    dispatch(
                                        updateFolderContents({
                                        username: navigation.repoUsername,
                                        repoName: navigation.repoName,
                                        branchName: navigation.repoBranchName,
                                        path: !navigation.currentPath.endsWith("/") ? navigation.currentPath + "/" : navigation.currentPath,
                                        contents: response.data?.children,
                                        })
                                    );
                                }
                        } catch (error) {
                            console.error("Error fetching folder contents:", error);
                        }
                                                   
                    }
                }
            
            } catch (err) {
                navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
            }
        };
        if (navigation.repoUsername && navigation.repoName && navigation.repoBranchName) {
            fetchFiles();
        }
    }, [navigation.repoUsername, navigation.repoName, navigation.repoBranchName,repositoryStructure, navigation.currentPath, location.pathname]);

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
            'repositoryName': navigation.repoName,
            'branchName': navigation.repoBranchName,
            'commitMessage': commitMessage,
            'path': navigation.currentPath + filePath,
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
        if (newBranch !== navigation.repoBranchName) {
            navigate(`/${navigation.repoUsername}/${navigation.repoName}/tree/${newBranch}`);
        }
    }
        
    return (
        <>
        <div className="flex flex-col h-full">
            <RepositoryRootDirectory 
                propRepo={propRepo} 
                setCurrentBranch={setCurrentBranch} 
                openCreateFileDialog={openCreateFileDialog}
                branches={branches}
            />
            {(navigation.currentPath !== "/" || location.pathname.includes("blob")) && repo && branches.length > 0 && (
            <div className="repo-container flex-grow">
                <Box sx={{ flexGrow: 1 }}>
                    <Breadcrumbs>
                        <Link to={`/${navigation.repoUsername}/repositories`} 
                        style={{ textDecoration: 'none', color: 'inherit' }}>
                        {navigation.repoUsername}
                        </Link>
                        <Link to={`/${navigation.repoUsername}/${navigation.repoName}`} 
                        style={{ textDecoration: 'none', color: 'inherit' }}>
                            {navigation.repoName}
                        </Link>
                        {navigation.currentPath.split('/').map((path, index, arr) => {
                        if (!path) return null;
                        const fullPath = arr.slice(0, index + 1).join('/');
                        return (
                            <Link
                            key={path}
                            to={`/${navigation.repoUsername}/${navigation.repoName}/tree/${navigation.repoBranchName}/${fullPath}`}
                            style={{ textDecoration: 'none', color: 'inherit' }}
                            >
                            {path}
                            </Link>
                        );
                        })}
                    </Breadcrumbs>     
                </Box>
                <Box sx={{ display: 'flex', flexGrow: 1, width: '100%' }}>
                <RepositoryNavigator />

                {location.pathname.includes("tree") && navigation.currentPath !== "/" ? 
                <Paper sx={{ flexGrow: 1, p: 2, width: '100%' }}>
                    {/* <Divider sx={{mb:2}}/> */}
                    {files.length > 0 &&
                    <List>
                    {files.map((file) => (
                        <ListItem key={file.id}>
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

                </Paper> :
                location.pathname.includes("blob") && 
                <>
                    <FileViewerPage 
                        username={navigation.repoUsername}
                        repoName={navigation.repoName}
                        branchName={navigation.repoBranchName}
                    />
                </> }
                </Box>
            </div>)}
            </div>
        </>
    );
};

export default RepositoryCode;
