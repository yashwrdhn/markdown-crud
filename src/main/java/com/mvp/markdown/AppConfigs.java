package com.mvp.markdown;

import com.mvp.markdown.service.StorageService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class AppConfigs {

    @Value("${vault.path}")
    public String vaultPath;

    private static final Logger log = LoggerFactory.getLogger(AppConfigs.class);

    public Path getResolvedVaultPath() {
        // This resolves the path relative to the directory where the application is running
        return java.nio.file.Paths.get(vaultPath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void validateVault() throws IOException {
        Path resolvedVaultPath = getResolvedVaultPath();
        if(!Files.exists(resolvedVaultPath)) {
            log.info("| Vault directory not found. Creating at: {}", resolvedVaultPath);
            Files.createDirectories(resolvedVaultPath);
        }
        else{
            log.info("| Vault directory found at {}", resolvedVaultPath);
        }
    }

}
