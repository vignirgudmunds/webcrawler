import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;

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

    public Elements getLinks() throws IOException {
	/********************************************************/
	/* GAP!												*/
	/* Get the links from the last document retrieved	*/
	/* We are only interested in <a href> links that	*/
	/* are html pages.									*/
	/********************************************************/
    	Elements hrefs = currentDoc.select("a[href]");
    	Elements links = new Elements();
    	for (Element link : hrefs) {
    		if (link.attr("abs:href").contains("http")) {
    			links.add(link);
    		}
    	}
    	
    	return links;
    }

    public String getBody() { //throws IOException {
	/********************************************************/
	/* GAP!												*/
	/* Get the text of the body from the last document 	*/
	/* retrieved										*/
	/********************************************************/
    	return currentDoc.text();
    }
}
