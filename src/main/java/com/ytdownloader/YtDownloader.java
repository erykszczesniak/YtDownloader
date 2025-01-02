package com.ytdownloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YtDownloader {

    private static final Logger logger = Logger.getLogger(YtDownloader.class.getName());

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // URL of the video to download
        String videoUrl = "";

        // Ask the user for the desired resolution
        System.out.println("Enter the desired resolution (e.g., 720p, 1080p): ");
        String resolution = scanner.nextLine();

        // Map resolution to yt-dlp format
        String format = getYtDlpFormat(resolution);

        // Set the download directory to the Desktop
        String desktopPath = System.getProperty("user.home") + "/Desktop";

        // Verify available formats and download the video
        if (!verifyFormats(videoUrl)) {
            logger.warning("Failed to fetch available formats. Defaulting to best MP4 quality.");
            format = "\"bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]\"";
        }

        executeDownload(videoUrl, format, desktopPath);
    }

    private static String getYtDlpFormat(String resolution) {
        return switch (resolution) {
            case "720p" -> "\"bestvideo[ext=mp4][height<=720]+bestaudio[ext=m4a]/best[ext=mp4][height<=720]\"";
            case "1080p" -> "\"bestvideo[ext=mp4][height<=1080]+bestaudio[ext=m4a]/best[ext=mp4][height<=1080]\"";
            case "480p" -> "\"bestvideo[ext=mp4][height<=480]+bestaudio[ext=m4a]/best[ext=mp4][height<=480]\"";
            default -> {
                System.out.println("Resolution not recognized, downloading best available quality in MP4.");
                yield "\"bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]\"";
            }
        };
    }

    private static boolean verifyFormats(String videoUrl) {
        String formatCheckCommand = String.format("yt-dlp -F %s", videoUrl);

        try {
            ProcessBuilder formatChecker = new ProcessBuilder("bash", "-c", formatCheckCommand);
            formatChecker.redirectErrorStream(true);

            Process process = formatChecker.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            System.out.println("Available formats:");
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking formats.", e);
            return false;
        }
    }

    private static void executeDownload(String videoUrl, String format, String downloadDirectory) {
        String command = String.format("yt-dlp --merge-output-format mp4 -o '%s/%%(title)s.%%(ext)s' -f %s %s",
                downloadDirectory, format, videoUrl);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Download completed successfully. Video saved to: " + downloadDirectory);
            } else {
                logger.severe("An error occurred during the download.");
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred while executing the download command.", e);
        }
    }
}
