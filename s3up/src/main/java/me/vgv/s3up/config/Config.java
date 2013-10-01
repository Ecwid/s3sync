package me.vgv.s3up.config;

import com.google.common.collect.ImmutableList;
import me.vgv.s3up.UploadFile;

import java.util.List;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class Config {

	private final S3Settings s3Settings;
	private final int threads;
	private final boolean gzipped;
	private final List<UploadFile> uploadFiles;

	public Config(S3Settings s3Settings, int threads, boolean gzipped, List<UploadFile> uploadFiles) {
		this.s3Settings = s3Settings;
		this.threads = threads;
		this.gzipped = gzipped;
		this.uploadFiles = uploadFiles == null ? ImmutableList.<UploadFile>of() : ImmutableList.copyOf(uploadFiles);
	}

	public S3Settings getS3Settings() {
		return s3Settings;
	}

	public int getThreads() {
		return threads;
	}

	public boolean isGzipped() {
		return gzipped;
	}

	public List<UploadFile> getUploadFiles() {
		return uploadFiles;
	}
}
