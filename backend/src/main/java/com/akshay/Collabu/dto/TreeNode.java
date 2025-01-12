package com.akshay.Collabu.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class TreeNode implements Serializable{
    private static final long serialVersionUID = -683598870493856303L;
    
    private String id;
	private String name;
    private String type; // "file" or "folder"
    private String path;
    private List<TreeNode> children;
    
    public TreeNode(String name, String type, String path) {
    	this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.path = path;
        this.children = new ArrayList<>();
    }
    
    public void addChild(TreeNode child) {
        this.children.add(child);
    }
}

