package me.vgv.s3up.config;

import com.google.common.base.Preconditions;

import java.util.Date;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class S3Settings {

	private final String accessKey;
	private final String secretKey;
	private final String bucket;
	private final boolean rrs;
	private final String cacheControl;
	private final Date expires;

	public S3Settings(String accessKey, String secretKey, String bucket, boolean rrs, String cacheControl, Date expires) {
		Preconditions.checkNotNull(accessKey, "accessKey is null");
		Preconditions.checkNotNull(secretKey, "secretKey is null");
		Preconditions.checkNotNull(bucket, "bucket is null");

		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.bucket = bucket;
		this.rrs = rrs;
		this.cacheControl = cacheControl;
		this.expires = expires;
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

	public boolean isRrs() {
		return rrs;
	}

	public String getCacheControl() {
		return cacheControl;
	}

	public Date getExpires() {
		return expires;
	}
}
