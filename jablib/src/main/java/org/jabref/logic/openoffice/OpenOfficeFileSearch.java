package org.jabref.logic.openoffice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.jabref.logic.os.OS;
import org.jabref.logic.util.io.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenOfficeFileSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenOfficeFileSearch.class);

    /**
     * Detects existing installation of OpenOffice and LibreOffice.
     *
     * @return a list of detected installation paths
     */
    public static List<Path> detectInstallations() {
        if (OS.WINDOWS) {
            List<Path> programDirs = findWindowsOpenOfficeDirs();
            return programDirs.stream().filter(dir -> FileUtil.find(OpenOfficePreferences.WINDOWS_EXECUTABLE, dir).isPresent()).toList();
        } else if (OS.OS_X) {
            List<Path> programDirs = findOSXOpenOfficeDirs();
            return programDirs.stream().filter(dir -> FileUtil.find(OpenOfficePreferences.OSX_EXECUTABLE, dir).isPresent()).toList();
        } else if (OS.LINUX) {
            List<Path> programDirs = findLinuxOpenOfficeDirs();
            return programDirs.stream().filter(dir -> FileUtil.find(OpenOfficePreferences.LINUX_EXECUTABLE, dir).isPresent()).toList();
        } else {
            return List.of();
        }
    }

    private static List<Path> findOpenOfficeDirectories(List<Path> programDirectories) {
        BiPredicate<Path, BasicFileAttributes> filePredicate = (path, attr) ->
                attr.isDirectory() && (path.toString().toLowerCase(Locale.ROOT).contains("openoffice")
                        || path.toString().toLowerCase(Locale.ROOT).contains("libreoffice"));

        return programDirectories.stream()
                                 .flatMap(dirs -> {
                                     try {
                                         return Files.find(dirs, 1, filePredicate);
                                     } catch (IOException e) {
                                         LOGGER.error("Problem searching for openoffice/libreoffice install directory", e);
                                         return Stream.empty();
                                     }
                                 })
                                 .toList();
    }

    private static List<Path> findWindowsOpenOfficeDirs() {
        List<Path> sourceList = new ArrayList<>();

        // 64-bit program directory
        String progFiles = System.getenv("ProgramFiles");
        if (progFiles != null) {
            sourceList.add(Path.of(progFiles));
        }

        // 32-bit program directory
        progFiles = System.getenv("ProgramFiles(x86)");
        if (progFiles != null) {
            sourceList.add(Path.of(progFiles));
        }

        return findOpenOfficeDirectories(sourceList)
                .stream()
                // On Windows, the executable is nested in subdirectory "program"
                .map(dir -> dir.resolve("program"))
                .toList();
    }

    private static List<Path> findOSXOpenOfficeDirs() {
        List<Path> sourceList = List.of(Path.of("/Applications"));

        return findOpenOfficeDirectories(sourceList);
    }

    private static List<Path> findLinuxOpenOfficeDirs() {
        List<Path> sourceList = Arrays.asList(Path.of("/usr/lib"), Path.of("/usr/lib64"), Path.of("/opt"));

        return findOpenOfficeDirectories(sourceList);
    }
}
