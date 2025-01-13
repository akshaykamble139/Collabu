import { createSlice } from "@reduxjs/toolkit";

const initialState = {
  repoUsername: "",
  repoName: "",
  repoBranchName: "main",
  currentPath: "/",
  fileName: "",
  pageName: "",
};

const navigationSlice = createSlice({
  name: "navigation",
  initialState,
  reducers: {
    setNavigationDetails: (state, action) => {
      const { repoUsername, repoName, repoBranchName, currentPath, fileName, pageName } = action.payload;
      state.repoUsername = repoUsername || state.repoUsername;
      state.repoName = repoName || state.repoName;
      state.repoBranchName = repoBranchName || state.repoBranchName;
      state.currentPath = currentPath || state.currentPath;
      state.fileName = fileName || state.fileName;
      state.pageName = pageName || state.pageName;
    },
    resetNavigationDetails: () => initialState,
  },
});

export const { setNavigationDetails, resetNavigationDetails } = navigationSlice.actions;
export default navigationSlice.reducer;
