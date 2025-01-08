import React, { useState } from 'react';
import { Box, TextField } from '@mui/material';
const AddFileForm = ({ onFileNameChange, onCommitMessageChange, onFileChange }) => {
    const [localFileName, setLocalFileName] = useState("");
  const [localCommitMessage, setLocalCommitMessage] = useState("Create ");
  const [localFile, setLocalFile] = useState(null);

  const handleFileNameUpdate = (e) => {
    const name = e.target.value;
    setLocalFileName(name);
    setLocalCommitMessage(`Create ${name}`);
    onFileNameChange(name); // Update parent state
  };

  const handleCommitMessageUpdate = (e) => {
    const message = e.target.value;
    setLocalCommitMessage(message);
    onCommitMessageChange(message); // Update parent state
  };

  const handleFileUpdate = (e) => {
    const file = e.target.files[0];
    setLocalFile(file);
    onFileChange(file); // Update parent state
  };

  return (
    <Box sx={{ mt: 2 }}>
      <TextField
        label="File name"
        fullWidth
        value={localFileName}
        onChange={handleFileNameUpdate}
        sx={{ mb: 2 }}
      />
      <TextField
        label="Commit Message"
        fullWidth
        multiline
        rows={3}
        value={localCommitMessage}
        onChange={handleCommitMessageUpdate}
        sx={{ mb: 2 }}
      />
      <input type="file" onChange={handleFileUpdate} />
    </Box>
    );
  };

  export default AddFileForm;
  