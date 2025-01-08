import { Box, FormControlLabel, Switch, TextField } from "@mui/material";
import React, { useState } from "react";

const CreateRepositoryForm = ({newRepo, setNewRepo}) => {
    const [localNewRepo, setLocalNewRepo] = useState(newRepo);
    
    const handleRepoNameUpdate = (e) => {
        const name = e.target.value;
        setLocalNewRepo({...localNewRepo, name});
        setNewRepo({...localNewRepo, name}); 
    };

    const handleRepoDescriptionUpdate = (e) => {
        const description = e.target.value;
        setLocalNewRepo({...localNewRepo, description});
        setNewRepo({...localNewRepo, description}); 
    };

    const handleRepoVisibilityUpdate = (e) => {
        const publicRepositoryOrNot = e.target.checked;
        setLocalNewRepo({...localNewRepo, publicRepositoryOrNot});
        setNewRepo({...localNewRepo, publicRepositoryOrNot}); 
    };
    return (
        <Box sx={{ mt: 2 }}>
            <TextField
            label="Repository name"
            fullWidth
            value={localNewRepo.name}
            onChange={handleRepoNameUpdate}
            sx={{ mb: 2 }}
            />
            <TextField
            label="Description (optional)"
            fullWidth
            multiline
            rows={3}
            value={localNewRepo.description}
            onChange={handleRepoDescriptionUpdate}
            sx={{ mb: 2 }}
            />
            <FormControlLabel
            control={
                <Switch
                checked={localNewRepo.publicRepositoryOrNot}
                onChange={handleRepoVisibilityUpdate}
                />
            }
            label={localNewRepo.publicRepositoryOrNot ? 'Public' : 'Private'}
            />
        </Box>
    )
};

export default CreateRepositoryForm;