import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { setRepositoryStructure, updateFolderContents } from "../../redux/repositoryStructureSlice";
import apiService from "../../services/apiService";
import TreeStructure from "./TreeStructure";
import { CircularProgress } from "@mui/material";

const RepositoryNavigator = () => {
  const dispatch = useDispatch();
  const [loading, setLoading] = useState(true);
  
  const navigation = useSelector(state => state.navigation)

  // Access repository structure from Redux
  const repositoryStructure = useSelector(
    (state) =>
      state.repositoryStructure[navigation.repoUsername]?.[navigation.repoName]?.[navigation.repoBranchName]
  );

  // Fetch root folder structure on load
  useEffect(() => {
    const fetchRootStructure = async () => {
      try {
        const response = await apiService.fetchTreeStructureOfFiles(
          navigation.repoUsername,
          navigation.repoName,
          navigation.repoBranchName,
          navigation.currentPath
        );
        dispatch(
          setRepositoryStructure({
            username: navigation.repoUsername,
            repoName: navigation.repoName,
            branchName: navigation.repoBranchName,
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
  }, [navigation.repoUsername, navigation.repoName, navigation.repoBranchName, repositoryStructure, dispatch]);

  // Handle folder navigation
  const handleFolderClick = async (folderPath) => {
    let folder; 

      if (repositoryStructure) {
        let currentFolder = repositoryStructure;
        if (folderPath !== "/") {
          const folders = folderPath.split("/").slice(1);
          folders.forEach((folder) => {
            currentFolder = currentFolder.children.find((child) => child.name === folder);
          });
        }
        folder = currentFolder;
      }

    // Check if the folder is already loaded
    if (folder?.children && folder.children.length == 0) {
      try {
        const response = await apiService.fetchTreeStructureOfCurrentFolder(
          navigation.repoUsername,
          navigation.repoName,
          navigation.repoBranchName,
          folderPath
        );

        if (response?.data?.children) {
          dispatch(
            updateFolderContents({
              username: navigation.repoUsername,
              repoName: navigation.repoName,
              branchName: navigation.repoBranchName,
              path: folderPath,
              contents: response.data?.children,
            })
          
          );
        }
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
        username={navigation.repoUsername} 
        repoName={navigation.repoName} 
        branchName={navigation.repoBranchName} 
        handleFolderClick={handleFolderClick}
        filePath={navigation.currentPath}
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
