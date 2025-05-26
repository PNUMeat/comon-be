package site.codemonster.comon.global.images.dto;

import site.codemonster.comon.global.error.images.ImageFileNameException;
import site.codemonster.comon.global.error.images.ImageFileTypeException;
import site.codemonster.comon.global.images.enums.ImageCategory;

import java.util.UUID;

public record PresignedUrlMetadata(
	String uniqueFileName,
	String objectKey,
	String contentType
) {
	public static PresignedUrlMetadata from(PresignedUrlRequest request, ImageCategory category) {
		if (request.fileName() == null || request.fileName().isBlank()) {
			throw new ImageFileNameException();
		}
		if (request.contentType() == null || request.contentType().isBlank()) {
			throw new ImageFileTypeException();
		}

		String uniqueFileName = UUID.randomUUID() + "_" + request.fileName();
		String objectKey = category.getFolderName() + "/" + uniqueFileName;
		return new PresignedUrlMetadata(uniqueFileName, objectKey, request.contentType());
	}
}
