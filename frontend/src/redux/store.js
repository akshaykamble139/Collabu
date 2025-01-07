import { configureStore } from "@reduxjs/toolkit";
import userReducer from "./userSlice";
import notificationReducer from "./notificationSlice";  // Import notification slice
import confirmationDialogReducer  from "./confirmationDialogSlice";

export const store = configureStore({
  reducer: {
    user: userReducer,
    notification: notificationReducer,
    confirmationDialog: confirmationDialogReducer,
  },
});
