package me.vgv.s3up;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;
import me.vgv.s3up.config.Config;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class UploadTask implements Runnable {

	private static final ThreadLocal<MimetypesFileTypeMap> MIME_TYPES = new ThreadLocal<MimetypesFileTypeMap>() {
		@Override
		protected MimetypesFileTypeMap initialValue() {
			return new MimetypesFileTypeMap();
		}
	};

	private static final ThreadLocal<AmazonS3> S3_CLIENT = new ThreadLocal<AmazonS3>();

	private final Config config;
	private final UploadFile uploadFile;

	public UploadTask(Config config, UploadFile uploadFile) {
		this.config = config;
		this.uploadFile = uploadFile;
	}

	private AmazonS3 getS3Client() {
		AmazonS3 amazonS3 = S3_CLIENT.get();
		if (amazonS3 == null) {
			amazonS3 = new AmazonS3Client(new BasicAWSCredentials(config.getS3Settings().getAccessKey(), config.getS3Settings().getSecretKey()));
			S3_CLIENT.set(amazonS3);
		}

		return amazonS3;
	}

	@Override
	public void run() {
		try {
			internalRun();
		} catch (Exception e) {
			throw new FatalException(e);
		}
	}

	private void internalRun() {
		AmazonS3 s3Client = getS3Client();

		// configure headers
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(MIME_TYPES.get().getContentType(uploadFile.getFile()));
		if (config.isGzipped()) {
			objectMetadata.setContentEncoding("gzip");
		}
		if (config.getS3Settings().getCacheControl() != null) {
			objectMetadata.setCacheControl(config.getS3Settings().getCacheControl());
		}
		if (config.getS3Settings().getExpires() != null) {
			objectMetadata.setHttpExpiresDate(config.getS3Settings().getExpires());
		}

		// if gzipped - compress file
		File file;
		if (config.isGzipped()) {
			file = Utils.compressFile(uploadFile.getFile());
			file.deleteOnExit();
		} else {
			file = new File(uploadFile.getFile());
		}

		// create put request
		PutObjectRequest putObjectRequest = new PutObjectRequest(config.getS3Settings().getBucket(), uploadFile.getKey(), file);
		if (config.getS3Settings().isRrs()) {
			putObjectRequest.setStorageClass(StorageClass.ReducedRedundancy);
		}
		putObjectRequest.withMetadata(objectMetadata);

		s3Client.putObject(putObjectRequest);
		System.out.println("Upload " + uploadFile.getFile());
	}
}
