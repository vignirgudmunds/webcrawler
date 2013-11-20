/*
 The class encapsulates the frontier, the queue of unvisited pages for a web crawler.
 The queue is implemented as a priority queue, based on a score of each URL.
*/

import java.net.MalformedURLException;
import java.util.*;

public class Frontier {
    private PriorityQueue<URLScore> queueURLs;  // A priority queue of URLs
    private Hashtable<String, Integer> theURLs; // Each url string is also kept in a hash table for quick lookup
    private boolean debug=false;
    private final int initialCapacity=1000;     // Initial capacity of the queue
    private int totalCount=0;                   // The number of url added to the frontier


    public Frontier(boolean debug) {
        this.debug = debug;
        queueURLs = new PriorityQueue<URLScore>(initialCapacity);
        theURLs = new Hashtable<String, Integer>(initialCapacity);
    }

    public void add(String url, double score) {
	/********************************************************/
	/* GAP!													*/
	/* Adds a new URLScore to the frontier, but only if		*/
	/* the url is not already there.						*/
	/********************************************************/
    	if (!theURLs.containsKey(url)) {
    		try {
        		queueURLs.add(new URLScore(url, score));
        		theURLs.put(url, 0);
        		totalCount++;
        	} catch (MalformedURLException e) {
        		System.out.println(e.getMessage());
    			// TODO: handle exception
    		}
    	}
    }

    public URLScore removeNext() {
	/********************************************************/
	/* GAP!							*/
	/* Remove and return the next URLScore in the frontier 	*/
	/********************************************************/
    	// TODO will return null if queue empty
    	totalCount--;
    	return queueURLs.poll();
    }

    public boolean isEmpty() {
        return (queueURLs.isEmpty());
    }

    public int totalCount() {
        return totalCount;
    }

}
