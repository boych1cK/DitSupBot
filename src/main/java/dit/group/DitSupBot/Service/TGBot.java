package dit.group.DitSupBot.Service;

import dit.group.DitSupBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScope;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class TGBot extends TelegramLongPollingBot {

    final BotConfig config;
    final HashMap<Long, ArrayList<String>> bazaHash = new HashMap<>();

    public TGBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList();
        listofCommands.add(new BotCommand("/start","Старт"));
        listofCommands.add(new BotCommand("/help","Инструкция"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e)
        {
        }
    }

    public static boolean isValid(String email)
    {
        return email.matches("^[\\w-\\.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            if (isValid(update.getMessage().getText())) {
                SendMessage message = new SendMessage();
                String Message = update.getMessage().getText();
                long chatID = update.getMessage().getChatId();
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                List<InlineKeyboardButton> row2 = new ArrayList<>();
                List<InlineKeyboardButton> row3 = new ArrayList<>();
                List<InlineKeyboardButton> row4 = new ArrayList<>();
                var ButtonCorp = new InlineKeyboardButton();
                var ButtonOGM = new InlineKeyboardButton();
                var Button1C = new InlineKeyboardButton();
                var ButtonSysInt = new InlineKeyboardButton();
                var ButtonTech = new InlineKeyboardButton();
                var ButtonSad = new InlineKeyboardButton();
                var ButtonPrj = new InlineKeyboardButton();
                ButtonCorp.setText("Связь");
                ButtonCorp.setCallbackData("CorpSvyaz");

                ButtonOGM.setText("ОГМ");
                ButtonOGM.setCallbackData("OGM");

                Button1C.setText("Системы управления");
                Button1C.setCallbackData("1c");

                ButtonSysInt.setText("Системная интеграция");
                ButtonSysInt.setCallbackData("OSI");

                ButtonTech.setText("Техническое обеспечение");
                ButtonTech.setCallbackData("OTO");

                ButtonSad.setText("СЭД");
                ButtonSad.setCallbackData("SAD");

                ButtonPrj.setText("Проекты");
                ButtonPrj.setCallbackData("Project");

                row1.add(ButtonCorp);
                row1.add(ButtonOGM);
                row2.add(Button1C);
                row2.add(ButtonSysInt);
                row3.add(ButtonSad);
                row3.add(ButtonPrj);
                row4.add(ButtonTech);
                rows.add(row1);
                rows.add(row2);
                rows.add(row3);
                rows.add(row4);
                markup.setKeyboard(rows);
                message.setReplyMarkup(markup);
                message.setChatId(chatID);
                if (bazaHash.containsKey(chatID)) {
                    message.setChatId(chatID);
                    message.setText("Привет, " + bazaHash.get(chatID).get(0) + ", выберите категорию вашего обращения");

                } else {
                    ArrayList<String> member = new ArrayList<>();
                    member.add(Message);
                    bazaHash.put(chatID, member);

                    message.setChatId(chatID);
                    message.setText("Мы вас запомнили))))-_-, выберите категорию вашего обращения");
                }
                try {
                    execute(message);
                } catch (TelegramApiException e) {

                }

            } else {
                String Message = update.getMessage().getText();
                long chatID = update.getMessage().getChatId();
                {
                    switch (Message) {
                        case "/start": {
                            StartRecieved(chatID, update.getMessage().getChat().getFirstName());
                            break;
                        }
                        default:
                            SendMessage(chatID, "Не понял");
                    }
                }
            }
        }
        else if(update.hasCallbackQuery()){
            String callbackData = update.getCallbackQuery().getData();
            long chatID = update.getCallbackQuery().getMessage().getChatId();
            SendMessage message = new SendMessage();
            message.setText("Уточните");
            message.setChatId(chatID);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            List<InlineKeyboardButton> row4 = new ArrayList<>();
            List<InlineKeyboardButton> row5 = new ArrayList<>();
            List<InlineKeyboardButton> row6= new ArrayList<>();
            List<InlineKeyboardButton> row7= new ArrayList<>();
            switch (callbackData){
                case "NoEmailButton":
                    SendMessage(chatID, "Обратитесь к сетевому специалисту, уточните информацию и напишите почту еще раз");
                    break;
                case "CorpSvyaz":
                    var ButtonGetSim = new InlineKeyboardButton();
                    var ButtonChngSim = new InlineKeyboardButton();
                    var ButtonSimDetail = new InlineKeyboardButton();
                    var ButtonSysChngTarif = new InlineKeyboardButton();
                    var ButtonBlockSim = new InlineKeyboardButton();
                    var ButtonSnotherSim = new InlineKeyboardButton();
                    ButtonGetSim.setText("Получить корп. СИМ-Карту");
                    ButtonGetSim.setCallbackData("GetSim");

                    ButtonChngSim.setText("Замена корп. СИМ-Карты");
                    ButtonChngSim.setCallbackData("ChangeSim");

                    ButtonSimDetail.setText("Детализация корп. СИМ-Карты");
                    ButtonSimDetail.setCallbackData("SimDetail");

                    ButtonSysChngTarif.setText("Смена тариф. плана");
                    ButtonSysChngTarif.setCallbackData("ChangeTarif");

                    ButtonBlockSim.setText("(Раз)Блокировка СИМ-Карты");
                    ButtonBlockSim.setCallbackData("BlockSim");

                    ButtonSnotherSim.setText("Другое");
                    ButtonSnotherSim.setCallbackData("OtherSim");

                    row1.add(ButtonGetSim);
                    row2.add(ButtonChngSim);
                    row3.add(ButtonSimDetail);
                    row4.add(ButtonSysChngTarif);
                    row5.add(ButtonBlockSim);
                    row6.add(ButtonSnotherSim);

                    rows.add(row1);
                    rows.add(row2);
                    rows.add(row3);
                    rows.add(row4);
                    rows.add(row5);
                    rows.add(row6);

                    markup.setKeyboard(rows);
                    message.setReplyMarkup(markup);

                    try {
                        execute(message);
                    } catch (TelegramApiException e) {

                    }
                    break;
                case "OGM":

                    break;
                case "1c":
                    var Button1CBasePerm = new InlineKeyboardButton();
                    var Button1CBasePermChng = new InlineKeyboardButton();
                    var ButtonDataSync = new InlineKeyboardButton();
                    var ButtonZUP = new InlineKeyboardButton();
                    var ButtonPeriodClose = new InlineKeyboardButton();
                    var Button1CUPP = new InlineKeyboardButton();
                    var ButtonAnother1C = new InlineKeyboardButton();
                    Button1CBasePerm.setText("Доступ к базам 1С");
                    Button1CBasePerm.setCallbackData("1CBasePerm");

                    Button1CBasePermChng.setText("Доступ к сетевым ресурсам");
                    Button1CBasePermChng.setCallbackData("1CChngPerm");

                    ButtonDataSync.setText("Запрос на синхронизацию данных");
                    ButtonDataSync.setCallbackData("DataSync");

                    ButtonZUP.setText("Проблема по работе в ЗУП");
                    ButtonZUP.setCallbackData("ZUP");

                    ButtonPeriodClose.setText("Вопросы по закрытию периода");
                    ButtonPeriodClose.setCallbackData("PeriodClose");

                    Button1CUPP.setText("Проблемы по работе в 1С УПП");
                    Button1CUPP.setCallbackData("UPP");

                    ButtonAnother1C.setText("Прочие вопросы по 1С");
                    ButtonAnother1C.setCallbackData("Other1C");

                    row1.add(Button1CBasePerm);
                    row2.add(Button1CBasePermChng);
                    row3.add(ButtonDataSync);
                    row4.add(ButtonZUP);
                    row5.add(ButtonPeriodClose);
                    row6.add(Button1CUPP);
                    row7.add(ButtonAnother1C);

                    rows.add(row1);
                    rows.add(row2);
                    rows.add(row3);
                    rows.add(row4);
                    rows.add(row5);
                    rows.add(row6);
                    rows.add(row7);

                    markup.setKeyboard(rows);
                    message.setReplyMarkup(markup);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {

                    }
                    break;
                case "OSI":
                    var ButtonMailOnPhone = new InlineKeyboardButton();
                    var ButtonGetRes = new InlineKeyboardButton();
                    var ButtonMailPermission = new InlineKeyboardButton();
                    var ButtonInstall = new InlineKeyboardButton();
                    var ButtonBank = new InlineKeyboardButton();
                    var ButtonAnotherInt = new InlineKeyboardButton();
                    ButtonMailOnPhone.setText("Установка эл.почты на телефон");
                    ButtonMailOnPhone.setCallbackData("MailInstall");

                    ButtonGetRes.setText("Доступ к сетевым ресурсам");
                    ButtonGetRes.setCallbackData("ResDostup");

                    ButtonMailPermission.setText("Отправка писем на внешние адреса");
                    ButtonMailPermission.setCallbackData("AnotherAdress");

                    ButtonInstall.setText("Установка ПО");
                    ButtonInstall.setCallbackData("ProgInstall");

                    ButtonBank.setText("Установка/настройка ЭЦП, Банков, ККМ");
                    ButtonBank.setCallbackData("BankInstall");

                    ButtonAnotherInt.setText("Другое");
                    ButtonAnotherInt.setCallbackData("OtherInt");

                    row1.add(ButtonMailOnPhone);
                    row2.add(ButtonGetRes);
                    row3.add(ButtonMailPermission);
                    row4.add(ButtonInstall);
                    row5.add(ButtonBank);
                    row6.add(ButtonAnotherInt);

                    rows.add(row1);
                    rows.add(row2);
                    rows.add(row3);
                    rows.add(row4);
                    rows.add(row5);
                    rows.add(row6);

                    markup.setKeyboard(rows);
                    message.setReplyMarkup(markup);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {

                    }
                    break;
                case "OTO":
                    var ButtonCart = new InlineKeyboardButton();
                    var ButtonID = new InlineKeyboardButton();
                    var ButtonPCIBP = new InlineKeyboardButton();
                    var ButtonOrgTech = new InlineKeyboardButton();
                    var ButtonProgInstall = new InlineKeyboardButton();
                    var ButtonGetPC = new InlineKeyboardButton();
                    var ButtonAnotherPC = new InlineKeyboardButton();
                    ButtonCart.setText("Запрос на замену картриджа");
                    ButtonCart.setCallbackData("CartChange");

                    ButtonID.setText("(раз)Блокировка пропуска");
                    ButtonID.setCallbackData("IDBlock");

                    ButtonPCIBP.setText("Проблемы с ПК, ИБП");
                    ButtonPCIBP.setCallbackData("PCProblem");

                    ButtonOrgTech.setText("Проблема с оргтехникой");
                    ButtonOrgTech.setCallbackData("OgrtechProb");

                    ButtonProgInstall.setText("Запрос на установку ПО");
                    ButtonProgInstall.setCallbackData("ProgramInstall");

                    ButtonGetPC.setText("Выдача ПК, оргтехники, оборудования");
                    ButtonGetPC.setCallbackData("GetPC");

                    ButtonAnotherPC.setText("Прочие проблемы с оборудованием");
                    ButtonAnotherPC.setCallbackData("OtherTech");

                    row1.add(ButtonCart);
                    row2.add(ButtonID);
                    row3.add(ButtonPCIBP);
                    row4.add(ButtonOrgTech);
                    row5.add(ButtonProgInstall);
                    row6.add(ButtonGetPC);
                    row7.add(ButtonAnotherPC);

                    rows.add(row1);
                    rows.add(row2);
                    rows.add(row3);
                    rows.add(row4);
                    rows.add(row5);
                    rows.add(row6);
                    rows.add(row7);

                    markup.setKeyboard(rows);
                    message.setReplyMarkup(markup);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {

                    }
                    break;
                case "SAD":
                    var ButtonSogl = new InlineKeyboardButton();
                    var ButtonCorres = new InlineKeyboardButton();
                    var Button1CDoc = new InlineKeyboardButton();
                    var ButtonOtherSad = new InlineKeyboardButton();
                    var ButtonVideo = new InlineKeyboardButton();

                    ButtonSogl.setText("Проблема по согласованию договоров");
                    ButtonSogl.setCallbackData("SoglDoc");

                    ButtonCorres.setText("Проблема по корреспонденции 1С");
                    ButtonCorres.setCallbackData("Corresp1C");

                    Button1CDoc.setText("Установка 1С Документооборот");
                    Button1CDoc.setCallbackData("1CDocInstall");

                    ButtonOtherSad.setText("Прочие вопросы по СЭД");
                    ButtonOtherSad.setCallbackData("OtherSad");

                    ButtonVideo.setText("Согласование договора - инструкция");
                    ButtonVideo.setCallbackData("Instruction");



                    row1.add(ButtonSogl);
                    row2.add(ButtonCorres);
                    row3.add(Button1CDoc);
                    row4.add(ButtonOtherSad);
                    row5.add(ButtonVideo);


                    rows.add(row1);
                    rows.add(row2);
                    rows.add(row3);
                    rows.add(row4);
                    rows.add(row5);


                    markup.setKeyboard(rows);
                    message.setReplyMarkup(markup);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {

                    }
                    break;
                case "Project":
                    SendMessage(chatID, "Корп связь");
                    break;
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

    private void StartRecieved(long chatID, String name){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatID));
        if(bazaHash.containsKey(chatID))
        {
            message.setText("Привет, "+ bazaHash.get(chatID));
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            List<InlineKeyboardButton> row2 = new ArrayList<>();
            List<InlineKeyboardButton> row3 = new ArrayList<>();
            List<InlineKeyboardButton> row4 = new ArrayList<>();
            var ButtonCorp = new InlineKeyboardButton();
            var ButtonOGM = new InlineKeyboardButton();
            var Button1C = new InlineKeyboardButton();
            var ButtonSysInt = new InlineKeyboardButton();
            var ButtonTech = new InlineKeyboardButton();
            var ButtonSad = new InlineKeyboardButton();
            var ButtonPrj = new InlineKeyboardButton();
            ButtonCorp.setText("Связь");
            ButtonCorp.setCallbackData("CorpSvyaz");

            ButtonOGM.setText("ОГМ");
            ButtonOGM.setCallbackData("OGM");

            Button1C.setText("Системы управления");
            Button1C.setCallbackData("1c");

            ButtonSysInt.setText("Системная интеграция");
            ButtonSysInt.setCallbackData("OSI");

            ButtonTech.setText("Техническое обеспечение");
            ButtonTech.setCallbackData("OTO");

            ButtonSad.setText("СЭД");
            ButtonSad.setCallbackData("SAD");

            ButtonPrj.setText("Проекты");
            ButtonPrj.setCallbackData("Project");

            row1.add(ButtonCorp);
            row1.add(ButtonOGM);
            row2.add(Button1C);
            row2.add(ButtonSysInt);
            row3.add(ButtonSad);
            row3.add(ButtonPrj);
            row4.add(ButtonTech);
            rows.add(row1);
            rows.add(row2);
            rows.add(row3);
            rows.add(row4);
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
        }
        catch (TelegramApiException e)
        {

        }
    }
    private  void  SendMessage(long chatID, String Text){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatID));
        message.setText(Text);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Помощь");
        rows.add(row);
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        }
        catch (TelegramApiException e)
        {

        }
    }
}
