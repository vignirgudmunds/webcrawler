// A simple Web Crawler written in Java
// Partly based on http://cs.nyu.edu/courses/fall02/G22.3033-008/proj1.html
// Our crawler is a sequential, topical crawler.
// It uses a priority queue for unvisited links.
// The links are scored with 1.0 if the topic is part of the anchor text for a link, else 0.0.
// This crawler lists HTML pages that contain the query words in their <body> section.
// Usage: From command line 
//     java WebCrawler <URL> <TOPIC> <QUERY WORDS> <N>
//  where   URL is the url (seed) to start the crawl,
//          TOPIC is the topic we are interested in (used to quide the crawler to relevant links)
//          QUERY WORDS is the phrase we are interested in
//          N (optional) is the maximum number of pages to crawl
//  Hrafn Loftsson, Reykjavik University, Fall 2013
//import java.text.*;

import java.util.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    private final int SEARCH_LIMIT = 500;  	// Absolute max # of pages crawled. Respect this, be polite!
    private final int MILLISECOND_WAIT = 300; // Wait between url requests, be polite!
    private final boolean DEBUG = false;     // To control debugging output
    private final String userAgent = "RuBot"; 	// Reykjavik University bot

    Frontier frontier;      // The frontier, the list of pages yet to be crawled (visited)
    Hashtable<String, Integer> visitedURLs;    // The list of visited URLs
    URLCanonicalizer canonicalizer; // Used to transform URLs to canonical form
    int maxPages;           // max number of pages to crawl, may be supplied by the user
    String topic;           // the topic we are interested in
    String queryString;    // the query string we are interested in
    String[] queryWords;   // individual words of the query string

    RobotTxtParser robotParser; // A robots.txt parser
    HTMLParser htmlParser;  	// A HTMLParser
    int totalRelevant=0;    	// Total number of pages containing our query string

    public void initialize(String[] argv) {
        String url;
        robotParser = new RobotTxtParser(userAgent, DEBUG);
        htmlParser = new HTMLParser();
        canonicalizer= new URLCanonicalizer();

        visitedURLs = new Hashtable<String, Integer>();
        frontier = new Frontier(DEBUG);

        url = argv[0];                  							// The seed URL supplied by the user
        topic = argv[1].toLowerCase();  							// The topic
        queryString = argv[2].toLowerCase().replaceAll("\\s+", " ");// The query words supplied by the user
        queryWords = queryString.split("\\s");    					// Assume space between query words
        String canonicalUrl = canonicalizer.getCanonicalURL(url);	// Canonicalize the URL
        frontier.add(canonicalUrl, 0.0);                            // The seed has score 0.0

        maxPages = SEARCH_LIMIT;
        if (argv.length > 3) { // Does the user override the search limit?
            int iPages = Integer.parseInt(argv[3]);
            if (iPages < maxPages)
                maxPages = iPages;
        }

        System.out.println("--------------------------------------------------------");
        System.out.println("Starting crawl, seed: " + canonicalUrl);
        System.out.println("Topic: " + topic);
        System.out.println("Query string: " + queryString);
        System.out.println("Maximum number of pages to visit: " + maxPages);
        System.out.println("--------------------------------------------------------");
   }   

    // Retrieve the links (href) from the given url
    private Elements getLinks(String url) {
        Elements links;
        try {
            links = htmlParser.getLinks();      // Retrieve the <a href> links
        }
        catch (IOException e) {
            System.out.println("Could not get links from " + url);
            links = new Elements();             // Empty elements
        }
        return links;
    }

    // Adds the retrieved links to the frontier
    private void addLinks(Elements links, boolean fromRelevant) {
	/********************************************************/
	/* GAP!													*/
	/* Make sure that you add canonicalized versions		*/
	/* of the links to the frontier							*/
	/* You also need to score the links						*/
	/********************************************************/
    	for (Element link : links) {
    		String url = canonicalizer.getCanonicalURL(link.attr("abs:href"));
    		Double score = 0.0;
    		
    		if (url.toLowerCase().contains(topic)) {
    			score += 1.0;
    		}
    		if (url.toLowerCase().contains(queryString)) {
    			score += 1.0;
    		}
    		if (fromRelevant) {
    			score += 1.0;
    		}
    		frontier.add(url, score);
    	}
    }

    /**
     * Does a case-insensitive comparison in searching for 
     * the phrase query in the given text.
     * Does not implement stemming.
     * 
     * @param text	The text to search for the query phrase in
     * @return true if our phrase query is found in the given text, otherwise false.
     */
    private boolean isRelevantText(String text) {
    	return text.toLowerCase().contains(queryString) ? true : false; 
    }
    
    /**
     * Retrieves the text in the body of the last retrieved document.
     * Decides whether or not the url is relevant, based on whether
     * or not it contains the phrase query. 
     * 
     * @return 		true if the body of the page corresponding to the url is relevant.
     */
    private boolean isRelevantUrl() {
    	return isRelevantText(htmlParser.getBody());
    }

    /**
     * Attempts to connect to the given url and retrive the html document.
     * 
     * @param url
     */
    private boolean processUrl(String url)
    {
	/********************************************************/
	/* GAP!													*/
	/* Process the given url, which means at least:			*/
	/* 1) Connect to it using the HTML parser				*/
	/* 2) Print an appropriate message if it is relevant	*/
	/* 3) Extract links from the url and add to frontier	*/
	/********************************************************/	
    	try {
    		htmlParser.connect(url, userAgent);
    		
    	} catch (IOException e) {
    		if (DEBUG) {
    			System.out.println(e.getMessage() + ": " + url);
    			return false;
    		}
		}
    	boolean relevant = false;
		
		if (isRelevantUrl()) {
			totalRelevant++;
			relevant = true;
    		System.out.println("Query found in page: " + url);
    	}
		
		addLinks(getLinks(url), relevant);
		return true;
    }

    // This method does the crawling.
    public void crawl()
    {
    	long startTime = System.currentTimeMillis();
    	//long visitTime = System.currentTimeMillis();
        for (int i = 0; i < maxPages; i++) {        // Visit maxPages
    	    /****************************************************/
    	    /* GAP!												*/
    	    /* Retrive the next url from the frontier			*/
    	    /* parse and process it given that					*/
    	    /* 	you are allowed to do so (robot.txt)			*/
    	    /* 	and that the url has not been visitied before 	*/
    	    /****************************************************/
        	// Retrive the next url from the frontier and process the url
        	long visitTime = System.currentTimeMillis();
        	URLScore currentUrl = frontier.removeNext();
        	boolean shouldWait = false;
        	if (robotParser.isUrlAllowed(currentUrl.getURLString())) {
        		
        		shouldWait = processUrl(currentUrl.getURLString());
        	}
        	int msToWait = MILLISECOND_WAIT - (int)(System.currentTimeMillis() - visitTime);
        	if (msToWait > 10 && currentUrl.getHost().equals(frontier.peekNextHost())) {
        		//if (currentUrl.getHost().equals(frontier.peekNextHost())) {
        			wait(msToWait);          // Be polite, wait x milliseconds before fetching the next page
        	} /*else {
        			wait(MILLISECOND_WAIT/2);        // Be polite, wait x/2 milliseconds before fetching the next page if it's a different host name
        		}*/
        	//}

            if (frontier.isEmpty()) break;   // Break the loop if frontier is empty
        }
        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("--------------------------------------------------------");
        System.out.println("Search complete, " + maxPages + " pages crawled");
        System.out.println("Search query " + queryString + " found in " + totalRelevant + " pages");
        System.out.println("Total distinctive urls found: " + frontier.totalCount());
        System.out.println("Seconds elapsed during crawl: " + elapsedTime);
        System.out.println("--------------------------------------------------------");
    }
    
    private void wait(int milliseconds)	// Halt execution for the specified number of milliseconds
    {
        try {
            Thread.currentThread().sleep(milliseconds);
        }
        catch (InterruptedException e) {
                e.printStackTrace();
        }
    }

    public static void main(String[] argv)
    {
    	/*URLCanonicalizer canonicalizerTest = new URLCanonicalizer();
    	canonicalizerTest.testCanonical();*/
    	
        WebCrawler wc = new WebCrawler();
        wc.initialize(argv);
        wc.crawl();
    }
}

