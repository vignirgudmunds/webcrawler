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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
    private final int SEARCH_LIMIT = 10000;  	// Absolute max # of pages crawled. Respect this, be polite!
    private final int MILLISECOND_WAIT = 300; // Wait between url requests, be polite!
    private final boolean DEBUG = false;     // To control debugging output
    private final String userAgent = "RuBot"; 	// Reykjavik University bot

    Frontier frontier;      // The frontier, the list of pages yet to be crawled (visited)
    Hashtable<String, Integer> visitedURLs;    // The list of visited URLs
    URLCanonicalizer canonicalizer; // Used to transform URLs to canonical form
    int maxPages;           // max number of pages to crawl, may be supplied by the user
    
    String topic;           // the topic we are interested in
    String topicEN = "";			// the topic we are interested in
    String queryString = "";    // the query string we are interested in
    String[] queryWords;   // individual words of the query string
    String queryStringEN = "";    // the query string in english we are interested in
    String[] queryWordsEN;   // individual english words of the query string
    boolean usingTopicEN = false;
    boolean usingQueryEN = false;

    RobotTxtParser robotParser; // A robots.txt parser
    HTMLParser htmlParser;  	// A HTMLParser
    int totalRelevant=0;    	// Total number of pages containing our query string
    
    static final Map<String, String> IStoEN;
	static {
	    IStoEN = new HashMap<String, String>();
	    String[][] pairs = {
    		{"á", "a"},
	        {"ð", "d"},
	        {"é", "e"},
	        {"í", "i"},
	        {"ó", "o"},
	        {"ú", "u"},
	        {"ý", "y"},
	        {"þ", "th"},
	        {"æ", "ae"},
	        {"ö", "o"}
	    };
	    for (String[] pair : pairs) {
	        IStoEN.put(pair[0], pair[1]);
	    }
	}

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
        
        topicToEN();
        queryWordsToEN(); // Map possible IS characters to US

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
        
        if(usingTopicEN) {
        	System.out.println("Topic: " + topic + " has been converted to: " + topicEN);
        }
        else {
        	System.out.println("Topic: " + topic);
        }
        
        if(usingQueryEN) {
        	System.out.println("Query string: " + queryString + " has been converted to: " + queryStringEN);
        }
        else {
        	System.out.println("Query string: " + queryString);
        }
        System.out.println("Maximum number of pages to visit: " + maxPages);
        System.out.println("--------------------------------------------------------");
   }   

    private void topicToEN() {
    	for (int i=0; i<topic.length(); i++) {
			String strIS = String.valueOf(topic.charAt(i));
			if(IStoEN.containsKey(strIS)) {
				String strEN = IStoEN.get(strIS);
				topicEN = topic.replace(strIS, strEN);
				usingTopicEN = true;
			}
		}	
    	
    	if (!usingTopicEN) {
    		topicEN = topic;
    	}
	}

	private void queryWordsToEN() {
        queryWordsEN = queryWords;  					// Assume space between query words
    	for (int i=0; i<queryWords.length; i++) {
    		for (int j=0; j<queryWords[i].length(); j++) {
    			String strIS = String.valueOf(queryWords[i].charAt(j));
    			if(IStoEN.containsKey(strIS)) {
    				String strEN = IStoEN.get(strIS);
    				queryWordsEN[i] = queryWords[i].replace(strIS, strEN);
    				usingQueryEN = true;
    			}
    		}
    		queryStringEN += queryWordsEN[i] + " ";
    	}
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
    private void addLinks(Elements links, int fromRelevant) {
	/********************************************************/
	/* GAP!													*/
	/* Make sure that you add canonicalized versions		*/
	/* of the links to the frontier							*/
	/* You also need to score the links						*/
	/********************************************************/
    	for (Element link : links) {
    		
    		System.out.println(link.attr("abs:href"));
    		String url = canonicalizer.getCanonicalURL(link.attr("abs:href"));
    		System.out.println(url);
    		System.out.println("--------------------------------------------");
    		
    		Double score = rateURL(url, fromRelevant);
    		synchronized (frontier) {
    			frontier.add(url, score);
			}
    		
    	}
    }

    /**
     * 
     * @param url
     * @param rel
     * @return
     */
    private Double rateURL(String url, double rel) {
    	Double score = rel;	// Will be 2 if it comes from a relevant site, otherwise 0
    	
    	// Increment score for every query word found in the url
    	for (int i=0; i<queryWordsEN.length; i++) {            
            if (url.toLowerCase().contains(queryWordsEN[i])) {        
                score += 1.0;
            }
    	}
    	if (url.toLowerCase().contains(topicEN)) {
			score += queryWordsEN.length;
		}
    	score += rel;
    	
		return score;

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
     * Spawns a thread that processes the given url, including connecting to it,
     * extracting its links and deciding if it's relevant.
     * @param url	The URL to process
     * @return		The thread that is processing it.
     */
    public Thread processUrl(final String url) {
    	// Define the thread
    	Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// Connect to the URL
				try {
		    		htmlParser.connect(url, userAgent);
		    		
		    	} catch (IOException e) {
		    		if (DEBUG) {
		    			System.out.println(e.getMessage() + ": " + url);
		    			return;
		    		}
				}
				// Process it and decide if it's relevant
		    	int relevance = 0;
				
				if (isRelevantUrl()) {
					totalRelevant++;
		    		System.out.println("Query found in page: " + url);
		    		
		    		// If the query is found in the page, we give it two 'relevance points'
		    		// which are used to score the links found on the page
		    		relevance = 2;		
		    	}
				addLinks(getLinks(url), relevance);
			}
    	});
    	thread.start();
    	return thread;
    }

    /**
     * Main crawling method.
     */
    public void crawl()
    {
    	List<Thread> threads = new ArrayList<Thread>();
    	long startTime = System.currentTimeMillis();
        for (int i = 0; i < maxPages; i++) {        // Visit maxPages
    	    /****************************************************/
    	    /* GAP!												*/
    	    /* Retrive the next url from the frontier			*/
    	    /* parse and process it given that					*/
    	    /* 	you are allowed to do so (robot.txt)			*/
    	    /* 	and that the url has not been visitied before 	*/
    	    /****************************************************/
        	URLScore currentUrl = null;
        	
        	// Retrieve the next URL from the frontier which may be temporarily empty
        	// if other threads have taken all the available URLS
			while (currentUrl == null) {
				synchronized (frontier) {	 
					if (!frontier.isEmpty()) {
						currentUrl = frontier.removeNext();
					} 
				}
        	}
			// Check if we are allowed to parse and process the URL
			if (robotParser.isUrlAllowed(currentUrl.getURLString())) { 
				threads.add(processUrl(currentUrl.getURLString()));	// Designate the actual work to a thread
				wait(MILLISECOND_WAIT);									// Be polite
			} else {
				i--;	// Don't count this as a crawled page if we weren't allowed to crawl it
			}
        }
        
        // Clean up any remaining for any remaining threads
        for (Thread t :  threads) {
        	try {
        		t.join();
        	} catch (InterruptedException e) {
        		if (DEBUG) e.printStackTrace();
			}
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

