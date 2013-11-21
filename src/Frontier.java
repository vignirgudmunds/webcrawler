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
    private Hashtable<String, Integer> visitedHosts; 	// Each host string is also kept in a hash table for quick lookup
    private boolean debug =false;
    private final int initialCapacity=1000;     // Initial capacity of the queue
    private int totalCount=0;                   // The number of url added to the frontier


    public Frontier(boolean debug) {
        this.debug = debug;
        queueURLs = new PriorityQueue<URLScore>(initialCapacity);
        theURLs = new Hashtable<String, Integer>(initialCapacity);
        visitedHosts = new Hashtable<String, Integer>(initialCapacity/2);
    }

    public void add(String url, double score) {
	/********************************************************/
	/* GAP!													*/
	/* Adds a new URLScore to the frontier, but only if		*/
	/* the url is not already there.						*/
	/********************************************************/
    	if (!theURLs.containsKey(url)) {
    		try {
    			URLScore theUrl = new URLScore(url, score);
    			/*if (isUnseenHost(theUrl.getHost())) {
    				theUrl = new URLScore(url, score + 1);
    			}*/
        		queueURLs.add(theUrl);
        		theURLs.put(url, 0);
        		totalCount++;
        	} catch (MalformedURLException e) {
        		if (debug) {
        			System.out.println(e.getMessage());
        		}
    		}
    	}
    }
    
    private boolean isUnseenHost(String host) {
    	if (!visitedHosts.contains(host)) {
    		visitedHosts.put(host, 0);
    		return false;
    	}
    	return true;
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
    
    public String peekNextHost() {
    	return queueURLs.peek().getHost();
    }

    public boolean isEmpty() {
        return (queueURLs.isEmpty());
    }

    public int totalCount() {
        return totalCount;
    }

}
