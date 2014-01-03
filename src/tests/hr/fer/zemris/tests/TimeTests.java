package hr.fer.zemris.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hr.fer.zemris.util.time.DateStampCache;
import hr.fer.zemris.util.time.TemporalList;
import hr.fer.zemris.util.time.TimeSpanCache;
import hr.fer.zemris.util.time.TimeStamp;
import hr.fer.zemris.util.time.TimeStampCache;
import hr.fer.zemris.util.time.TimeStampOverflowException;

import org.junit.Test;

public class TimeTests {

	@Test
	public void test1() {
		TimeSpanCache timeSpanCache = new TimeSpanCache();
		TimeStampCache timeStampCache = new TimeStampCache();
		DateStampCache dateStampCache = new DateStampCache();
		TemporalList tlist = new TemporalList(timeSpanCache);
		tlist.addInterval(
			dateStampCache.get("2008-09-08"),
			timeSpanCache.get(timeStampCache.get(8, 0), timeStampCache.get(10, 0)),
			"Predavanje 08-10"
		);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(11, 0), timeStampCache.get(13, 0)),
				"Predavanje 11-13"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(7, 0), timeStampCache.get(15, 0)),
				"Labosi 7-15"
			);
		assertNotNull(tlist);
		System.out.println(tlist);
	}

	@Test
	public void test2() {
		TimeSpanCache timeSpanCache = new TimeSpanCache();
		TimeStampCache timeStampCache = new TimeStampCache();
		DateStampCache dateStampCache = new DateStampCache();
		TemporalList tlist = new TemporalList(timeSpanCache);
		tlist.addInterval(
			dateStampCache.get("2008-09-08"),
			timeSpanCache.get(timeStampCache.get(8, 0), timeStampCache.get(10, 0)),
			"Predavanje 08-10"
		);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(11, 0), timeStampCache.get(13, 0)),
				"Predavanje 11-13"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(9, 0), timeStampCache.get(12, 0)),
				"Labosi 7-15"
			);
		assertNotNull(tlist);
		System.out.println(tlist);
	}

	@Test
	public void test3() {
		TimeSpanCache timeSpanCache = new TimeSpanCache();
		TimeStampCache timeStampCache = new TimeStampCache();
		DateStampCache dateStampCache = new DateStampCache();
		TemporalList tlist = new TemporalList(timeSpanCache);
		tlist.addInterval(
			dateStampCache.get("2008-09-08"),
			timeSpanCache.get(timeStampCache.get(8, 0), timeStampCache.get(10, 0)),
			"Predavanje 08-10"
		);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(11, 0), timeStampCache.get(13, 0)),
				"Predavanje 11-13"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(7, 0), timeStampCache.get(7, 30)),
				"Labosi 7:00-7:30"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(8, 30), timeStampCache.get(9, 30)),
				"Labosi 8:30-9:30"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(10, 30), timeStampCache.get(10, 45)),
				"Labosi 10:30-10:45"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(14, 0), timeStampCache.get(15, 0)),
				"Labosi 14:00-15:00"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(6, 0), timeStampCache.get(6, 30)),
				"Labosi 6:00-6:30"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(15, 0), timeStampCache.get(16, 0)),
				"Labosi 15:00-16:00"
			);
		assertNotNull(tlist);
		System.out.println(tlist);
	}

	@Test
	public void test4() {
		TimeSpanCache timeSpanCache = new TimeSpanCache();
		TimeStampCache timeStampCache = new TimeStampCache();
		DateStampCache dateStampCache = new DateStampCache();
		TemporalList tlist = new TemporalList(timeSpanCache);
		tlist.addInterval(
			dateStampCache.get("2008-09-08"),
			timeSpanCache.get(timeStampCache.get(8, 0), timeStampCache.get(10, 0)),
			"Predavanje 08-10"
		);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(11, 0), timeStampCache.get(13, 0)),
				"Predavanje 11-13"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(7, 0), timeStampCache.get(7, 30)),
				"Labosi 7:00-7:30"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(8, 30), timeStampCache.get(9, 30)),
				"Labosi 8:30-9:30"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(10, 30), timeStampCache.get(10, 45)),
				"Labosi 10:30-10:45"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(14, 0), timeStampCache.get(15, 0)),
				"Labosi 14:00-15:00"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(6, 0), timeStampCache.get(6, 30)),
				"Labosi 6:00-6:30"
			);
		tlist.addInterval(
				dateStampCache.get("2008-09-08"),
				timeSpanCache.get(timeStampCache.get(15, 0), timeStampCache.get(16, 0)),
				"Labosi 15:00-16:00"
			);
		dateStampCache.get("2008-09-09");
		assertNotNull(tlist);
		System.out.println(tlist);
		System.out.println(tlist.createInversionList(dateStampCache.getDates(), timeStampCache.get(8, 0), timeStampCache.get(20, 0)));
	}
	
	@Test
	public void test5() {
		TimeStampCache timeStampCache = new TimeStampCache();
		TimeStamp ts = timeStampCache.get(8, 0);
		ts = ts.add(timeStampCache, 2, 13);
		assertEquals("Sati ne odgovaraju.", 10, ts.getHour());
		assertEquals("MInute ne odgovaraju.", 13, ts.getMinute());
		ts = timeStampCache.get(8, 0);
		ts = ts.add(timeStampCache, 2, 73);
		assertEquals("Sati ne odgovaraju.", 11, ts.getHour());
		assertEquals("MInute ne odgovaraju.", 13, ts.getMinute());
	}

	@Test
	public void test6() {
		TimeStampCache timeStampCache = new TimeStampCache();
		TimeStamp ts = timeStampCache.get(8, 0);
		ts.add(timeStampCache, 12, 0);
	}

	@Test(expected=TimeStampOverflowException.class)
	public void test7() {
		TimeStampCache timeStampCache = new TimeStampCache();
		TimeStamp ts = timeStampCache.get(8, 0);
		ts.add(timeStampCache, 19, 0);
	}

	@Test(expected=TimeStampOverflowException.class)
	public void test8() {
		TimeStampCache timeStampCache = new TimeStampCache();
		TimeStamp ts = timeStampCache.get(8, 0);
		ts.add(timeStampCache, 18, 1);
	}
}
