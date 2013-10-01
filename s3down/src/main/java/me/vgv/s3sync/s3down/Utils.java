package me.vgv.s3sync.s3down;

import me.vgv.s3sync.common.FatalException;
import me.vgv.s3sync.common.config.S3Settings;
import me.vgv.s3sync.s3down.config.Config;
import org.apache.commons.cli.*;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class Utils {

	public static Config parseConfig(String[] args) {
		Options options = new Options();

		options.addOption("help", false, "Print help");
		options.addOption("accessKey", true, "Access key");
		options.addOption("secretKey", true, "Secret key");
		options.addOption("credFile", true, "File with access key and/or secret key");
		options.addOption("bucket", true, "Bucket");
		options.addOption("threads", true, "Upload threads");
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

		return new Config(s3Settings, threads, key, local);
	}
}
