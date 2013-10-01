package me.vgv.s3sync.s3up;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class UtilsTest {

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
