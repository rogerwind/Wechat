package com.roger.kxmoment.util;

public class StopWatch {
	
	 private long start;

	    /**
	     * Creates a new StopWatch object.
	     */
	    public StopWatch() {
	        start = System.currentTimeMillis();
	    }

	    /**
	     * Format the specified millisecond time in hours, minutes, seconds, and milliseconds.
	     *
	     * @param timeMs the duration time in milliseconds
	     * @return the formatted duration
	     */
	    public String duration(long timeMs) {
	        long tSec = Math.round(timeMs / 1000);
	        long tMin = Math.round(tSec / 60);
	        long tHrs = Math.round(tMin / 60);

	        String fmtTime = null;

	        if (tSec > 0) {
	            timeMs = timeMs - (tSec * 1000);
	        }

	        if (tMin > 0) {
	            tSec = tSec - (tMin * 60);
	        }

	        if (tHrs > 0) {
	            tMin = tMin - (tHrs * 60);
	        }

	        if (tHrs > 0) {
	            fmtTime = (tHrs + "h " + tMin + "m " + tSec + "s " + timeMs + "ms");
	        } else if (tMin > 0) {
	            fmtTime = (tMin + "m " + tSec + "s " + timeMs + "ms");
	        } else if (tSec > 0) {
	            fmtTime = (tSec + "s " + timeMs + "ms");
	        } else {
	            fmtTime = (timeMs + "ms");
	        }

	        return fmtTime;
	    }


	    /**
	     * The elapsed time in milliseconds since started.
	     *
	     * @return the elapsed time in milliseconds
	     */
	    public long getElapsedMs() {
	        return System.currentTimeMillis() - start;
	    }


	    /**
	     * The elapsed time in seconds since started.
	     *
	     * @return the elapsed time in seconds
	     */
	    public double getElapsedSeconds() {
	        double elapsed = getElapsedMs();

	        return elapsed / 1000;
	    }


	    /**
	     * Set the stop watch start time to now.
	     *
	     * @return this stop watch
	     */
	    public StopWatch start() {
	        start = System.currentTimeMillis();

	        return this;
	    }


	    /**
	     * Returns a formatted string representation of the object.
	     *
	     * @return a formatted string representation of the object.
	     */
	    public String toString() {
	        return duration(getElapsedMs());
	    }
}
