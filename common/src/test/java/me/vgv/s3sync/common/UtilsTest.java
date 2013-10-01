package me.vgv.s3sync.common;

import com.google.common.io.Files;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
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
		List<String> files = me.vgv.s3sync.common.Utils.scanDirectory(tempDir);
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

		Map<String, String> props = me.vgv.s3sync.common.Utils.readCredFile(tempFile.getAbsolutePath());
		Assert.assertEquals(props.size(), 3);
		Assert.assertEquals(props.get("first"), "1");
		Assert.assertEquals(props.get("second"), "2");
		Assert.assertEquals(props.get("third"), "3");
	}

}
