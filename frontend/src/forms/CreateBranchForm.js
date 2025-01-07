import React, { useState } from 'react';
import { Box, FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material';
const CreateBranchForm = ({ branches, newBranchName, sourceBranchName, onBranchNameChange, onSourceBranchChange}) => {

  const [localNewBranchName, setLocalNewBranchName] = useState(newBranchName);
  const [localSourceBranchName, setLocalSourceBranchName] = useState(sourceBranchName);
  const handleBranchNameUpdate = (e) => {
    const name = e.target.value;
    setLocalNewBranchName(name);
    onBranchNameChange(name);
  };

  const handleSourceBranchUpdate = (e) => {
    const sourceBranch = e.target.value;
    setLocalSourceBranchName(sourceBranch);
    onSourceBranchChange(sourceBranch);
  };

  return (
    <Box sx={{ mt: 2 }}>
    <TextField
      label="Branch name"
      fullWidth
      value={localNewBranchName}
      onChange={handleBranchNameUpdate}
      sx={{ mb: 2 }}
    />
    <FormControl sx={{ minWidth: 120 }}>
      <InputLabel>Source Branch</InputLabel> 
      <Select
        value={localSourceBranchName}
        onChange={handleSourceBranchUpdate}
        label="Source Branch"
      >
        {branches.map((branch) => (
          <MenuItem key={branch.id} value={branch.name}>{branch.name}</MenuItem>
        ))}
      </Select>
    </FormControl>
  </Box>
    );
  };

  export default CreateBranchForm;
  