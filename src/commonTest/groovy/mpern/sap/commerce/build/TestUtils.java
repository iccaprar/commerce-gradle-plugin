package mpern.sap.commerce.build;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static void generateDummyPlatform(Path destination, String version) throws IOException, URISyntaxException {
        generateDummyPlatformFromTemplate(destination, version, "/dummy-platform");
    }

    public static void generateDummyPlatformNewModel(Path destination, String version) throws IOException, URISyntaxException {
        generateDummyPlatformFromTemplate(destination, version, "/dummy-platform-new-model");
    }

    private static void generateDummyPlatformFromTemplate(Path destination, String version, String template)
        throws IOException, URISyntaxException {
        Path targetZip = destination.resolve(String.format("hybris-commerce-suite-%s.zip", version));
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem zipfs = FileSystems.newFileSystem(URI.create("jar:" + targetZip.toUri().toString()), env, null)) {
            Path sourceDir = Paths.get(TestUtils.class.getResource(template).toURI());
            processSourceDir(zipfs, sourceDir);
            String build = String.format("version=%s\n", version);
            Path buildNumber = zipfs.getPath("hybris", "bin", "platform", "build.number");
            Files.write(buildNumber, build.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    public static void generateDummyExtensionPack(Path destination, String version) throws Exception {
        Path targetZip = destination.resolve(String.format("hybris-cloud-extension-pack-%s.zip", version));
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem zipfs = FileSystems.newFileSystem(URI.create("jar:" + targetZip.toUri().toString()), env, null)) {
            Path sourceDir = Paths.get(TestUtils.class.getResource("/dummy-cloud-extension-pack").toURI());
            processSourceDir(zipfs, sourceDir);
        }
    }

    public static void generateDummyIntegrationPack(Path destination, String version) throws Exception {
        Path targetZip = destination.resolve(String.format("hybris-commerce-integrations-%s.zip", version));
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem zipfs = FileSystems.newFileSystem(URI.create("jar:" + targetZip.toUri().toString()), env, null)) {
            Path sourceDir = Paths.get(TestUtils.class.getResource("/dummy-integration-pack").toURI());
            processSourceDir(zipfs, sourceDir);
        }
    }

    public static Path ensureParents(Path p) throws Exception {
        Files.createDirectories(p.getParent());
        return p;
    }

    private static void processSourceDir(FileSystem zipfs, Path sourceDir) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relative = sourceDir.relativize(file);
                if (relative.getParent() != null) {
                    Path path = zipfs.getPath(relative.getParent().toString());
                    Files.createDirectories(path);
                }
                Path target = zipfs.getPath(relative.toString());
                Files.copy(file, target);
                return super.visitFile(file, attrs);
            }
        });
    }
}
