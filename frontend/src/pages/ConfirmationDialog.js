import React from "react";
import { Dialog, DialogActions, DialogContent, DialogTitle, Button } from "@mui/material";

const ConfirmationDialog = ({ 
  open, 
  onClose, 
  onConfirm, 
  title = "Confirm Action", 
  children, 
  confirmText = "Confirm" 
}) => {

  const color = confirmText.toLowerCase().includes("delete") || confirmText.toLowerCase().includes("deactivate") ? "error" : "primary"
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>{children}</DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={onConfirm} color={color} variant="contained">
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmationDialog;
