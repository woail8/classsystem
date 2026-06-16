package com.campuslearning.server.service;

import com.campuslearning.server.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;

/**
 * 服务端本地文件存储服务。
 */
@Service
public class FileStorageService {

    private final Path rootPath;

    public FileStorageService(@Value("${campus.file.upload-path}") String uploadPath) {
        this.rootPath = Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    public StoredFileInfo store(String category, MultipartFile file) {
        return store(category, "", file);
    }

    public StoredFileInfo store(String category, String relativeDirectory, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "unknown.dat" : file.getOriginalFilename());
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalName.substring(dotIndex);
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;

        try {
            Path targetDirectory = resolveDirectory(category, relativeDirectory);
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), targetDirectory.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(500, "文件保存失败");
        }

        String storedPath = relativeDirectory == null || relativeDirectory.trim().isEmpty()
                ? storedName
                : relativeDirectory.replace("\\", "/").replaceAll("/+$", "") + "/" + storedName;
        return new StoredFileInfo(storedPath, originalName);
    }

    public Resource loadAsResource(String category, String fileName) {
        Path filePath = resolvePath(category, fileName);
        FileSystemResource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new BusinessException(404, "文件不存在");
        }
        return resource;
    }

    public void delete(String category, String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }
        try {
            Files.deleteIfExists(resolvePath(category, fileName));
        } catch (IOException ex) {
            throw new BusinessException(500, "文件删除失败");
        }
    }

    public void deleteDirectory(String category, String relativeDirectory) {
        if (!StringUtils.hasText(relativeDirectory)) {
            return;
        }
        Path directory = resolveDirectory(category, relativeDirectory);
        if (!Files.exists(directory)) {
            return;
        }
        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    });
        } catch (IOException ex) {
            throw new BusinessException(500, "目录删除失败");
        } catch (IllegalStateException ex) {
            throw new BusinessException(500, "目录删除失败");
        }
    }

    private Path resolveDirectory(String category, String relativeDirectory) {
        Path categoryPath = rootPath.resolve(category).normalize();
        Path resolved = StringUtils.hasText(relativeDirectory)
                ? categoryPath.resolve(relativeDirectory).normalize()
                : categoryPath;
        if (!resolved.startsWith(categoryPath)) {
            throw new BusinessException(400, "非法文件目录");
        }
        return resolved;
    }

    private Path resolvePath(String category, String fileName) {
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(400, "文件名不能为空");
        }
        Path categoryPath = rootPath.resolve(category).normalize();
        Path filePath = categoryPath.resolve(fileName).normalize();
        if (!filePath.startsWith(categoryPath)) {
            throw new BusinessException(400, "非法文件路径");
        }
        return filePath;
    }

    /**
     * 服务端保存后的文件信息。
     */
    public static class StoredFileInfo {
        private final String storedName;
        private final String originalName;

        public StoredFileInfo(String storedName, String originalName) {
            this.storedName = storedName;
            this.originalName = originalName;
        }

        public String getStoredName() {
            return storedName;
        }

        public String getOriginalName() {
            return originalName;
        }
    }
}
