/**
 A class to convert an url to canonical (normalized) form.
 */

public class URLCanonicalizer {
	
	String prefix = "";
	
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
    
    public void testCanonical() {
    	
    	URLCanonicalizer canonicalizerTest = new URLCanonicalizer();
    	
    	 /*http://www.mbl.is/sport/golf/#Mest_lesi__dag
    	http://www.mbl.is/sport/golf/2013/11/06/vissa_hja_birgi_leifi/#Mest_lesi__dag
    		Query found in page: http://www.mbl.is/sport/golf/2013/11/05/frestad_hja_birgi_leifi_sem_a_eina_holu_eftir/#
    		Query found in page: http://www.mbl.is/sport/golf/2013/11/05/frestad_hja_birgi_leifi_sem_a_eina_holu_eftir/#Mest_lesi__dag*/
    	
    	System.out.println("https://itunes.apple.com/is/app/morgunbla-i/id512506440 -> " + canonicalizerTest.getCanonicalURL("https://itunes.apple.com/is/app/morgunbla-i/id512506440"));
    	
    	/*System.out.println("http://mbl.is -> " + canonicalizerTest.getCanonicalURL("http://mbl.is"));
    	System.out.println("www.mbl.is -> " + canonicalizerTest.getCanonicalURL("www.mbl.is"));
    	System.out.println("http://www.mbl.is -> " + canonicalizerTest.getCanonicalURL("http://www.mbl.is"));
    	System.out.println("ftp:168.192.28.1 -> " + canonicalizerTest.getCanonicalURL("ftp:168.192.28.1"));
    	System.out.println("mbl.is -> " + canonicalizerTest.getCanonicalURL("mbl.is")); 
    	System.out.println("MBL.is -> " + canonicalizerTest.getCanonicalURL("MBL.is"));
    	System.out.println("http://mbl.is:80/frettir/enski/ -> " + canonicalizerTest.getCanonicalURL("http://mbl.is:80/frettir/enski/"));
    	System.out.println("http://foodhunter.is/cs2#some -> " + canonicalizerTest.getCanonicalURL("http://foodhunter.is/cs2#some"));
    	System.out.println("http://mbl.is -> " + canonicalizerTest.getCanonicalURL("http://mbl.is"));
    	System.out.println("http://mbl.is/People -> " + canonicalizerTest.getCanonicalURL("http://mbl.is/People"));
    	System.out.println("http://mbl.is/faq.html#3 -> " + canonicalizerTest.getCanonicalURL("http://mbl.is/faq.html#3"));
    	System.out.println("http://mbl.is/# -> " + canonicalizerTest.getCanonicalURL("http://mbl.is/#"));
    	System.out.println("http://mbl.is/index.html -> " + canonicalizerTest.getCanonicalURL("http://mbl.is/index.html"));*/
    }
    
    private String canonicalize(String url) {
		
    	if (url == null) {
    		return null;
    	}
		
		String coreURL = "";
    	
		if(url.matches("http://.*")) {
			prefix = "http://";
		}
		else if (url.matches("https://.*")) {
			prefix = "https://";
		}
		
		coreURL = url.substring(prefix.length());
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
			if (word.startsWith("#")) {
				return null;	// Requires removal of last word from URL
			}
			else {
				word = word.substring(0, word.indexOf("#"));
			}
		}
    	
    	if (word.contains("?") && !word.startsWith("?")){
    		word = word.substring(0, word.indexOf("?"));
		}
    	
		return canonWord(word);
	}

    // Not implemented yet
	private String canonWord(String word) {
		return word;
	}

	private String assembleURL(String[] wordList) {
		String assembledURL = prefix;
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
    	
    	if (url.matches("http://www.*") || url.matches("https://www.*")) {
    		preURL = preURL.replaceFirst("www.", "");
    	}
    	
    	else if(url.matches("www.*")) {
    		preURL = preURL.replaceFirst("www.", "http://");
    	}
    	
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
	
}
