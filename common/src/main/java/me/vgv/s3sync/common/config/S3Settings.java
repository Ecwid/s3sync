package me.vgv.s3sync.common.config;

import com.google.common.base.Preconditions;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class S3Settings {

	private final String accessKey;
	private final String secretKey;
	private final String bucket;

	public S3Settings(String accessKey, String secretKey, String bucket) {
		Preconditions.checkNotNull(accessKey, "accessKey is null");
		Preconditions.checkNotNull(secretKey, "secretKey is null");
		Preconditions.checkNotNull(bucket, "bucket is null");

		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.bucket = bucket;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getBucket() {
		return bucket;
	}
}
