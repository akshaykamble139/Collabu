import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Box, Container, Typography, Paper, Breadcrumbs } from '@mui/material';
import instance from '../services/axiosConfig'; 
import { showNotification } from '../redux/notificationSlice';
import { useDispatch, useSelector } from 'react-redux';

const BranchPage = () => {
    const { username, repoName, branchName } = useParams();
    const [files, setFiles] = useState([]);
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const userData = useSelector(state => state.user);

    useEffect(() => {
        const fetchBranchFiles = async () => {
            try {
                const response = await instance.get(`/files/${username}/${repoName}/${branchName}`);
                setFiles(response.data);
            } catch (err) {
                navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
            }
        };

        if (username && repoName && branchName) {
            fetchBranchFiles();
        }
    }, [username, repoName, branchName]);

    return (
        <Container maxWidth="lg" sx={{ py: 4 }}>
            <Breadcrumbs aria-label="breadcrumb" sx={{ mb: 4 }}>
                <Link to={`/${username}/repositories`} underline="hover" color="inherit">
                    {username}
                </Link>
                <Link to={`/${username}/${repoName}`}>{repoName}</Link>
                <Typography color="text.primary">{branchName}</Typography>
            </Breadcrumbs>

            <Typography variant="h4" gutterBottom>
                {branchName} - Files
            </Typography>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {files.map((file) => (
                    <Paper key={file.path} sx={{ p: 2 }}>
                        <Link to={`/${username}/${repoName}/blob/${branchName}/${file.path}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                            <Typography variant="body1">
                                {file.path}
                            </Typography>
                        </Link>
                    </Paper>
                ))}
            </Box>
        </Container>
    );
};

export default BranchPage;
