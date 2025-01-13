import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Box, Typography, CircularProgress, Paper, Button, Avatar } from '@mui/material';
import apiService from '../services/apiService';
import { useSelector } from 'react-redux';

const FileViewerPage = () => {
  const navigation = useSelector(state => state.navigation)

  const location = useLocation();
  const [filePath, setFilePath] = useState(window.location.pathname.split("/blob/" + navigation.repoBranchName + "/")[1]);
  const [fileData, setFileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const first = location.pathname.split("/blob/");
    const suffix = first[1].split("/");
    setFilePath(suffix.slice(1,suffix.length).join("/"));
  }, [location.pathname])

  useEffect(() => {
    const fetchFileContent = async () => {
      try {
        const response = await apiService.fetchFileContent(navigation.repoUsername, navigation.repoName, navigation.repoBranchName, filePath, {
          responseType: 'blob', // Treat the response as a Blob
        });

        if (response.data && Array.isArray(response.data) && (response.data.length > 1 || 
          (response.data.length == 1 && response.data[0].type === "folder"))) {
            window.location = window.location.pathname.replace("blob", "tree");
        }

        const contentType = response.headers['content-type'];
        const contentDisposition = response.headers['content-disposition'];
        const fileName = contentDisposition
          ? contentDisposition.split('filename=')[1]?.replace(/"/g, '')
          : filePath.split('/').pop();

        if (contentType.startsWith('application/json')) {
          // Handle small text files (JSON response)
          const textData = await response.data.text();
          const jsonData = JSON.parse(textData);
          setFileData({
            type: 'text',
            content: jsonData.content,
            fileName: jsonData.name,
            mimeType: jsonData.mimeType,
          });
        } else if (contentType.startsWith('image/')) {
          // Handle image files (Base64 encoded)
          const base64Data = await response.data.text();
          setFileData({
            type: 'image',
            base64Content: base64Data,
            fileName,
            mimeType: contentType,
          });
        } else {
          // Handle large or binary files (raw bytes)
          const blob = new Blob([response.data], { type: contentType });
          const downloadUrl = URL.createObjectURL(blob);
          setFileData({
            type: 'binary',
            fileName,
            mimeType: contentType,
            downloadUrl,
            size: response.data.size,
          });
        }
      } catch (err) {
        console.error('Error loading file:', err);
        navigate('/error', { state: { code: err?.response?.code ? err.response.code : 404 } });
      } finally {
        setLoading(false);
      }
    };

    if (navigation.repoUsername && navigation.repoName && navigation.repoBranchName && filePath) {
      fetchFileContent();
    }

    // Cleanup function to revoke object URLs
    return () => {
      if (fileData?.downloadUrl) {
        URL.revokeObjectURL(fileData.downloadUrl);
      }
    };
  }, [navigation.repoUsername, navigation.repoName, navigation.repoBranchName, filePath, location.pathname]);

  if (loading) return <CircularProgress />;
  if (error) return <Typography color="error">{error}</Typography>;

  // Render small text files
  if (fileData?.type === 'text') {
    return (
      <Box sx={{ py: 4 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>{fileData.fileName}</Typography>
        <Paper sx={{ p: 3, whiteSpace: 'pre-wrap', fontFamily: 'monospace' }}>
          {fileData.content}
        </Paper>
      </Box>
    );
  }

  // Render image files
  if (fileData?.type === 'image') {
    return (
      <Box sx={{ py: 4 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>{fileData.fileName}</Typography>
        <img
          src={`data:${fileData.mimeType};base64,${fileData.base64Content}`}
          alt={fileData.fileName}
          style={{ maxWidth: '100%' }}
        />
      </Box>
    );
  }

  // Render large or binary files
  if (fileData?.type === 'binary') {
    return (
      <Box sx={{ py: 4 }}>
        <Typography variant="h5" sx={{ mb: 2 }}>{fileData.fileName}</Typography>
        <Typography variant="body1">
          {fileData.mimeType.startsWith('text/') ? 'Large Text File' : 'Binary File'}
          {fileData.size && ` (${(fileData.size / 1024).toFixed(1)} KB)`}
        </Typography>
        <Button
          variant="contained"
          href={fileData.downloadUrl}
          download={fileData.fileName}
          sx={{ mt: 2 }}
        >
          Download {fileData.fileName}
        </Button>
      </Box>
    );
  }

  // Fallback for unsupported file types
  return (
    <Box sx={{ py: 4 }}>
      <Typography variant="h5" sx={{ mb: 2 }}>{filePath}</Typography>
      <Typography color="error">Unsupported file type.</Typography>
    </Box>
  );
};

export default FileViewerPage;