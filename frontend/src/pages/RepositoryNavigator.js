import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { setRepositoryStructure, updateFolderContents } from "../redux/repositoryStructureSlice";
import apiService from "../services/apiService";
import TreeStructure from "./TreeStructure";
import { CircularProgress } from "@mui/material";

const RepositoryNavigator = ({ username, repoName, branchName, filePath }) => {
  const dispatch = useDispatch();
  const [loading, setLoading] = useState(true);

  // Access repository structure from Redux
  const repositoryStructure = useSelector(
    (state) =>
      state.repositoryStructure[username]?.[repoName]?.[branchName]
  );

  // Fetch root folder structure on load
  useEffect(() => {
    const fetchRootStructure = async () => {
      try {
        const response = await apiService.fetchTreeStructureOfFiles(
          username,
          repoName,
          branchName,
          filePath
        );
        dispatch(
          setRepositoryStructure({
            username,
            repoName,
            branchName,
            structure: response.data,
          })
        );
        setLoading(false);
      } catch (error) {
        console.error("Error fetching root structure:", error);
      }
    };

    if (!repositoryStructure) {
      fetchRootStructure();
    }
  }, [username, repoName, branchName, repositoryStructure, dispatch]);

  // Handle folder navigation
  const handleFolderClick = async (folderPath) => {
    const folder = repositoryStructure?.children.find(
      (child) => child.path === folderPath
    );

    // Check if the folder is already loaded
    if (!folder?.children) {
      try {
        const response = await apiService.fetchFolderContents(
          username,
          repoName,
          branchName,
          folderPath
        );
        dispatch(
          updateFolderContents({
            username,
            repoName,
            branchName,
            path: folderPath,
            contents: response.data,
          })
        );
      } catch (error) {
        console.error("Error fetching folder contents:", error);
      }
    }
  };

  // Render repository structure
  const renderStructure = (structure) => {
    if (!structure?.children) return null;

    return (
        <TreeStructure 
        files={structure.children} 
        username={username} 
        repoName={repoName} 
        branchName={branchName} 
        handleFolderClick={handleFolderClick}
        />

    //   <ul>
    //     {structure.children.map((item) => (
    //       <li key={item.path}>
    //         {item.type === "folder" ? (
    //           <span onClick={() => handleFolderClick(item.path)}>{item.name}</span>
    //         ) : (
    //           <span>{item.name}</span>
    //         )}
    //       </li>
    //     ))}
    //   </ul>
    );
  };

  return (
    <div>
      {repositoryStructure ? (
        renderStructure(repositoryStructure)
      ) : (
        (loading) && <CircularProgress />
      )}
    </div>
  );
};

export default RepositoryNavigator;
