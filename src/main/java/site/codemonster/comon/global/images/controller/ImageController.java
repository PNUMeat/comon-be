package site.codemonster.comon.global.images.controller;

import site.codemonster.comon.global.error.dto.response.ApiResponse;
import site.codemonster.comon.global.images.dto.PresignedUrlMetadata;
import site.codemonster.comon.global.images.dto.PresignedUrlRequest;
import site.codemonster.comon.global.images.dto.PresignedUrlResponse;
import site.codemonster.comon.global.images.enums.ImageCategory;
import site.codemonster.comon.global.images.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static site.codemonster.comon.global.response.ResponseMessageEnum.*;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
	private final ImageService imageService;

	@PostMapping("/presigned-url")
	public ResponseEntity<?> getPresignedUrl(
		@RequestBody PresignedUrlRequest request,
		@RequestParam ImageCategory imageCategory
	) {
		PresignedUrlMetadata metadata = PresignedUrlMetadata.from(request, imageCategory);
		PresignedUrlResponse response = imageService.generatePresignedUrl(metadata);

		return ResponseEntity.status(PRESIGNED_URL_SUCCESS.getStatusCode())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ApiResponse.successResponse(response, PRESIGNED_URL_SUCCESS.getMessage()));
	}

	@PostMapping("/presigned-url/list")
	public ResponseEntity<?> getPresignedUrls(
		@RequestBody List<PresignedUrlRequest> requests,
		@RequestParam ImageCategory imageCategory
	) {
		List<PresignedUrlMetadata> metadataList = requests.stream()
			.map(req -> PresignedUrlMetadata.from(req, imageCategory))
			.toList();

		List<PresignedUrlResponse> responseList = imageService.generatePresignedUrlList(metadataList);

		return ResponseEntity.status(PRESIGNED_URL_SUCCESS.getStatusCode())
			.contentType(MediaType.APPLICATION_JSON)
			.body(ApiResponse.successResponse(responseList, PRESIGNED_URL_SUCCESS.getMessage()));
	}
}
