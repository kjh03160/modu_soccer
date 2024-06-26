package com.modu.soccer.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsS3Config {

	@Value("${aws.credentials.access_key}")
	private String accessKey;

	@Value("${aws.credentials.secret_key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Bean
	public AmazonS3Client s3Client() {
		BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);
		return (AmazonS3Client) AmazonS3ClientBuilder.standard()
			.withRegion(region)
			.withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
			.build();
	}
}