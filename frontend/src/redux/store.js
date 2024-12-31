import { configureStore } from "@reduxjs/toolkit";
import userReducer from "./userSlice";
import notificationReducer from "./notificationSlice";  // Import notification slice

export const store = configureStore({
  reducer: {
    user: userReducer,
    notification: notificationReducer,  // Add to store
  },
});
