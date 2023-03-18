import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XMLHandler extends DefaultHandler {
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        if (qName.equals("voter")) {
            String name = attributes.getValue("name");
            Date birthDay;
            try {
                birthDay = dateFormat.parse(attributes.getValue("birthDay"));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            Voter voter = new Voter(name, birthDay);
            Integer count = Loader.getVoterCounts().get(voter);
            Loader.getVoterCounts().put(voter, count == null ? 1 : count + 1);
        }
    }
}