package site.codemonster.comon.global.images.dto;

public record PresignedUrlRequest(
	String fileName,
	String contentType
) {
}
