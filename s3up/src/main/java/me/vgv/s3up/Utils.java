package me.vgv.s3up;

import com.amazonaws.util.DateUtils;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import me.vgv.s3up.config.Config;
import me.vgv.s3up.config.S3Settings;
import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

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

	public static Date parseExpiresDate(String expires) {
		final long DAY = 1000L * 86400;
		final long MONTH = 31 * DAY;
		final long YEAR = 365 * DAY;
		final Pattern deltaExpiresPattern = Pattern.compile("([0-9]+) (day|month|year)");

		Matcher matcher = deltaExpiresPattern.matcher(expires);
		if (matcher.find()) {
			int length = Integer.parseInt(matcher.group(1));
			String period = matcher.group(2);
			if ("day".equals(period)) {
				return new Date(System.currentTimeMillis() + length * DAY);
			} else if ("month".equals(period)) {
				return new Date(System.currentTimeMillis() + length * MONTH);
			} else {
				return new Date(System.currentTimeMillis() + length * YEAR);
			}
		} else {
			try {
				return new DateUtils().parseRfc822Date(expires);
			} catch (java.text.ParseException e) {
				throw new FatalException(e);
			}
		}
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

		String bucket = null;
		if (commandLine.hasOption("bucket")) {
			bucket = commandLine.getOptionValue("bucket");
		}

		// rrs
		boolean rrs = commandLine.hasOption("rrs");

		// Content-Encoding
		String cacheControl = commandLine.hasOption("cacheControl") ? commandLine.getOptionValue("cacheControl") : null;

		// expires
		Date expires = commandLine.hasOption("expires") ? parseExpiresDate(commandLine.getOptionValue("expires")) : null;

		return new S3Settings(accessKey, secretKey, bucket, rrs, cacheControl, expires);
	}

	public static List<UploadFile> parseUploadFiles(CommandLine commandLine) {
		// key
		String key = extractRequiredOption(commandLine, "key");
		if (key == null) {
			throw new FatalException("'key' parameter not found");
		} else {
			while (key.length() > 0 && key.endsWith("/")) {
				key = key.substring(0, key.length() - 1);
			}
		}

		// local file or folder
		String local = extractRequiredOption(commandLine, "local");
		if (local == null) {
			throw new FatalException("'local' parameter not found");
		}

		if (new File(local).isFile()) {
			UploadFile uploadFile = new UploadFile(key, local);
			return Arrays.asList(uploadFile);
		} else if (new File(local).isDirectory()) {
			File directory = new File(local);

			List<String> files = Utils.scanDirectory(directory);
			List<UploadFile> uploadFiles = new ArrayList<UploadFile>();
			for (String file : files) {
				String part = file.substring(directory.getAbsolutePath().length());
				if (part.startsWith(File.separator)) {
					part = part.substring(1);
				}

				String uploadKey = key + "/" + part;
				uploadFiles.add(new UploadFile(uploadKey, file));
			}

			return uploadFiles;
		} else {
			throw new FatalException("Path '" + local + "' isn't file or directory");
		}
	}

	public static Config parseConfig(String[] args) {
		Options options = new Options();

		options.addOption("help", false, "Print help");
		options.addOption("accessKey", true, "Access key");
		options.addOption("secretKey", true, "Secret key");
		options.addOption("credFile", true, "File with access key and/or secret key");
		options.addOption("bucket", true, "Bucket");
		options.addOption("threads", true, "Upload threads");
		options.addOption("rrs", false, "Use Reduced Redundancy storage");
		options.addOption("gzipped", false, "GZip content before uploading");
		options.addOption("cacheControl", true, "Cache-Control header");
		options.addOption("expires", true, "Expires header");
		options.addOption("key", true, "S3 key");
		options.addOption("local", true, "Local file or folder");

		CommandLine commandLine;
		try {
			Parser parser = new BasicParser();
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			throw new FatalException(e);
		}

		if (commandLine.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar s3up.jar", options, true);
			System.exit(0);
		}

		// S3settings
		S3Settings s3Settings = parseS3Settings(commandLine);

		// threads
		String threadsStr = commandLine.getOptionValue("threads");
		int threads;
		if (threadsStr == null) {
			threads = 2;
		} else {
			try {
				threads = Integer.parseInt(threadsStr);
			} catch (NumberFormatException e) {
				threads = 2;
			}
		}

		// Content-Encoding
		boolean gzipped = commandLine.hasOption("gzipped");

		// upload files
		List<UploadFile> uploadFiles = parseUploadFiles(commandLine);

		return new Config(s3Settings, threads, gzipped, uploadFiles);
	}

	public static File compressFile(String file) {
		try {
			InputStream inputStream = null;
			OutputStream outputStream = null;
			try {
				File gzippedFile = File.createTempFile("s3up_", null);
				inputStream = new FileInputStream(file);
				outputStream = new GZIPOutputStream(new FileOutputStream(gzippedFile));
				ByteStreams.copy(inputStream, outputStream);
				return gzippedFile;
			} finally {
				Closeables.close(inputStream, true);
				Closeables.close(outputStream, true);
			}
		} catch (IOException e) {
			throw new FatalException(e);
		}
	}

	private static String extractRequiredOption(CommandLine commandLine, String optionName) {
		if (commandLine.hasOption(optionName)) {
			return commandLine.getOptionValue(optionName);
		} else {
			System.out.println("-" + optionName + " argument not found. Use -help parameter");
			return null;
		}
	}

}
