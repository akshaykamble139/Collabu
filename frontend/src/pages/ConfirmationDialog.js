import React, { useEffect, useState } from "react";
import { Dialog, DialogActions, DialogContent, DialogTitle, Button } from "@mui/material";
import { hideConfirmationDialog } from "../redux/confirmationDialogSlice";
import { useDispatch, useSelector } from "react-redux";
const ConfirmationDialog = () => {
  const { open, title, confirmText, message, onConfirm  } = useSelector((state) => state.confirmationDialog);
  const dispatch = useDispatch();
  const onClose= () => {
    setShowForm(false)
    dispatch(hideConfirmationDialog());
  };

  const [showForm, setShowForm] = useState(true);
  const onConfirmFunction= () => {
    if (onConfirm) {
        onConfirm();
    }
    else {
        dispatch(hideConfirmationDialog());
    }
  }

  useEffect(() => {
    setShowForm(open)
  },[open])
  const color = confirmText.toLowerCase().includes("delete") || confirmText.toLowerCase().includes("deactivate") ? "error" : "primary"
  return (
    <>
    {showForm &&
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>{message}</DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={onConfirmFunction} color={color} variant="contained">
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog> }
    </>
  );
};

export default ConfirmationDialog;
