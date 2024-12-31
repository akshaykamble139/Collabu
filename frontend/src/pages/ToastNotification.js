import React, { useEffect, useState } from "react";
import { Box, Typography, Slide } from "@mui/material";

const ToastNotification = ({ message, type = "success", onClose }) => {
  const [open, setOpen] = useState(true);

  useEffect(() => {
    // Automatically close the toast after 5 seconds
    const timer = setTimeout(() => {
      setOpen(false);
      onClose && onClose();
    }, 5000);

    return () => clearTimeout(timer); // Clear timeout on unmount
  }, [onClose]);

  return (
    <Slide direction="up" in={open} mountOnEnter unmountOnExit>
      <Box
        sx={{
          position: "fixed",
          bottom: 20,
          right: 20,
          minWidth: 250,
          padding: 2,
          borderRadius: 2,
          boxShadow: 3,
          backgroundColor: type === "success" ? "#4caf50" : "#f44336",
          color: "white",
          textAlign: "center",
          zIndex: 1300,
        }}
      >
        <Typography variant="body1">{message}</Typography>
      </Box>
    </Slide>
  );
};

export default ToastNotification;
