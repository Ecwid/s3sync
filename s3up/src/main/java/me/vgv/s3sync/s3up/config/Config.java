package me.vgv.s3sync.s3up.config;

import com.google.common.collect.ImmutableList;
import me.vgv.s3sync.common.config.S3Settings;
import me.vgv.s3sync.s3up.UploadFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class Config {

	private final S3Settings s3Settings;
	private final boolean rrs;
	private final String cacheControl;
	private final Date expires;
	private final String charset;
	private final Map<String,String> userMetadata;
	private final int threads;
	private final boolean useGzip;
	private final boolean useZopfli;
	private final List<UploadFile> uploadFiles;

	public Config(S3Settings s3Settings, int threads, boolean useGzip, boolean useZopfli, List<UploadFile> uploadFiles, boolean rrs, String cacheControl, Date expires, String charset, Map<String,String> userMetadata) {
		this.s3Settings = s3Settings;
		this.threads = threads;
		this.useGzip = useGzip;
		this.useZopfli = useZopfli;
		this.rrs = rrs;
		this.cacheControl = cacheControl;
		this.expires = expires;
		this.uploadFiles = uploadFiles == null ? ImmutableList.<UploadFile>of() : ImmutableList.copyOf(uploadFiles);
		this.charset = charset;
		this.userMetadata = userMetadata;
	}

	public S3Settings getS3Settings() {
		return s3Settings;
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

	public int getThreads() {
		return threads;
	}

	public boolean isUseGzip() {
		return useGzip;
	}

	public boolean isUseZopfli() {
		return useZopfli;
	}

	public List<UploadFile> getUploadFiles() {
		return uploadFiles;
	}

	public String getCharset() {
		return charset;
	}

	public Map<String, String> getUserMetadata() {
		return userMetadata;
	}
}
