package com.modu.soccer.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
	private final AmazonS3Client s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;


	public String uploadFile(MultipartFile multipartFile) {
		validateFileExists(multipartFile);

		String fileName = createFileName(multipartFile.getOriginalFilename());

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(multipartFile.getContentType());

		try (InputStream inputStream = multipartFile.getInputStream()) {
			s3Client
				.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
				.withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (IOException e) {
			log.error("upload s3 exception: {}", e.getMessage());
			throw new CustomException(ErrorCode.UNKNOWN_ERROR);
		}
		return s3Client.getUrl(bucketName, fileName).toString();
	}

	private void validateFileExists(MultipartFile multipartFile) {
		if (multipartFile.isEmpty()) {
			log.warn("file is empty");
			throw new CustomException(ErrorCode.INVALID_PARAM);
		}
	}

	private String createFileName(String fileName) {
		return UUID.randomUUID().toString().concat(getFileExtension(fileName));
	}

	private String getFileExtension(String fileName) {
		try {
			return fileName.substring(fileName.lastIndexOf("."));
		} catch (StringIndexOutOfBoundsException e) {
			throw new CustomException(ErrorCode.INVALID_PARAM);
		}
	}
}
