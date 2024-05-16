package dit.group.DitSupBot.Service;

import dit.group.DitSupBot.Email.EmailSender;
import dit.group.DitSupBot.LDAP.LDAP;
import dit.group.DitSupBot.config.BotConfig;
import dit.group.DitSupBot.config.Categories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class TGBot extends TelegramLongPollingBot {

    final BotConfig config;
    Categories categories;


    final HashMap<Long, ArrayList<String>> bazaHash = new HashMap<>();
    public TGBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList();
        listofCommands.add(new BotCommand("/start","Старт"));
        listofCommands.add(new BotCommand("/help","Инструкция"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
            log.info("BotCommandsHasBeenSet");
        } catch (TelegramApiException e)
        {
            log.error("ComandListError: "+ e.getMessage());
        }
        categories = new Categories();

    }

    public static boolean isValidMail(String email)
    {
        return email.matches("^[\\w-\\.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$");
    }
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {

            if (isValidMail(update.getMessage().getText())) {
                SendMessage message = new SendMessage();
                String Message = update.getMessage().getText();
                long chatID = update.getMessage().getChatId();
                if(new LDAP().MailVerify(Message)) {
                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    for(int i = 0; i < categories.catList.size(); i++)
                    {
                        var Button = new InlineKeyboardButton();
                        List<InlineKeyboardButton> buttList = new ArrayList<>();
                        Button.setText(categories.catList.get(i).getAttributes().getNamedItem("name").getNodeValue());
                        Button.setCallbackData(categories.catList.get(i).getAttributes().getNamedItem("id").getNodeValue());
                        buttList.add(Button);
                        rows.add(buttList);
                        //System.out.println(Button.getText());
                    }
                    markup.setKeyboard(rows);
                    message.setReplyMarkup(markup);
                    message.setChatId(chatID);
                    if (bazaHash.containsKey(chatID)) {
                        message.setChatId(chatID);
                        message.setText("Здравствуйте, " + new LDAP().getName(bazaHash.get(chatID).get(2)) + ", выберите категорию вашего обращения");
                        bazaHash.get(chatID).set(0,"start");
                        bazaHash.get(chatID).set(1,"noSendTo");
                        bazaHash.get(chatID).set(3,"noPhone");
                        bazaHash.get(chatID).set(4,"noSubject");
                        //System.out.println(bazaHash.get(chatID));

                    } else {
                        ArrayList<String> member = new ArrayList<>();
                        bazaHash.put(chatID, member);
                        bazaHash.get(chatID).add("start");
                        bazaHash.get(chatID).add("noSendTo");
                        bazaHash.get(chatID).add(Message);
                        bazaHash.get(chatID).add("noPhone");
                        bazaHash.get(chatID).add("noSubject");

                        message.setChatId(chatID);
                        message.setText("Отлично, а теперь выберите категорию вашего обращения");
                        //System.out.println(bazaHash.get(chatID));
                    }
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        log.error("Sending message error: " + e.getMessage());
                    }
                }
                else {
                    SendMessage(chatID, "Почта неверна", false);
                }

            } else{
                String Message = update.getMessage().getText();
                long chatID = update.getMessage().getChatId();
                if(Message.equals("/start") || Message.equals("Старт") || Message.equals("старт"))
                {
                    StartRecieved(chatID);
                    if(bazaHash.containsKey(chatID)){
                        bazaHash.get(chatID).set(0, "start");
                        bazaHash.get(chatID).set(1, "noSendTo");
                        bazaHash.get(chatID).set(4, "noSubject");
                    }
                } else if (Message.equals("Помощь") || Message.equals("/help") || Message.equals("помощь")) {
                                SendMessage(chatID, "Чтобы отправить обращение в техподдержку ДИТ напишите свою рабочую почту, " +
                                        "Выберите категорию из списка, опишите проблему и нажмите отправить.", true);
                    log.info("Message sent to "+chatID);
                }
                else if(bazaHash.containsKey(chatID) && bazaHash.get(chatID).get(0).equals("Send"))
                {

                    try {
                        if(new EmailSender().Send(Message, bazaHash.get(chatID).get(4), bazaHash.get(chatID).get(1), bazaHash.get(chatID).get(3), bazaHash.get(chatID).get(2)))
                        {
                            SendMessage(chatID, "Отправлено! \nЕсли хотите отправить еще одно обращение, нажмите кнопку Старт снизу.", true);
                            log.info("Message sent to "+chatID);
                            bazaHash.get(chatID).set(0, "start");
                            bazaHash.get(chatID).set(1, "noSendTo");
                            bazaHash.get(chatID).set(4, "noSubject");
                        }
                        else
                        {
                            SendMessage(chatID, "Ошибка, попробуйте позже", true);
                        }

                    } catch (MessagingException e) {
                        log.error("Sending message error: "+ e.getMessage());
                        SendMessage(chatID, "Ошибка, попробуйте позже", true);
                    }

                }
                else {
                    SendMessage(chatID, "Команда не распознана",false);
                }

            }

        }
        else if(update.hasCallbackQuery()){
            String callbackData = update.getCallbackQuery().getData();
            long chatID = update.getCallbackQuery().getMessage().getChatId();
            if(callbackData.equals("NoEmailButton")) {
                if(bazaHash.containsKey(chatID)) {
                    SendMessage(chatID, "Ваша почта "+ bazaHash.get(chatID).get(0), false);
                }
                else {
                    SendMessage(chatID, "Обратитесь к сетевому специалисту, уточните информацию и напишите почту еще раз", false);
                }
            }
            else if(bazaHash.containsKey(chatID)) {
                SendMessage message = new SendMessage();
                message.setChatId(chatID);
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                boolean found = false;
                for (int i = 0; i < categories.catList.size(); i++) {
                    if (categories.catList.get(i).getAttributes().getNamedItem("id").getNodeValue().equals(callbackData)) {
                        //System.out.println(categories.catList.get(i).getAttributes().getNamedItem("id").getNodeValue());
                        NodeList sublist = categories.catList.get(i).getChildNodes();
                        //System.out.println(sublist.getLength());
                        Node current;
                        found = true;
                        message.setText("Уточните");

                        for (int j = 0; j < sublist.getLength(); j++) {
                            current = sublist.item(j);
                            if (current.getNodeType() == Node.ELEMENT_NODE) {
                                List<InlineKeyboardButton> row = new ArrayList<>();
                                var Button = new InlineKeyboardButton();
                                Button.setText(current.getAttributes().getNamedItem("name").getNodeValue());
                                Button.setCallbackData(current.getAttributes().getNamedItem("id").getNodeValue()+"~"+i);
                                //System.out.println(current.getAttributes().getNamedItem("id").getNodeValue()+"~"+i);
                                row.add(Button);
                                rows.add(row);
                            }
                        }
                        break;
                    }
                }
                if(!found)
                {
                    bazaHash.get(chatID).set(1, "noSendTo");
                    bazaHash.get(chatID).set(0, "start");
                    bazaHash.get(chatID).set(4, "noSubject");
                    String[] splitted = callbackData.split("~");
                        //System.out.println(splitted[1]);
                        NodeList sublist = categories.catList.get(Integer.parseInt(splitted[1])).getChildNodes();
                        Node current;

                        for (int j = 0; j < sublist.getLength(); j++) {
                            current = sublist.item(j);
                            if (current.getNodeType() == Node.ELEMENT_NODE) {
                                if(current.getAttributes().getNamedItem("id").getNodeValue().equals(splitted[0]))
                                {
                                    String mail = current.getAttributes().getNamedItem("mail").getNodeValue();
                                    bazaHash.get(chatID).set(1, mail);
                                    bazaHash.get(chatID).set(0, "Send");
                                    bazaHash.get(chatID).set(4, current.getAttributes().getNamedItem("name").getNodeValue());
                                    //System.out.println(bazaHash.get(chatID));
                                    if(bazaHash.get(chatID).get(3).equals("noPhone"))
                                    {
                                        KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                                        contact.setRequestContact(true);
                                        KeyboardRow row = new KeyboardRow();
                                        row.add(contact);

                                        List<KeyboardRow> keyboard = new ArrayList<>();
                                        keyboard.add(row);

                                        ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                                        Phonemarkup.setKeyboard(keyboard);
                                        Phonemarkup.setIsPersistent(true);
                                        Phonemarkup.setResizeKeyboard(true);

                                        SendMessage sendMessage = new SendMessage();
                                        sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. " +
                                                "\nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
                                        sendMessage.setChatId(chatID);
                                        sendMessage.setReplyMarkup(Phonemarkup);

                                        try {
                                            execute(sendMessage);
                                        } catch (TelegramApiException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                        SendMessage(chatID, "Опишите проблему",false);
                                    }
                                    break;
                                }
                            }
                        }

                }
                markup.setKeyboard(rows);
                message.setReplyMarkup(markup);
                message.setChatId(chatID);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }else {
                StartRecieved(chatID);
            }
        }
        else if(update.getMessage().hasContact()&&bazaHash.containsKey(update.getMessage().getChatId()))
        {
            long chatID = update.getMessage().getChatId();
            if(bazaHash.get(chatID).get(0).equals("Send")) {
                bazaHash.get(chatID).set(3, update.getMessage().getContact().getPhoneNumber());
                SendMessage(chatID,"Опишите проблему",false);
            }
            else {
                StartRecieved(chatID);
            }
        }
    }
    @Override
    public String getBotToken(){
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    private void StartRecieved(long chatID){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatID));
        if(bazaHash.containsKey(chatID))
        {
            message.setText("Здравствуйте, "+new LDAP().getName(bazaHash.get(chatID).get(2))+", выберите категорию вашего обращения:");
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            for(int i = 0; i < categories.catList.size(); i++)
            {
                var Button = new InlineKeyboardButton();
                List<InlineKeyboardButton> buttList = new ArrayList<>();
                Button.setText(categories.catList.get(i).getAttributes().getNamedItem("name").getNodeValue());
                Button.setCallbackData(categories.catList.get(i).getAttributes().getNamedItem("id").getNodeValue());
                buttList.add(Button);
                rows.add(buttList);
                //System.out.println(Button.getText());
            }
            markup.setKeyboard(rows);
            message.setReplyMarkup(markup);
            message.setChatId(chatID);
        }
        else {
            message.setText("Введите адрес вашей электронной почты");
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            var Button = new InlineKeyboardButton();

            Button.setText("Я не знаю электронную почту!");
            Button.setCallbackData("NoEmailButton");
            row.add(Button);
            rows.add(row);
            markup.setKeyboard(rows);
            message.setReplyMarkup(markup);

        }
        try {
            execute(message);
            log.info("Message sent to "+chatID);
        }
        catch (TelegramApiException e)
        {
            log.error("Sending message error: "+ e.getMessage());
        }
    }
    private  void  SendMessage(long chatID, String Text, boolean ButtonFlag){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatID));
        message.setText(Text);
        if(ButtonFlag)
        {
            ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
            List<KeyboardRow> rows = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add("Помощь");
            row.add("Старт");
            rows.add(row);
            markup.setKeyboard(rows);
            message.setReplyMarkup(markup);
        }
        try {
            execute(message);
            log.info("Message sent to "+String.valueOf(chatID));
        }
        catch (TelegramApiException e)
        {
            log.error("Sending message error: "+ e.getMessage());
        }
    }
}
