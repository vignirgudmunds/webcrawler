/**
 A class to convert an url to canonical (normalized) form.
 */

public class URLCanonicalizer {
	
	String prefix = "";
	
	/**
	 * Processes the url passed as a parameter and returns a canonicalized
	 * version of the url
	 * @param url The url to be canonicalized
	 * @return Canonicalized url
	 */
    public String getCanonicalURL(String url) {
    	
    	// Check if the passed url is null
    	if (url == null) {
    		return null;
    	}
    	
    	// Removes port 80, if necessary
    	if (url.matches(".*:80(/.*)*")) {
    		url = url.replace(":80", "");
    	}
    	
    	// Canonicalize the prefix of the url
    	url = canonPrefix(url);

    	return canonicalize(url);
    }
    
    /**
     * Processes the prefix of the url passed as a parameter and returns a 
     * url with a canonicalized prefix
     * @param The url which is being processed
     * @return The url with a canonicalized prefix
     */
 	private String canonPrefix(String url) {
     	
     	String preURL = url;
     	
     	/* Check if the url matches certain patterns of prefixes and 
     	canonicalize correspondingly */
     	
     	// Remove www, if necessary
     	if (url.matches("http://www.*") || url.matches("https://www.*")) {
     		preURL = preURL.replaceFirst("www.", "");
     	}
     	
     	// Replace www with http(s)://, if missing
     	else if(url.matches("www.*")) {
     		preURL = preURL.replaceFirst("www.", "http://");
     	}
     	
     	// Valid format
     	else if(url.matches("http.*") || url.matches("https.*")) {
     		return preURL;
     	}
     	
     	// Not a http URL
     	else if(url.matches("([a-z]|[A-Z])*:.*")) {
     		return null;
     	}
     	
     	// URL has no prefix
     	else {
     		preURL = "http://" + preURL;
     	}
     	
     	return preURL;
     }
    
 	/**
 	 * Canonicalizes known patterns and returns a canonicalized url
 	 * @param url The url which is being processed
 	 * @return The canonicalized url
 	 */
    private String canonicalize(String url) {
		
    	// Url is not http based on result from prefix canonicalization
    	if (url == null) {
    		return null;
    	}
		
		String coreURL = "";
    	
		// Set the prefix type of the url
		if(url.matches("http://.*")) {
			prefix = "http://";
		}
		else if (url.matches("https://.*")) {
			prefix = "https://";
		}
		
		// Sanitize the prefix before 
		coreURL = url.substring(prefix.length());
		
		// Split the core of the url into words
		String[] wordList = coreURL.split("/");
		wordList[0] = wordList[0].toLowerCase();
		
		// Certain patterns exclusive for the last word canonicalized 
		int last = wordList.length-1; 
		wordList[last] = lastWord(wordList[last]);
		
		// Assemble the words back into a single url and returns
		return assembleURL(wordList);
	}

    
    /**
     * Function specifically called on the last word of a url in order to
     * canonicalize certain patterns exclusive to the last word
     * @param word The last word of the url
     * @return Last word canonicalized
     */
    private String lastWord(String word) {
    	// Remove index suffix
    	if (word.equalsIgnoreCase("index.htm") || word.equalsIgnoreCase("index.html")) {
    		return null;		// Requires removal of last word from URL
    	}
    	
    	// Remove page position related parameter
    	if (word.contains("#")){
			if (word.startsWith("#")) {
				return null;	// Requires removal of last word from URL
			}
			else {
				word = word.substring(0, word.indexOf("#"));
			}
		}
    	
    	// Remove db parameters
    	if (word.contains("?") && !word.startsWith("?")){
    		word = word.substring(0, word.indexOf("?"));
		}
    	
		return word;
	}
    
    /**
     * Assembles the words of a url back to a single url
     * @param wordList List of the url words
     * @return A single url string
     */
	private String assembleURL(String[] wordList) {
		String assembledURL = prefix;
		for (int i=0; i<wordList.length; i++) {
			if (wordList[i] != null) {
				assembledURL += wordList[i] + "/";
			}
		}
		return assembledURL;
	}
	
}
