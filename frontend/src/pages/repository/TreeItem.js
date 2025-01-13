import React from 'react';
import { Folder, FileText, ChevronRight, ChevronDown } from 'lucide-react';
import { Link } from 'react-router-dom';
import { iconColor } from '../../utility/FileIconColors';

const TreeItem = ({ file, depth, isExpanded, onToggle, username, repoName, branchName }) => {
    const getFileIcon = (fileName) => {
        const extension = fileName.split('.').pop().toLowerCase();

        return (
            <FileText
                size={16}
                className={`flex-shrink-0 mr-3 ${iconColor[extension] || 'text-gray-600'}`}
            />
        );
    };

    return (
        <div
            className="flex items-center w-full hover:bg-gray-50 cursor-pointer py-3 px-4 transition-colors duration-200"
            style={{ paddingLeft: `${depth * 20 + 16}px` }}
        >
            {file.type === 'folder' && (
                <div className="w-4 h-4 mr-3 flex items-center justify-center transition-transform duration-200" onClick={onToggle}>
                    {isExpanded ? (
                        <ChevronDown className="w-4 h-4 text-gray-500 transform rotate-180" />
                    ) : (
                        <ChevronRight className="w-4 h-4 text-gray-500" />
                    )}
                </div>
            )}

            <div className="mr-3 flex items-center">
                {file.type === 'folder' ? (
                    <Folder className="w-5 h-5 text-blue-400" />
                ) : (
                    getFileIcon(file.name)
                )}
            </div>

            {file.type !== 'folder' ? (
                <Link
                    to={`/${username}/${repoName}/blob/${branchName}${file.path}${file.name}`}
                    className="text-sm font-medium text-gray-700 hover:text-blue-600 hover:underline flex-grow truncate"
                    onClick={(e) => e.stopPropagation()}
                >
                    {file.name}
                </Link>
            ) : (
                <Link
                    to={`/${username}/${repoName}/tree/${branchName}${file.path}${file.name}`}
                    className="text-sm font-semibold text-gray-800 flex-grow truncate"
                    onClick={(e) => e.stopPropagation()}
                >
                    {file.name}
                </Link>
            )}

            <div className="flex-shrink-0 ml-2 text-xs text-gray-500">
                {file.size && (
                    <span>{(file.size / 1024).toFixed(1)} KB</span>
                )}
            </div>
        </div>
    );
};

export default TreeItem;