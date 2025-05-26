package site.codemonster.comon.global.images.dto;

public record PresignedUrlResponse(
	String fileName,
	String presignedUrl,
	String contentType
) {
}
