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
    private void addLinks(Elements links, boolean fromRelevant) {
	/********************************************************/
	/* GAP!													*/
	/* Make sure that you add canonicalized versions		*/
	/* of the links to the frontier							*/
	/* You also need to score the links						*/
	/********************************************************/
    	for (Element link : links) {
    		
    		String url = canonicalizer.getCanonicalURL(link.attr("abs:href"));
    		
    		Double score = rateURL(url, fromRelevant);
    		synchronized (frontier) {
    			frontier.add(url, score);
			}
    		
    	}
    }

    private Double rateURL(String url, boolean rel) {
    	
    	Double score = 0.0;
    	
    	if (url.toLowerCase().contains(topicEN)) {
			score += 1.0;
		}
    	
    	for (int i=0; i<queryWordsEN.length; i++) {            
            if (url.toLowerCase().contains(queryWordsEN[i])) {        
                score += 1.0;
                break;
            }
    	}	
    	
    	if (rel) {
			score += 1.0;
		}
		
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
    	//System.out.println("OLD URL" + url);
    	//url = canonicalizer.getCanonicalURL(url);
    	//System.out.println("NEW URL" + url);
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
        	Thread thread = new Thread(new Runnable() {
        		private void wait(int milliseconds)	// Halt execution for the specified number of milliseconds
        	    {
        	        try {
        	            Thread.currentThread().sleep(milliseconds);
        	        }
        	        catch (InterruptedException e) {
        	                e.printStackTrace();
        	        }
        	    }
				@Override
				public void run() {
					URLScore currentUrl = null;
					while (currentUrl == null) {
						boolean wait = true;
	    				synchronized (frontier) {
	    					if (!frontier.isEmpty()) {
	    						currentUrl = frontier.removeNext();
	    						wait = false;
	    					}
	    				}
	    				if (wait) {
	    					wait(1);
	    				}
					}
					if (robotParser.isUrlAllowed(currentUrl.getURLString())) { // robotparser lock?
        				processUrl(currentUrl.getURLString());
					}
				}
        	});
        	thread.start();
        	threads.add(thread);
        	wait(MILLISECOND_WAIT);
        	
        	//if (frontier.isEmpty()) break;	// TODO want this?
        }
        for (Thread t :  threads) {
        	try {
        		t.join();
        	} catch (InterruptedException e) {
        		e.printStackTrace();
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

