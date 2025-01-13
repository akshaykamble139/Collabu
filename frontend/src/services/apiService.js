import instance from "./axiosConfig";

const apiService = {
    login: (form) => 
        instance.post("/api/auth/login", form),
    register:(form) =>
        instance.post("/api/auth/register", form),
    fetchProfileDetails: (username) => 
        instance.get(`/users/profile/${username}`),

    changePassword:(password) =>
        instance.post("/users/password", { password }),
    updateProfileDetails: (bio,location,website) =>
        instance.put("/users/profile/update", {bio, location, website}),

    deactivateAccount: () =>
        instance.post("/users/deactivate"),
    deleteAccount: () =>
        instance.delete("/users"),
    deleteRepository: (repoId) => 
        instance.delete(`/repositories/${repoId}`),
    deleteFile: (fileId) => 
        instance.delete(`/files/${fileId}`),
    deleteBranch: (branchId) =>
        instance.delete(`/branches/${branchId}`),

    reactivateAccount: (form) =>
        instance.post("/api/auth/reactivate", form),

    fetchAllRepositories: (username) => 
        instance.get(`/repositories/user/${username}`),
    fetchRepositoryData: (username, repoName) => 
        instance.get(`/repositories/${username}/${repoName}`),
    fetchBranches: (username, repoName) => 
        instance.get(`/branches/${username}/${repoName}`),
    fetchFilesFromBranch: (username, repoName, branchName, filePath) =>
        instance.get(`/files/${username}/${repoName}/${branchName}${filePath}`),
    fetchStarStatus: (ownerUsername, repositoryName) => 
        instance.post('/stars/status', {ownerUsername, repositoryName}),
    fetchFileContent: (username, repoName, branchName, filePath) =>
        instance.get(`/files/${username}/${repoName}/${branchName}/${filePath}`, {responseType: 'blob'}),
    fetchTreeStructureOfFiles: (username, repoName, branchName, folderPath) =>
        instance.get(`/files/${username}/${repoName}/${branchName}/tree${folderPath}`),
    fetchTreeStructureOfCurrentFolder: (username, repoName, branchName, folderPath) =>
        instance.get(`/files/${username}/${repoName}/${branchName}/tree${folderPath}?onlyCurrentFolder=true`),

    
    toggleStar: (ownerUsername, repositoryName) => 
        instance.post('/stars/toggle', {ownerUsername, repositoryName}),
    forkRepository: (repositoryId, name) => 
        instance.post('/repositories/fork', { repositoryId, name}),

    createBranch: (name, parentBranchName, repositoryName) =>
        instance.post(`/branches`, { name, parentBranchName, repositoryName}),
    createRepository: (name,description,ownerUsername,publicRepositoryOrNot) =>
        instance.post('/repositories', {name, description, ownerUsername, publicRepositoryOrNot}),
    uploadFile: (formData) =>
        instance.post("/files", formData, {headers: { "Content-Type": "multipart/form-data" }}),
    updateProfilePicture:(formData) => 
        instance.post("/users/upload-profile-picture", formData, {headers: { "Content-Type": "multipart/form-data" }}),

        
};

export default apiService;
  