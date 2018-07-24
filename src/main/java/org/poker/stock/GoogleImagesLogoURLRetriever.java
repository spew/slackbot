package org.poker.stock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;

public class GoogleImagesLogoURLRetriever implements LogoURLRetriever {
    private static final Logger logger = LogManager.getLogger(GoogleImagesLogoURLRetriever.class);
    private static final double MIN_RATIO = 1.59d;
    private static final int MAX_IMAGES = 10;

    @Override
    public Optional<String> retrieve(String companyName) {
        String userAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.116 Safari/537.36";

        try {
            String url = formatUrl(companyName);
            Document doc = Jsoup.connect(url).userAgent(userAgent).referrer("https://www.google.com/").get();
            Elements elements = doc.select("div.rg_meta");
            return parseImageResults(elements);
        } catch (IOException e) {
            logger.warn("error trying to retrieve logo", e);
            return Optional.empty();
        }
    }


    private String formatUrl(String companyName) throws UnsupportedEncodingException {
        // unescape to handle edge case where name for $spy comes back as "SPDR S&amp;P 500 ETF"
        companyName = Parser.unescapeEntities(companyName, true);

        companyName = URLEncoder.encode(companyName, "UTF-8") + "+logo";
        return String.format("https://www.google.com/search?site=imghp&tbm=isch&source=hp&q=%s&gws_rd=cr", companyName);
    }

    private Optional<String> parseImageResults(Elements elements) {
        String resultUrl;
        BufferedImage image;

        int max = Math.min(MAX_IMAGES, elements.size());
        for (int i = 0; i < max; i++) {
            Element element = elements.get(i);
            if (element.childNodeSize() > 0) {
                JSONObject jsonObject = new JSONObject(element.childNode(0).toString());
                resultUrl = ((String) jsonObject.get("ou"));
                try {
                    logger.debug("Trying " + resultUrl);
                    image = ImageIO.read(new URL(resultUrl));
                    if (image != null && ratio(image) > MIN_RATIO) {
                        return Optional.of(resultUrl);
                    }
                } catch (IOException ie) {
                    logger.warn("Exception while processing image. Url: " + resultUrl, ie);
                }
            }
        }
        return Optional.empty();
    }

    private double ratio(BufferedImage image) {
        return (double) image.getWidth() / (double) image.getHeight();
    }
}
