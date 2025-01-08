import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Box, Typography, CircularProgress, Paper, Button, Avatar } from '@mui/material';
import apiService from '../services/apiService';

const FileViewerPage = () => {
  const { username, repoName, branchName = 'main', filePath } = useParams();
  const [fileData, setFileData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchFileContent = async () => {
      try {
        const response = await apiService.fetchFileContent(username, repoName, branchName, filePath, {
          responseType: 'blob', // Treat the response as a Blob
        });

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
        setError('Failed to load file content.');
      } finally {
        setLoading(false);
      }
    };

    fetchFileContent();

    // Cleanup function to revoke object URLs
    return () => {
      if (fileData?.downloadUrl) {
        URL.revokeObjectURL(fileData.downloadUrl);
      }
    };
  }, [username, repoName, branchName, filePath]);

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