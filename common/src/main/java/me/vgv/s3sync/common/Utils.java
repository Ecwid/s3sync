package me.vgv.s3sync.common;

import me.vgv.s3sync.common.config.S3Settings;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class Utils {

	public static List<String> scanDirectory(File directory) {
		List<String> result = new ArrayList<String>();

		File[] files = directory.listFiles();
		if (files != null) {
			for (File item : files) {
				if (item.isDirectory()) {
					result.addAll(scanDirectory(item));
				} else if (item.isFile()) {
					result.add(item.getAbsolutePath());
				}
			}
		}

		return result;
	}

	public static Map<String, String> readCredFile(String filePath) {
		Properties properties = new Properties();

		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
			properties.load(inputStream);
		} catch (IOException e) {
			throw new FatalException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// NOP
				}
			}
		}

		Map<String, String> result = new HashMap<String, String>();
		for (String name : properties.stringPropertyNames()) {
			String value = properties.getProperty(name);
			if (value != null) {
				result.put(name, value);
			}
		}
		return result;
	}

	public static S3Settings parseS3Settings(CommandLine commandLine) {
		Map<String, String> propsFromFile = new HashMap<String, String>();
		if (commandLine.hasOption("credFile")) {
			String credFile = commandLine.getOptionValue("credFile");
			propsFromFile = readCredFile(credFile);
		}

		String accessKey = propsFromFile.get("accessKey");
		if (commandLine.hasOption("accessKey")) {
			accessKey = commandLine.getOptionValue("accessKey");
		}

		String secretKey = propsFromFile.get("secretKey");
		if (commandLine.hasOption("secretKey")) {
			secretKey = commandLine.getOptionValue("secretKey");
		}

		String bucket = propsFromFile.get("bucket");
		if (commandLine.hasOption("bucket")) {
			bucket = commandLine.getOptionValue("bucket");
		}

		return new S3Settings(accessKey, secretKey, bucket);
	}
}
