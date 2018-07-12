package org.poker.stock;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Optional;

public class GoogleImagesLogoURLRetriever {
    public Optional<String> retrieve(String companyName) {
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";

        try {
            String url = formatUrl(companyName);
            Document doc = Jsoup.connect(url).userAgent(userAgent).referrer("https://www.google.com/").get();

            Element element = doc.selectFirst("div.rg_meta");
            JSONObject jsonObject = new JSONObject(element.childNode(0).toString());
            String resultUrl = ((String) jsonObject.get("ou"));

            return Optional.of(resultUrl);

        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private String formatUrl(String companyName) throws UnsupportedEncodingException {
        // unescape to handle edge case where name for $spy comes back as "SPDR S&amp;P 500 ETF"
        companyName = Parser.unescapeEntities(companyName, true);

        companyName = URLEncoder.encode(companyName, "UTF-8") + "+logo";
        return String.format("https://www.google.com/search?site=imghp&tbm=isch&source=hp&q=%s&gws_rd=cr", companyName);
    }
}
