/*
 The class encapsulates the frontier, the queue of unvisited pages for a web crawler.
 The queue is implemented as a priority queue, based on a score of each URL.
*/

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.omg.CORBA.Current;

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
    
    public void add(String url, double score) {
	/********************************************************/
	/* GAP!													*/
	/* Adds a new URLScore to the frontier, but only if		*/
	/* the url is not already there.						*/
	/********************************************************/
    		// If the url has been visited before, we don't need to do anything else
        	if (!theURLs.containsKey(url)) {
        		try {
        			URLScore theUrl = new URLScore(url, score);
        			queueURLs.add(theUrl);
            		theURLs.put(url, 0);	// we've seen this url before
            		totalCount++;
            	} catch (MalformedURLException e) {
            		if (debug) {
            			System.out.println(e.getMessage());
            		}
        		}
        	}
    }

    public URLScore removeNext() {
	/********************************************************/
	/* GAP!							*/
	/* Remove and return the next URLScore in the frontier 	*/
	/********************************************************/
    	return queueURLs.poll();
    	
    }

    public boolean isEmpty() {
    	return theURLs.isEmpty();
    }

    public int totalCount() {
    	return totalCount;
    }
    
    public int totalDomains() {
    	return theURLs.size();
    }
}
