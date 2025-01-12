import { createSlice } from "@reduxjs/toolkit";

const repositoryStructureSlice = createSlice({
  name: "repositoryStructure",
  initialState: {}, // Structure: { username: { repoName: { branchName: structure } } }
  reducers: {
    setRepositoryStructure: (state, action) => {
      const { username, repoName, branchName, structure } = action.payload;
      if (!state[username]) state[username] = {};
      if (!state[username][repoName]) state[username][repoName] = {};
      state[username][repoName][branchName] = structure;
    },
    updateFolderContents: (state, action) => {
      const { username, repoName, branchName, path, contents } = action.payload;
      const branchStructure = state[username]?.[repoName]?.[branchName];
      if (branchStructure) {
        let currentFolder = branchStructure;
        if (path !== "/") {
          const folders = path.split("/");
          folders.forEach((folder) => {
            currentFolder = currentFolder.children.find((child) => child.name === folder);
          });
        }
        currentFolder.children = contents;
      }
    },
  },
});

export const { setRepositoryStructure, updateFolderContents } = repositoryStructureSlice.actions;
export default repositoryStructureSlice.reducer;
