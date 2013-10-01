package me.vgv.s3up;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public final class FatalException extends RuntimeException {

	public FatalException(String message) {
		super(message);
	}

	public FatalException(Throwable cause) {
		super(cause);
	}
}
