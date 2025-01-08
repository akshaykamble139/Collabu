import { Box, TextField } from "@mui/material";
import React, { useState } from "react";

const ForkRepositoryForm = ({newRepoName, setNewRepoName}) => {
    const [localNewRepoName, setLocalNewRepoName] = useState(newRepoName);
    
    const handleRepoNameUpdate = (e) => {
        const name = e.target.value;
        setLocalNewRepoName(name);
        setNewRepoName(name); 
    };

    return (
        <Box sx={{ mt: 2 }}>
            <TextField
                label="Repository Name"
                fullWidth
                value={localNewRepoName}
                onChange={handleRepoNameUpdate}
                margin="dense"
            />
        </Box>
    )
};

export default ForkRepositoryForm;