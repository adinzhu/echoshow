package com.meari.echoshow.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Log4jUtil {
	
	public static final Log getLog(Class<?> obj) {
		Log log = LogFactory.getLog(obj);
		return log;
	}
}
