package me.vgv.s3sync.s3down.config;

import me.vgv.s3sync.common.config.S3Settings;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class Config {

	private final S3Settings s3Settings;
	private final int threads;
	private final String key;
	private final String local;

	public Config(S3Settings s3Settings, int threads, String key, String local) {
		this.s3Settings = s3Settings;
		this.threads = threads;
		this.key = key;
		this.local = local;
	}

	public S3Settings getS3Settings() {
		return s3Settings;
	}

	public int getThreads() {
		return threads;
	}

	public String getKey() {
		return key;
	}

	public String getLocal() {
		return local;
	}
}
