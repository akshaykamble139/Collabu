import React from "react";
import { Dialog, DialogActions, DialogContent, DialogTitle, Button } from "@mui/material";
import { useConfirmationDialog } from "./ConfirmationDialogContext";
const ConfirmationDialog = () => {
  const { dialogConfig, hideDialog } = useConfirmationDialog();
  const { open, title, confirmText, component: Component, onConfirm } = dialogConfig;

  const onClose= () => {
    hideDialog()
  };

  const onConfirmFunction= () => {
    if (onConfirm) {
        onConfirm();
    }
    else {
        hideDialog();
    }
  }

  const color = confirmText.toLowerCase().includes("delete") || confirmText.toLowerCase().includes("deactivate") ? "error" : "primary"
  return (
    <>
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>{Component ? <Component /> : ""}</DialogContent>
      <DialogActions>
        <Button onClick={hideDialog}>Cancel</Button>
        <Button onClick={onConfirmFunction} color={color} variant="contained">
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog> 
    </>
  );
};

export default ConfirmationDialog;
