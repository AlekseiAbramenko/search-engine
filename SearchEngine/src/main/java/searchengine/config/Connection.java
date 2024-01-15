package searchengine.config;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class Connection {
    public static Document getConnection(String link) throws IOException {
        return Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .timeout(10000)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .followRedirects(false)
                .get();
    }
}
