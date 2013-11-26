/*
 The class encapsulates the frontier, the queue of unvisited pages for a web crawler.
 The queue is implemented as a priority queue, based on a score of each URL.
*/

import java.net.MalformedURLException;
import java.util.*;

public class Frontier {
    private PriorityQueue<URLScore> queueURLs;  		// A priority queue of URLs
    private Hashtable<String, Integer> theURLs; 		// Each url string is also kept in a hash table for quick lookup
    private boolean debug =false;
    private final int initialCapacity=1000;     // Initial capacity of the queue
    private int totalCount=0;                   // The number of url added to the frontier

    public Frontier(boolean debug) {
        this.debug = debug;
        queueURLs = new PriorityQueue<URLScore>(initialCapacity);
        theURLs = new Hashtable<String, Integer>(initialCapacity);
    }
    
    /**
     * Adds a new URLScore to the frontier if the url has not been visited before
     * @param url		String
     * @param score		Priority score of the url
     */
    public void add(String url, double score) {
    		// If the url has been visited before, we don't need to do anything else
        	if (!theURLs.containsKey(url)) {
        		try {
        			URLScore theUrl = new URLScore(url, score);
        			queueURLs.add(theUrl);
            		theURLs.put(url, 0);		// Log this as a seen URL
            		totalCount++;
            	} catch (MalformedURLException e) {
            		if (debug) {
            			System.out.println(e.getMessage());
            		}
        		}
        	}
    }

    /**
     * @return the highest priority URL or null if the queue is empty
     */
    public URLScore removeNext() {
    	return queueURLs.poll();
    	
    }

    /**
     * @return true if the queue is empty, false otherwise 
     */
    public boolean isEmpty() {
    	return queueURLs.isEmpty();
    }

    /**
     * @return the total number of distinctive URLs found (including those visited) 
     */
    public int totalCount() {
    	return totalCount;
    }
}
