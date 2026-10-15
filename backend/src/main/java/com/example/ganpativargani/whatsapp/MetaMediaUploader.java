package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

/**
 * Uploads a public image URL to Meta's Resumable Upload API and returns a
 * {@code header_handle} suitable for WhatsApp template IMAGE headers.
 */
@Slf4j
@Component
public class MetaMediaUploader {

    private final WhatsAppProperties properties;
    private final RestClient restClient;

    public MetaMediaUploader(WhatsAppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + properties.secrets().accessToken())
                .build();
    }

    public String uploadImageFromUrl(String imageUrl) {
        requireAppId();
        validateHttpsImageUrl(imageUrl);

        byte[] bytes;
        String contentType;
        try {
            var response = RestClient.create()
                    .get()
                    .uri(URI.create(imageUrl.trim()))
                    .retrieve()
                    .toEntity(byte[].class);
            bytes = response.getBody();
            MediaType mediaType = response.getHeaders().getContentType();
            contentType = mediaType != null ? mediaType.toString() : guessContentType(imageUrl);
        } catch (Exception ex) {
            log.warn("Failed to download image for Meta upload: {}", imageUrl, ex);
            throw new BadRequestException(
                    "Could not download the image URL. Use a public HTTPS link to a .jpg or .png file (max 5 MB).");
        }

        if (bytes == null || bytes.length == 0) {
            throw new BadRequestException("Image URL returned an empty file.");
        }
        if (bytes.length > 5 * 1024 * 1024) {
            throw new BadRequestException("Image must be 5 MB or smaller for WhatsApp templates.");
        }
        if (contentType == null || !(contentType.contains("jpeg") || contentType.contains("jpg") || contentType.contains("png"))) {
            contentType = guessContentType(imageUrl);
        }
        if (!contentType.contains("jpeg") && !contentType.contains("jpg") && !contentType.contains("png")) {
            throw new BadRequestException("WhatsApp image headers only support JPEG or PNG.");
        }

        String fileName = fileNameFromUrl(imageUrl, contentType);
        String sessionId = startUploadSession(fileName, bytes.length, normalizeContentType(contentType));
        return uploadBinary(sessionId, bytes);
    }

    private void requireAppId() {
        String appId = properties.secrets().appId();
        if (appId == null || appId.isBlank()) {
            throw new BadRequestException(
                    "WHATSAPP_APP_ID is required to submit templates with images. "
                            + "Find it in Meta Developer → App settings → Basic → App ID.");
        }
    }

    @SuppressWarnings("unchecked")
    private String startUploadSession(String fileName, int fileLength, String fileType) {
        String path = "https://graph.facebook.com/%s/%s/uploads".formatted(
                properties.secrets().apiVersion(),
                properties.secrets().appId().trim());
        try {
            String raw = restClient.post()
                    .uri(uriBuilder -> URI.create(path + "?"
                            + "file_name=" + encode(fileName)
                            + "&file_length=" + fileLength
                            + "&file_type=" + encode(fileType)
                            + "&access_token=" + encode(properties.secrets().accessToken())))
                    .retrieve()
                    .body(String.class);
            Map<String, Object> body = MetaGraphJson.parseMap(raw);
            Object id = body.get("id");
            if (id == null || String.valueOf(id).isBlank()) {
                throw new BadRequestException("Meta did not return an upload session id.");
            }
            return String.valueOf(id);
        } catch (RestClientException ex) {
            log.warn("Meta upload session start failed", ex);
            throw new BadRequestException("Meta media upload failed to start. " + shortMsg(ex));
        }
    }

    @SuppressWarnings("unchecked")
    private String uploadBinary(String sessionId, byte[] bytes) {
        String url = "https://graph.facebook.com/%s/%s".formatted(
                properties.secrets().apiVersion(),
                sessionId.startsWith("upload:") ? sessionId : sessionId);
        try {
            String raw = restClient.post()
                    .uri(URI.create(url))
                    .header("file_offset", "0")
                    .header("Authorization", "OAuth " + properties.secrets().accessToken())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes)
                    .retrieve()
                    .body(String.class);
            Map<String, Object> body = MetaGraphJson.parseMap(raw);
            Object handle = body.get("h");
            if (handle == null || String.valueOf(handle).isBlank()) {
                throw new BadRequestException("Meta did not return a media handle for the image.");
            }
            return String.valueOf(handle);
        } catch (RestClientException ex) {
            log.warn("Meta binary upload failed", ex);
            throw new BadRequestException("Meta media upload failed. " + shortMsg(ex));
        }
    }

    public static void validateHttpsImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BadRequestException("Image URL is required for Utility templates with an image header.");
        }
        String url = imageUrl.trim();
        if (!url.toLowerCase(Locale.ROOT).startsWith("https://")) {
            throw new BadRequestException("Image URL must start with https://");
        }
    }

    private static String guessContentType(String url) {
        String lower = url.toLowerCase(Locale.ROOT);
        if (lower.contains(".png")) {
            return "image/png";
        }
        return "image/jpeg";
    }

    private static String normalizeContentType(String contentType) {
        if (contentType.contains("png")) {
            return "image/png";
        }
        return "image/jpeg";
    }

    private static String fileNameFromUrl(String url, String contentType) {
        try {
            String path = URI.create(url).getPath();
            if (path != null && path.contains(".")) {
                String name = path.substring(path.lastIndexOf('/') + 1);
                if (!name.isBlank()) {
                    return name;
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        return contentType.contains("png") ? "template.png" : "template.jpg";
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String shortMsg(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            return "Unknown error.";
        }
        return msg.length() > 250 ? msg.substring(0, 250) + "…" : msg;
    }
}
