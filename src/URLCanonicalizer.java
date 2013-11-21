/**
 A class to convert an url to canonical (normalized) form.
 */

public class URLCanonicalizer {

    public String getCanonicalURL(String url) {
    	
    	if (url == null) {
    		return null;
    	}
    	
    	if (url.matches(".*:80(/.*)*")) {
    		url = url.replace(":80", "");
    	}
    	
    	url = canonPrefix(url);

    	return canonicalize(url);
    }
    
    private String canonicalize(String url) {
		
    	if (url == null) {
    		return null;
    	}
    	
		String coreURL = url.substring("http://".length());
		String[] wordList = coreURL.split("/");
		
		wordList[0] = wordList[0].toLowerCase();

		for (int i=0; i<wordList.length-1; i++) {

			wordList[i] = canonWord(wordList[i]); // not implemented yet
		}
		
		int last = wordList.length-1; 
		wordList[last] = lastWord(wordList[last]);
		
		return assembleURL(wordList);
	}

    private String lastWord(String word) {
    	if (word.equalsIgnoreCase("index.htm") || word.equalsIgnoreCase("index.html")) {
    		return null;		// Requires removal of last word from URL
    	}
    	
    	
    	if (word.contains("#")){
			if (word.length() == 1) {
				return null;	// Requires removal of last word from URL
			}
			else {
				word = word.substring(0, word.indexOf("#"));
			}
		}
    	
		return canonWord(word);
	}

    // Not implemented yet
	private String canonWord(String word) {
		return word;
	}

	private String assembleURL(String[] wordList) {
		String assembledURL = "http://";
		for (int i=0; i<wordList.length; i++) {
			if (wordList[i] != null) {
				assembledURL += wordList[i] + "/";
			}
		}
		return assembledURL;
	}

	// Canonalizing the prefix of the URL
	private String canonPrefix(String url) {
    	
    	String preURL = url;
    	
    	if (url.matches("http://www.*")) {
    		preURL = preURL.replaceFirst("www.", "");
    	}
    	
    	else if(url.matches("www.*")) {
    		preURL = preURL.replaceFirst("www.", "http://");
    	}
    	
    	else if(url.matches("http.*")) {
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
	
}
