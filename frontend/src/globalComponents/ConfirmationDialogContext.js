import React, { createContext, useContext, useState } from "react";

const ConfirmationDialogContext = createContext();

export const useConfirmationDialog = () => useContext(ConfirmationDialogContext);

export const ConfirmationDialogProvider = ({ children }) => {
  const [dialogConfig, setDialogConfig] = useState({
    open: false,
    title: "",
    confirmText: "Confirm",
    onConfirm: null,
    component: null, // For dynamic forms or messages
  });

  const showDialog = (config) => {
    setDialogConfig({ ...dialogConfig, ...config, open: true });
  };

  const hideDialog = () => {
    setDialogConfig((prev) => ({ ...prev, open: false }));
  };

  return (
    <ConfirmationDialogContext.Provider value={{ dialogConfig, showDialog, hideDialog }}>
      {children}
    </ConfirmationDialogContext.Provider>
  );
};
