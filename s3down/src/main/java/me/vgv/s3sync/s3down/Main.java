package me.vgv.s3sync.s3down;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import me.vgv.s3sync.s3down.config.Config;

import java.io.InputStream;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class Main {

	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		});

		// parse config
		Config config = Utils.parseConfig(args);

		long start = System.currentTimeMillis();

		// download file
		AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials(config.getS3Settings().getAccessKey(), config.getS3Settings().getSecretKey()));
		S3Object object = s3Client.getObject(config.getS3Settings().getBucket(), config.getKey());
		InputStream inputStream = object.getObjectContent();
		me.vgv.s3sync.common.Utils.saveStreamToFileAndClose(inputStream, config.getLocal());

		System.out.println("Downloaded in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
	}

}
