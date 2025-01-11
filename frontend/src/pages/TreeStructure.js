import React from 'react';
import { TreeView, TreeItem } from '@mui/lab';
import { Folder, File } from 'react-feather';
import { Link } from 'react-router-dom';

const TreeStructure = ({ files, username, repoName, branchName }) => {
  const renderTreeItems = (file) => {
    return (
      <TreeItem
        key={file.name}
        nodeId={file.name.toString()}
        label={
          <Link
            to={file.type === 'folder'
              ? `/${username}/${repoName}/tree/${branchName}${file.path}${file.name}`
              : `/${username}/${repoName}/blob/${branchName}${file.path}${file.name}`
            }
            style={{ textDecoration: 'none', color: 'inherit' }}
          >
            {file.type === 'folder' ? <Folder size={16} /> : <File size={16} />} {file.name}
          </Link>
        }
      >
        {file.children?.map((child) => renderTreeItems(child))}
      </TreeItem>
    );
  };

  return (
    <TreeView
      defaultCollapseIcon={<Folder size={16} />}
      defaultExpandIcon={<Folder size={16} />}
    >
      {files.map((file) => renderTreeItems(file))}
    </TreeView>
  );
};

export default TreeStructure;
