/**
 * 
 */
package ru.futurelink.mo.web.utils;

import java.io.InputStream;
import java.util.Scanner;

/**
 * @author pavlov
 *
 */
public class MOUtils {
	@SuppressWarnings("resource")
	public static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}
