package me.vgv.s3up;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class UploadFile {

	private final String key;
	private final String file;

	public UploadFile(String key, String file) {
		while (key.length() > 0 && key.startsWith("/")) {
			key = key.substring(1);
		}

		this.key = key;
		this.file = file;
	}

	public String getKey() {
		return key;
	}

	public String getFile() {
		return file;
	}
}
