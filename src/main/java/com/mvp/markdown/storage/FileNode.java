package com.mvp.markdown.storage;

import java.util.List;

public record FileNode(String name, boolean isDirectory, List<FileNode> children) {}