import React, { useState, useEffect } from 'react';
import TreeItem from './TreeItem';
import { useNavigate } from 'react-router-dom';

const TreeStructure = ({ files, username, repoName, branchName, handleFolderClick, filePath }) => {
  const [expandedNodes, setExpandedNodes] = useState([]);
  const navigate = useNavigate();

  // Helper function to find all folder IDs along the path
  const findExpandedNodes = (files, path, parentPath = '') => {
    let expanded = [];
    for (const file of files) {
      const fullPath = `${parentPath}${file.name}/`;
      if (file.type === 'folder') {
        expanded.push(file.id);
        if (path.startsWith(fullPath)) {
          expanded = expanded.concat(findExpandedNodes(file.children || [], path, fullPath));
        }
      }
    }
    return expanded;
  };

  useEffect(() => {
    // Initialize expanded nodes based on the current path
    if (filePath) {
      const nodesToExpand = findExpandedNodes(files, filePath);
      setExpandedNodes(nodesToExpand);
    }
  }, [files, filePath]);

  const toggleNode = (nodeId, path, type) => {
    if (type === 'folder') {
      setExpandedNodes((prev) =>
        prev.includes(nodeId)
          ? prev.filter((id) => id !== nodeId)
          : [...prev, nodeId]
      );
      handleFolderClick(path);
    }
    else {
      navigate(`/${username}/${repoName}/blob/${branchName}${path}`);
    }
  };

  const renderTreeItems = (file, depth = 0) => {
    const isExpanded = expandedNodes.includes(file.id);

    return (
      <div key={file.id} className="w-full">
        <TreeItem
          file={file}
          depth={depth}
          isExpanded={isExpanded}
          onToggle={() => toggleNode(file.id, file.path + file.name, file.type)}
          username={username}
          repoName={repoName}
          branchName={branchName}
        />
        
        {isExpanded && file.children && (
          <div className="w-full">
            {file.children.map((child) => renderTreeItems(child, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="w-full max-w-md bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
      <div className="border-b border-gray-200 bg-gray-50 px-4 py-3">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-gray-700">Files</h3>
        </div>
      </div>
      
      <div className="divide-y divide-gray-100">
        {files.map((file) => renderTreeItems(file))}
      </div>
    </div>
  );
};

export default TreeStructure;
