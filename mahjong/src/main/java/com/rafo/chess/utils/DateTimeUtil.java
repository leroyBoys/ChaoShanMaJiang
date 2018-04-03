package com.rafo.chess.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class DateTimeUtil
{
	public static final SimpleDateFormat daySdf = new SimpleDateFormat("yyyyMMdd");
	public static final int C_ONE_SECOND = 1000;
	public static final int C_ONE_MINUTE = 60 * C_ONE_SECOND;
	public static final int C_ONE_HOUR = 60 * C_ONE_MINUTE;
	public static final int C_ONE_DAY = 24 * C_ONE_HOUR;

	public static final int M_ONE_MINUTE = 60;
	public static final int M_ONE_HOUR = 60 * M_ONE_MINUTE;
	public static final int M_ONE_DAY = 24 * M_ONE_HOUR;

	private static String timeZone = "GMT+8";
	private static ReentrantReadWriteLock timeLock = new ReentrantReadWriteLock();
	/*
	 * 获取Date的字符串格式，以北京时间为准
	 */
	public static String getGMT8String(Date date)
	{
		try
		{
			timeLock.readLock().lock();
			DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			return dateFormat.format(date);
		}
		finally
		{
			timeLock.readLock().unlock();
		}
	}

	public static String getToday(){
		return getDateString(new Date(),daySdf);
	}

	public static String getDateString(Date date,SimpleDateFormat dateFormat)
	{
		try
		{
			timeLock.readLock().lock();
			dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
			return dateFormat.format(date);
		}
		finally
		{
			timeLock.readLock().unlock();
		}
	}
	
	public static long getCurrentTimeMillis()
	{
		try
		{
			timeLock.readLock().lock();
			return System.currentTimeMillis();
		}
		finally
		{
			timeLock.readLock().unlock();
		}
	}
	
	public static Date getNowDate()
	{
		return new Date();
	}
	
	public static Date getDate(Long milliseconds)
	{
		return new Date(milliseconds);
	}
	
	/**
	 * 两天前的00点时间，例如：2016.06.06返回2016.06.04.0.0.0
	 */
	public static Date getTwoDayAgo(Date today)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String todayStr = sdf.format(today);
		Calendar c = Calendar.getInstance();
		Date todayZero = null;
		try {
			todayZero = sdf.parse(todayStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		c.setTime(todayZero);
		int day = c.get(Calendar.DATE);
		c.set(Calendar.DATE, day - 2);

		return c.getTime();
	}
	
	public static long getDateDiff(long date1, long date2)
	{
		return date1 - date2;
	}

	public static String getDay(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);

		return String.format("%d%02d%02d",year, month, day);
	}
}
