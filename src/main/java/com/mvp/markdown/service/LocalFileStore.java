package com.mvp.markdown.service;

import java.io.IOException;
import java.nio.file.Path;

public class LocalFileStore implements FileStore {

    @Override
    public void createFile(Path path) throws IOException {

    }

    @Override
    public String readFile(Path path) throws IOException {
        return "";
    }

    @Override
    public void writeFile(Path path, String content) throws IOException {

    }

    @Override
    public void deleteFile(Path path) throws IOException {

    }

    @Override
    public void move(Path source, Path target) throws IOException {

    }

    @Override
    public boolean exists(Path path) {
        return false;
    }

    @Override
    public void createDirectories(Path path) throws IOException {

    }
}
