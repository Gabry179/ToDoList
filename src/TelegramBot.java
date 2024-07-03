import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient;

    //HashMap per memorizzare i valori di titolo, descrizione e deadline di una task
    private HashMap<String,String> statusMap = new HashMap<>();
    private HashMap<String,String> titleMap = new HashMap<>();
    private HashMap<String,String> descriptionMap = new HashMap<>();
    private HashMap<String,String> deadlineMap = new HashMap<>();

    //Bottoni
    private List<KeyboardRow> keyboard = new ArrayList<>();
    private ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboard);
    private ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(false);
    private KeyboardRow row1 = new KeyboardRow();
    private KeyboardRow row2 = new KeyboardRow();
    private KeyboardButton buttonList = new KeyboardButton("☰ Lista");
    private KeyboardButton button1 = new KeyboardButton("➕ Aggiungi");
    private KeyboardButton button2 = new KeyboardButton("✏️ Modifica");
    private KeyboardButton button3 = new KeyboardButton("🗑️ Rimuovi");

    //Connessione al db
    private Connection con = null;
    private Statement stmt = null;
    private ResultSet rs = null;

    //Usata per individuare quale task cancellare o modificare
    private int toDelete = 0;

    public TelegramBot(String clientToken) {
        this.telegramClient = new OkHttpTelegramClient(clientToken);
    }

    @Override
    public void consume(Update update) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (update.hasMessage() && update.getMessage().hasText()) {
                    String chatId = update.getMessage().getChatId().toString();
                    String text = update.getMessage().getText();
                    SendMessage sendMessage;
                    String state = "default";
                    if(statusMap.containsKey(chatId)) {
                        state = statusMap.get(chatId);
                    } else{
                        statusMap.put(chatId, "default");
                    }
                    switch(state) {
                        case "getTitle":
                            if(text.equals("/annulla")){
                                statusMap.put(chatId, "default");
                                sendMessage = new SendMessage(chatId, "❌ Annullato!");
                                replyKeyboardMarkup.setKeyboard(keyboard);
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                                replyKeyboardRemove.setRemoveKeyboard(false);

                                statusMap.remove(chatId);
                                titleMap.remove(chatId);
                                descriptionMap.remove(chatId);
                                deadlineMap.remove(chatId);

                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            if(toDelete != 0){
                                int n = 0;
                                con = MySql.getConnection();
                                try {
                                    stmt = con.createStatement();
                                    rs = stmt.executeQuery("SELECT * FROM `to-do_list`.task WHERE ID_Utente="+update.getMessage().getChat().getId());
                                    while(rs.next() && n<=toDelete) {
                                        n+=1;
                                        if(toDelete == n) {
                                            toDelete = rs.getInt(1);
                                            break;
                                        }
                                    }

                                    stmt.executeUpdate("DELETE FROM `to-do_list`.task WHERE ID_Task="+toDelete);
                                    toDelete = 0;
                                    rs.close();
                                    con.close();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            titleMap.put(chatId, text);
                            sendMessage = new SendMessage(chatId, "📝 Inserisci la descrizione!\n\n/annulla");
                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            statusMap.put(chatId,"getDescription");
                            break;
                        case "getDescription":
                            if(text.equals("/annulla")){
                                statusMap.put(chatId, "default");
                                sendMessage = new SendMessage(chatId, "❌ Annullato!");
                                replyKeyboardMarkup.setKeyboard(keyboard);
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                                replyKeyboardRemove.setRemoveKeyboard(false);

                                statusMap.remove(chatId);
                                titleMap.remove(chatId);
                                descriptionMap.remove(chatId);
                                deadlineMap.remove(chatId);

                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                            descriptionMap.put(chatId, text);
                            sendMessage = new SendMessage(chatId, "🕒 Inserisci la scadenza! (aaaa-mm-dd)\n\n/annulla");
                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            statusMap.put(chatId,"getDeadline");
                            break;

                        case "getDeadline":
                            if(text.equals("/annulla")){
                                statusMap.put(chatId, "default");
                                sendMessage = new SendMessage(chatId, "❌Annullato!");
                                replyKeyboardMarkup.setKeyboard(keyboard);
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                                replyKeyboardRemove.setRemoveKeyboard(false);

                                statusMap.remove(chatId);
                                titleMap.remove(chatId);
                                descriptionMap.remove(chatId);
                                deadlineMap.remove(chatId);

                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }

                            deadlineMap.put(chatId, text);
                            String title = titleMap.get(chatId);
                            String description = descriptionMap.get(chatId);
                            sendMessage = new SendMessage(chatId, "🗓️ Stai per inserire questa task:\n\nTitolo: " + title + "\nDescrizione: " + description + "\nScadenza: " + text + "\n\nVuoi confermare?");

                            //Bottoni per la conferma
                            List<KeyboardRow> keyboardConfirmation = new ArrayList<>();
                            ReplyKeyboardMarkup replyKeyboardMarkupConfirmation = new ReplyKeyboardMarkup(keyboardConfirmation);
                            KeyboardRow rowConfirmation = new KeyboardRow();
                            KeyboardButton buttonConfirmation = new KeyboardButton("✅ Conferma");
                            KeyboardButton buttonCancel = new KeyboardButton("❌ Annulla");
                            rowConfirmation.add(buttonConfirmation);
                            rowConfirmation.add(buttonCancel);
                            keyboardConfirmation.add(rowConfirmation);
                            replyKeyboardMarkupConfirmation.setResizeKeyboard(true);
                            replyKeyboardMarkupConfirmation.setKeyboard(keyboardConfirmation);
                            sendMessage.setReplyMarkup(replyKeyboardMarkupConfirmation);

                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            statusMap.put(chatId, "getConfirmation");
                            break;

                        case "getConfirmation":
                            replyKeyboardRemove.setRemoveKeyboard(false);
                            replyKeyboardMarkup.setKeyboard(keyboard);

                            if(text.equals("✅ Conferma")){
                                //query
                                con = MySql.getConnection();
                                try {
                                    stmt = con.createStatement();
                                    stmt.executeUpdate("INSERT INTO `to-do_list`.task(Titolo, Descrizione, Scadenza, ID_Utente) VALUES('" + titleMap.get(chatId) + "','" + descriptionMap.get(chatId) + "','" + deadlineMap.get(chatId) + "'," + update.getMessage().getChat().getId() + ")");
                                    stmt.close();
                                    con.close();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                                sendMessage = new SendMessage(chatId, "✅ Dati inseriti correttamente!");
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);

                                statusMap.remove(chatId);
                                titleMap.remove(chatId);
                                descriptionMap.remove(chatId);
                                deadlineMap.remove(chatId);

                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else if(text.equals("❌ Annulla")){
                                sendMessage = new SendMessage(chatId, "❌ Annullato!");
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);

                                statusMap.remove(chatId);
                                titleMap.remove(chatId);
                                descriptionMap.remove(chatId);
                                deadlineMap.remove(chatId);

                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else{
                                sendMessage = new SendMessage(chatId, "❌ Comando non valido. Riprova!");
                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;

                        case "modify":
                            if(text.equals("🔙 Indietro")){
                                statusMap.put(chatId, "default");
                                sendMessage = new SendMessage(chatId, "🔙 Torno indietro!");
                                replyKeyboardMarkup.setKeyboard(keyboard);
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                            }
                            else {
                                sendMessage = new SendMessage(chatId, "🖊️ Inserisci il titolo.\n\n/annulla");
                                statusMap.put(chatId, "getTitle");
                                toDelete = Integer.parseInt(text);
                                //Le due righe sotto nascondono i pulsanti del bot
                                replyKeyboardRemove.setRemoveKeyboard(true);
                                sendMessage.setReplyMarkup(replyKeyboardRemove);
                            }
                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            break;

                        case "remove":
                            int n = 0;
                            toDelete = Integer.parseInt(text);
                            if(text.equals("🔙 Indietro")){
                                statusMap.put(chatId, "default");
                                sendMessage = new SendMessage(chatId, "🔙 Torno indietro!");
                                replyKeyboardMarkup.setKeyboard(keyboard);
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                            } else {
                                con = MySql.getConnection();
                                try {
                                    stmt = con.createStatement();
                                    rs = stmt.executeQuery("SELECT * FROM `to-do_list`.task WHERE ID_Utente=" + update.getMessage().getChat().getId());
                                    while (rs.next()) {
                                        n += 1;
                                        if (toDelete == n) {
                                            toDelete = rs.getInt(1);
                                            break;
                                        }
                                    }
                                    sendMessage = new SendMessage(chatId, "Stai per eliminare definitivamente questa task:\n\nTitolo: " + rs.getString("Titolo") + "\nDescrizione: " + rs.getString("Descrizione") + "\nScadenza: " + rs.getString("Scadenza") + "\n\nVuoi confermare?");
                                    rs.close();
                                    con.close();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                                statusMap.put(chatId, "removalConfirmation");

                                //Bottoni per la conferma
                                keyboardConfirmation = new ArrayList<>();
                                replyKeyboardMarkupConfirmation = new ReplyKeyboardMarkup(keyboardConfirmation);
                                rowConfirmation = new KeyboardRow();
                                buttonConfirmation = new KeyboardButton("✅ Conferma");
                                buttonCancel = new KeyboardButton("❌ Annulla");
                                rowConfirmation.add(buttonConfirmation);
                                rowConfirmation.add(buttonCancel);
                                keyboardConfirmation.add(rowConfirmation);
                                replyKeyboardMarkupConfirmation.setResizeKeyboard(true);
                                replyKeyboardMarkupConfirmation.setKeyboard(keyboardConfirmation);
                                sendMessage.setReplyMarkup(replyKeyboardMarkupConfirmation);
                            }
                            try {
                                telegramClient.execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "removalConfirmation":
                            if(text.equals("✅ Conferma")){
                                //query
                                con = MySql.getConnection();
                                try {
                                    stmt = con.createStatement();
                                    stmt.executeUpdate("DELETE FROM `to-do_list`.task WHERE ID_Task="+toDelete);
                                    toDelete = 0;
                                    rs.close();
                                    con.close();
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }

                                sendMessage = new SendMessage(chatId, "✅ Dati cancellati correttamente!");
                                replyKeyboardMarkup.setKeyboard(keyboard);
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);

                                statusMap.remove(chatId);

                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else if(text.equals("❌ Annulla")){
                                sendMessage = new SendMessage(chatId, "❌ Annullato!");
                                replyKeyboardMarkup.setKeyboard(keyboard);
                                sendMessage.setReplyMarkup(replyKeyboardMarkup);
                                statusMap.remove(chatId);

                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            } else{
                                sendMessage = new SendMessage(chatId, "❌ Comando non valido. Riprova!");
                                try {
                                    telegramClient.execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                            break;

                        case "default":
                            switch (text) {
                                case "/start":
                                    String user_complete_name = update.getMessage().getChat().getFirstName();
                                    if(update.getMessage().getChat().getLastName() != null){
                                        user_complete_name += " " + update.getMessage().getChat().getLastName();
                                    }
                                    sendMessage = new SendMessage(chatId, "🤖 Benvenuto "+user_complete_name+"! Qui potrai gestire la tua to do list. Scegli l'operazione che vuoi effettuare!");

                                    //Bottoni
                                    if(keyboard.isEmpty()) {
                                        row1.add(buttonList);
                                        row2.add(button1);
                                        row2.add(button2);
                                        row2.add(button3);
                                        keyboard.add(row1);
                                        keyboard.add(row2);
                                    }
                                    replyKeyboardMarkup.setResizeKeyboard(true);
                                    replyKeyboardMarkup.setKeyboard(keyboard);
                                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                                    try {
                                        telegramClient.execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "➕ Aggiungi":
                                    sendMessage = new SendMessage(chatId, "🖊️ Inserisci il titolo.\n\n/annulla");
                                    statusMap.put(chatId, "getTitle");

                                    //Le due righe sotto nascondono i pulsanti del bot
                                    replyKeyboardRemove.setRemoveKeyboard(true);
                                    sendMessage.setReplyMarkup(replyKeyboardRemove);

                                    try {
                                        telegramClient.execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "✏️ Modifica":
                                    con = MySql.getConnection();
                                    n = 0;
                                    String temp = "";

                                    temp = "📝 Scegli la task da modificare:";

                                    //bottoni per scegliere la task
                                    List<KeyboardRow> keyboardManagement = new ArrayList<>();
                                    KeyboardRow rowManagement = new KeyboardRow();
                                    KeyboardButton buttonManagement;

                                    try {
                                        stmt = con.createStatement();
                                        rs = stmt.executeQuery("SELECT * FROM `to-do_list`.task WHERE ID_Utente="+update.getMessage().getChat().getId());
                                        while(rs.next()) {
                                            n+=1;
                                            if(n%5==0) { //per evitare che tutti i pulsanti vadano su una riga
                                                keyboardManagement.add(rowManagement);
                                                buttonManagement = new KeyboardButton(Integer.toString(n));
                                                rowManagement = new KeyboardRow(buttonManagement);
                                            } else{
                                                buttonManagement = new KeyboardButton(Integer.toString(n));
                                                rowManagement.add(buttonManagement);
                                            }
                                            temp += "\n\nTask " + n + ":\nTitolo: " + rs.getString(2) + "\nDescrizione: " + rs.getString(3) + "\nScadenza: " + rs.getString(4);
                                        }
                                        rs.close();
                                        con.close();
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    keyboardManagement.add(rowManagement);
                                    buttonManagement = new KeyboardButton("🔙 Indietro");
                                    rowManagement = new KeyboardRow(buttonManagement);

                                    sendMessage = new SendMessage(chatId, temp);

                                    keyboardManagement.add(rowManagement);
                                    replyKeyboardMarkup.setResizeKeyboard(true);
                                    replyKeyboardMarkup.setKeyboard(keyboardManagement);
                                    sendMessage.setReplyMarkup(replyKeyboardMarkup);
                                    statusMap.put(chatId, "modify");

                                    try {
                                        telegramClient.execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "🗑️ Rimuovi":
                                    con = MySql.getConnection();
                                    n = 0;

                                    temp = "🗑️ Scegli la task da rimuovere:";

                                    //bottoni per scegliere la task
                                    keyboardManagement = new ArrayList<>();
                                    rowManagement = new KeyboardRow();

                                    try {
                                        stmt = con.createStatement();
                                        rs = stmt.executeQuery("SELECT * FROM `to-do_list`.task WHERE ID_Utente="+update.getMessage().getChat().getId());
                                        while(rs.next()) {
                                            n+=1;
                                            if(n%5==0) { //per evitare che tutti i pulsanti vadano su una riga
                                                keyboardManagement.add(rowManagement);
                                                buttonManagement = new KeyboardButton(Integer.toString(n));
                                                rowManagement = new KeyboardRow(buttonManagement);
                                            } else{
                                                buttonManagement = new KeyboardButton(Integer.toString(n));
                                                rowManagement.add(buttonManagement);
                                            }
                                            temp += "\n\nTask " + n + ":\nTitolo: " + rs.getString(2) + "\nDescrizione: " + rs.getString(3) + "\nScadenza: " + rs.getString(4);
                                        }
                                        rs.close();
                                        con.close();
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }
                                    keyboardManagement.add(rowManagement);
                                    buttonManagement = new KeyboardButton("🔙 Indietro");
                                    rowManagement = new KeyboardRow(buttonManagement);

                                    sendMessage = new SendMessage(chatId, temp);

                                    keyboardManagement.add(rowManagement);
                                    replyKeyboardMarkup.setResizeKeyboard(true);
                                    replyKeyboardMarkup.setKeyboard(keyboardManagement);
                                    sendMessage.setReplyMarkup(replyKeyboardMarkup);

                                    statusMap.put(chatId, "remove");

                                    try {
                                        telegramClient.execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "☰ Lista":
                                    con = MySql.getConnection();
                                    n = 0;

                                    temp = "📄 Ecco l'elenco delle tue task!";

                                    try {
                                        stmt = con.createStatement();
                                        rs = stmt.executeQuery("SELECT * FROM `to-do_list`.task WHERE ID_Utente="+update.getMessage().getChat().getId());
                                        while(rs.next()) {
                                            n+=1;
                                            temp += "\n\nTask " + n + ":\nTitolo: " + rs.getString(2) + "\nDescrizione: " + rs.getString(3) + "\nScadenza: " + rs.getString(4);
                                        }
                                        rs.close();
                                        con.close();
                                    } catch (SQLException e) {
                                        throw new RuntimeException(e);
                                    }

                                    sendMessage = new SendMessage(chatId, temp);

                                    try {
                                        telegramClient.execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                default:
                                    sendMessage = new SendMessage(chatId, "❌ Comando non riconosciuto");

                                    //Bottoni
                                    if(keyboard.isEmpty()) {
                                        row1.add(buttonList);
                                        row2.add(button1);
                                        row2.add(button2);
                                        row2.add(button3);
                                        keyboard.add(row1);
                                        keyboard.add(row2);
                                    }
                                    replyKeyboardMarkup.setResizeKeyboard(true);
                                    replyKeyboardMarkup.setKeyboard(keyboard);
                                    sendMessage.setReplyMarkup(replyKeyboardMarkup);
                                    try {
                                        telegramClient.execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                            }
                    }
                }
            }
        }).start();
    }
}