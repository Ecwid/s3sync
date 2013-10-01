package me.vgv.s3up;

import com.google.common.io.Files;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class UtilsTest {

	@Test
	public void testScanDirectory() throws Exception {
		File tempDir = Files.createTempDir();

		// create dir
		File internalDir = new File(tempDir, "some-dir");
		internalDir.mkdir();

		// create files
		File first = new File(tempDir, "first");
		first.createNewFile();

		File second = new File(tempDir, "second");
		second.createNewFile();

		File third = new File(internalDir, "third");
		third.createNewFile();

		// check
		List<String> files = Utils.scanDirectory(tempDir);
		Assert.assertEquals(files.size(), 3);
		Assert.assertTrue(files.contains(first.getAbsolutePath()));
		Assert.assertTrue(files.contains(second.getAbsolutePath()));
		Assert.assertTrue(files.contains(third.getAbsolutePath()));
	}

	@Test
	public void testReadCredFile() throws Exception {
		File tempFile = File.createTempFile("s3up-test-", null);
		FileOutputStream outputStream = new FileOutputStream(tempFile);
		outputStream.write("first=1\nsecond=2\nthird=3".getBytes("UTF-8"));

		Map<String, String> props = Utils.readCredFile(tempFile.getAbsolutePath());
		Assert.assertEquals(props.size(), 3);
		Assert.assertEquals(props.get("first"), "1");
		Assert.assertEquals(props.get("second"), "2");
		Assert.assertEquals(props.get("third"), "3");
	}

	@Test
	public void testParseExpiresFile_FirstCase() throws Exception {
		{
			Date date = Utils.parseExpiresDate("1 year");
			long delta = date.getTime() - System.currentTimeMillis();
			Assert.assertTrue(Math.abs(delta - 1 * 1000L * 86400 * 365) < 100);
		}

		{
			Date date = Utils.parseExpiresDate("3 year");
			long delta = date.getTime() - System.currentTimeMillis();
			Assert.assertTrue(Math.abs(delta - 3 * 1000L * 86400 * 365) < 100);
		}
		{
			Date date = Utils.parseExpiresDate("2 month");
			long delta = date.getTime() - System.currentTimeMillis();
			Assert.assertTrue(Math.abs(delta - 2 * 1000L * 86400 * 31) < 100);
		}
		{
			Date date = Utils.parseExpiresDate("1 day");
			long delta = date.getTime() - System.currentTimeMillis();
			Assert.assertTrue(Math.abs(delta - 1000L * 86400) < 100);
		}
		{
			Date date = Utils.parseExpiresDate("5 year");
			long delta = date.getTime() - System.currentTimeMillis();
			Assert.assertTrue(Math.abs(delta - 5 * 1000L * 86400 * 365) < 100);
		}
		{
			Date date = Utils.parseExpiresDate("7 month");
			long delta = date.getTime() - System.currentTimeMillis();
			Assert.assertTrue(Math.abs(delta - 7 * 1000L * 86400 * 31) < 100);
		}
	}

}
