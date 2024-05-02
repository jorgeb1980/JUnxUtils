package unxutils.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static unxutils.format.HumanReadableFormat.format;

/**
 * Human readable format test
 */
public class TestFormat {

	@Test
	public void testZeroFormat() {
		assertEquals("0", format(0));
	}
	
	@Test 
	public void testByteFormats() {
		assertEquals("1", format(1));
		assertEquals("10", format(10));
		assertEquals("100", format(100));
		assertEquals("999", format(999));
		assertEquals("1000", format(1000));
		assertEquals("1023", format(1023));
	}
	
	@Test
	public void testKbyteFormats() {
		assertEquals("1,0K", format(1024));
		assertEquals("1,2K", format(1207));
		assertEquals("95,8K", format(95.8 * 1024));
		assertEquals("1000,0K", format(1000 * 1024));
		assertEquals("1001,0K", format(1001 * 1024));
		assertEquals("1023,0K", format(1023 * 1024));
		assertEquals("1023,8K", format(1023.8 * 1024));
	}
	
	@Test
	public void testMegabyteFormats() {
		assertEquals("1,2M", format(1230333));
		assertEquals("1,2M", format(1240333));
		assertEquals("1,0M", format(1024*1024));
		assertEquals("2,0M", format(2*1024*1024));
		assertEquals("3,0M", format(3*1024*1024));
		assertEquals("999,0M", format(999*1024*1024));
		assertEquals("999,9M", format(999.9*1024*1024));
		assertEquals("999,9M", format(999.95*1024*1024));
		assertEquals("1000,4M", format(1000.4*1024*1024));
		assertEquals("1023,9M", format(1023.9*1024*1024));
	}
	
	@Test 
	public void testGigaBytesFormats() {
		assertEquals("1,2G", format(1230333 * 1024));
		assertEquals("1,2G", format(1240333 * 1024));
		assertEquals("1,0G", format(1024*1024*1024));
		assertEquals("2,0G", format(Long.parseUnsignedLong("2147483648")));
		assertEquals("3,0G", format(Long.parseUnsignedLong("3221225472")));
		assertEquals("27,0G", format(Long.parseUnsignedLong("28991029248")));
		BigDecimal bigDecimal = new BigDecimal("28991029248");
		bigDecimal = bigDecimal.add(new BigDecimal(100 * 1024 * 1024));
		assertEquals("27,1G", format(bigDecimal));
		BigDecimal bigDecimal2 = new BigDecimal("28991029248");
		bigDecimal2 = bigDecimal2.add(new BigDecimal(900 * 1024 * 1024));
		assertEquals("27,9G", format(bigDecimal2));
	}
	
}
