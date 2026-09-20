package com.mvp.configs;

import com.mvp.markdown.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class MarkdownConfiguration {

    @Bean
    MetadataStore metadataStore(
            @Value("${alexandria.vault.path}") String vaultPath) {
        return new SidecarMetadataStore();
    }

    @Bean
    LocalFileStore fileStore() {
        return new LocalFileStore();
    }

    @Bean
    StorageService storageService(
            @Value("${alexandria.vault.path}") String vaultPath,
            FileStore fileStore,
            MetadataStore metadataStore) {
        return new StorageService(
                Path.of(vaultPath),
                fileStore(),
                metadataStore
        );
    }
}
