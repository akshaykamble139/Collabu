import React, { useState } from 'react';
import { Folder, FileText } from 'react-feather';
import { Link } from 'react-router-dom';

const FolderOpen = ({ size }) => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    className="feather feather-folder-open"
  >
    <path d="M22 19a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2H12l-2-2H2a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h20z"></path>
    <polyline points="2 15 2 3 12 3 14 5 22 5 22 15"></polyline>
  </svg>
);

const TreeStructure = ({ files, username, repoName, branchName, handleFolderClick }) => {
  const [expandedNodes, setExpandedNodes] = useState([]);

  const toggleNode = (nodeId) => {
    setExpandedNodes((prev) =>
      prev.includes(nodeId)
        ? prev.filter((id) => id !== nodeId)
        : [...prev, nodeId]
    );
  };

  const renderTreeItems = (file) => {
    const isExpanded = expandedNodes.includes(file.id);

    return (
      <div key={file.id} style={{ marginLeft: '20px' }}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            cursor: file.type === 'folder' ? 'pointer' : 'default',
          }}
          onClick={() => {
            if (file.type === 'folder') {
              toggleNode(file.id);
              handleFolderClick(file.path);
              window.location.pathname = `/${username}/${repoName}/tree/${branchName}${file.path}${file.name}`;
            }
          }}
        >
          {file.type === 'folder' ? (
            isExpanded ? <FolderOpen size={16} /> : <Folder size={16} />
          ) : (
            <FileText size={16} />
          )}
          {file.type === 'file' ? (
            <Link
              to={`/${username}/${repoName}/blob/${branchName}${file.path}${file.name}`}
              style={{ textDecoration: 'none', color: 'inherit', marginLeft: '8px' }}
            >
              {file.name}
            </Link>
          ) : (
            <span style={{ marginLeft: '8px' }}>{file.name}</span>
          )}
        </div>
        {isExpanded && file.children && (
          <div style={{ marginTop: '4px' }}>
            {file.children.map((child) => renderTreeItems(child))}
          </div>
        )}
      </div>
    );
  };

  return (
    <div
      style={{
        maxWidth: '300px',
        overflowY: 'auto',
        padding: '8px',
        border: '1px solid #ddd',
        borderRadius: '4px',
        backgroundColor: '#f9f9f9',
      }}
    >
      {files.map((file) => renderTreeItems(file))}
    </div>
  );
};

export default TreeStructure;
