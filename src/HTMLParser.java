import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.io.IOException;

/*
 * This parser uses the jsoup Java HTML Parser -- see http://jsoup.org/
 */
public class HTMLParser {
    private Document currentDoc;   // The last document retrieved

    public void connect(String url, String agent) throws IOException {
        try {
            currentDoc = Jsoup.connect(url).userAgent(agent).get();
        }
        catch (java.nio.charset.IllegalCharsetNameException e)  {
            throw new IOException(e.toString());
        } catch (IllegalArgumentException ex) {
        	throw new IOException("Error connecting to " + url + ": " + ex.getMessage());
        }
    }

    /**
     * Retrieves all links from the last document retrieved.
     * Filters links that are non-http/https protocols. 
     * @return a collection of Link Elements
     * @throws IOException
     */
    public Elements getLinks() throws IOException {
    	Elements hrefs = currentDoc.select("a[href]");
    	Elements links = new Elements();
    	for (Element link : hrefs) {
    		if (link.attr("abs:href").contains("http")) {
    			links.add(link);
    		}
    	}    	
    	return links;
    }

    /**
     * @return the body of the last document retrieved
     */
    public String getBody() {
    	return currentDoc.text();
    }
}
