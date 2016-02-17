package me.vgv.s3sync.s3up;

import com.amazonaws.util.DateUtils;
import me.vgv.s3sync.common.FatalException;
import me.vgv.s3sync.common.config.S3Settings;
import me.vgv.s3sync.s3up.config.Config;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class Utils {

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

	public static List<UploadFile> parseUploadFiles(CommandLine commandLine) {
		// key
		String key = me.vgv.s3sync.common.Utils.extractRequiredOption(commandLine, "key");
		if (key == null) {
			throw new FatalException("'key' parameter not found");
		} else {
			while (key.length() > 0 && key.endsWith("/")) {
				key = key.substring(0, key.length() - 1);
			}
		}

		// local file or folder
		String local = me.vgv.s3sync.common.Utils.extractRequiredOption(commandLine, "local");
		if (local == null) {
			throw new FatalException("'local' parameter not found");
		}

		if (new File(local).isFile()) {
			UploadFile uploadFile = new UploadFile(key, local);
			return Arrays.asList(uploadFile);
		} else if (new File(local).isDirectory()) {
			File directory = new File(local);

			List<String> files = me.vgv.s3sync.common.Utils.scanDirectory(directory);
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
		options.addOption("zopfli", false, "Zopfli content before uploading");
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
		S3Settings s3Settings = me.vgv.s3sync.common.Utils.parseS3Settings(commandLine);

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

		// rrs
		boolean rrs = commandLine.hasOption("rrs");

		// Content-Encoding
		String cacheControl = commandLine.hasOption("cacheControl") ? commandLine.getOptionValue("cacheControl") : null;

		// expires
		Date expires = commandLine.hasOption("expires") ? parseExpiresDate(commandLine.getOptionValue("expires")) : null;


		// Content-Encoding
		boolean useGzip = commandLine.hasOption("gzipped");
		boolean useZopfli = commandLine.hasOption("zopfli");

		// upload files
		List<UploadFile> uploadFiles = parseUploadFiles(commandLine);

		return new Config(s3Settings, threads, useGzip, useZopfli, uploadFiles, rrs, cacheControl, expires);
	}

}
