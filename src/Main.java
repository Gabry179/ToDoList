import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.xml.sax.InputSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Main {
    public static void main(String[] args) {
        XPath configPath = XPathFactory.newInstance().newXPath();
        String pathExpression = "/settings/bot_token";
        InputSource inputSource = new InputSource("src\\config.xml");
        try{
            String token = (String)configPath.compile(pathExpression).evaluate(inputSource, XPathConstants.STRING);
            TelegramBotsLongPollingApplication botApplication = new TelegramBotsLongPollingApplication();
            botApplication.registerBot(token, new TelegramBot(token));
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}