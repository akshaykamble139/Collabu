import { createSlice } from "@reduxjs/toolkit";

const confirmationDialogSlice = createSlice({
  name: "confirmationDialog",
  initialState: {
    open: false,
    title: "Confirm Action",
    message: "",
    confirmText: "Confirm",
    onConfirm: null, // Function to execute on confirmation
  },
  reducers: {
    showConfirmationDialog: (state, action) => {
      const { title, message, confirmText, onConfirm } = action.payload;
      state.open = true;
      state.title = title || "";
      state.message = message || "";
      state.confirmText = confirmText || "Confirm";
      state.onConfirm = onConfirm || null;
    },
    hideConfirmationDialog: (state) => {
      state.open = false;
      state.title = "";
      state.message = "";
      state.confirmText = "Confirm";
      state.onConfirm = null;
    },
  },
});

export const { showConfirmationDialog, hideConfirmationDialog } = confirmationDialogSlice.actions;
export default confirmationDialogSlice.reducer;
