package com.mvp.markdown.service;

import java.util.List;

public record FileNode(String name, boolean isDirectory, List<FileNode> children) {}