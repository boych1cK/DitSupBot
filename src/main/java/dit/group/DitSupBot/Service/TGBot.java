package dit.group.DitSupBot.Service;

import dit.group.DitSupBot.Email.EmailSender;
import dit.group.DitSupBot.LDAP.LDAP;
import dit.group.DitSupBot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TGBot extends TelegramLongPollingBot {

    final List<String> SupMails = new ArrayList<>();




    final BotConfig config;
    final HashMap<Long, ArrayList<String>> bazaHash = new HashMap<>();
    private static final String PHONE_NUMBER_GARBAGE_REGEX = "[()\\s-]+";
    private static final String PHONE_NUMBER_REGEX = "^((\\+[1-9]?[0-9])|0)?[7-9][0-9]{9}$";
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);


    public TGBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList();
        listofCommands.add(new BotCommand("/start","Старт"));
        listofCommands.add(new BotCommand("/help","Инструкция"));
        SupMails.add("help.lv1@support.adm-nk.ru");
        SupMails.add("help.lv2@support.adm-nk.ru");
        SupMails.add("help.lv3@support.adm-nk.ru");
        SupMails.add("help.1c.work@support.adm-nk.ru");
        SupMails.add("help.1c.upd@support.adm-nk.ru");
        SupMails.add("help.1c.prj@support.adm-nk.ru");
        SupMails.add("help.1c.cons@support.adm-nk.ru");
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
            log.info("BotCommandsHasBeenSet");
        } catch (TelegramApiException e)
        {
            log.error("ComandListError: "+ e.getMessage());
        }
    }

    public static boolean isValidMail(String email)
    {
        return email.matches("^[\\w-\\.]+@[\\w-]+(\\.[\\w-]+)*\\.[a-z]{2,}$");
    }
    public static boolean isValidPhone(String number)
    {
        number.replaceAll("[\\D]","");
        if(number.length()>=9 || number.length()<=11)
        {
            return true;
        }
        else {
            return false;
        }

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
                        bazaHash.get(chatID).add("none");
                        bazaHash.get(chatID).add("query");
                        bazaHash.get(chatID).add("0");

                        message.setChatId(chatID);
                        message.setText("Отлично, а теперь выберите категорию вашего обращения");
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
                    StartRecieved(chatID, update.getMessage().getChat().getFirstName());
                    if(bazaHash.containsKey(chatID)){
                        bazaHash.get(chatID).set(2, "query");
                        bazaHash.get(chatID).set(1, "none");
                    }
                } else if (Message.equals("Помощь") || Message.equals("/help") || Message.equals("помощь")) {
                                SendMessage(chatID, "Чтобы отправить обращение в техподдержку ДИТ напишите свою рабочую почту, " +
                                        "Выберите категорию из списка, опишите проблему и нажмите отправить.", true);
                    log.info("Message sent to "+chatID);
                }else if(bazaHash.containsKey(chatID) && bazaHash.get(chatID).get(2).equals("Phone")){
                    if(isValidPhone(Message)){
                        SendMessage(chatID, "Опишите проблему",false );
                        bazaHash.get(chatID).set(2,"Send");
                        bazaHash.get(chatID).set(3,Message);
                    }
                    else
                    {
                        SendMessage(chatID, "Неправильно введен номер, попробуйте ввести еще раз",false );
                    }
                }


                else if(bazaHash.containsKey(chatID) && bazaHash.get(chatID).get(2).equals("Send"))
                {

                    try {
                        if(new EmailSender().Send(Message, bazaHash.get(chatID).get(0), bazaHash.get(chatID).get(1), bazaHash.get(chatID).get(3)))
                        {
                            SendMessage(chatID, "Отправлено! \nЕсли хотите отправить еще одно обращение, нажмите кнопку Старт снизу.", true);
                            log.info("Message sent to "+chatID);
                        }
                        else
                        {
                            SendMessage(chatID, "Ошибка, попробуйте позже", true);
                            log.info("Message sent to "+chatID);
                        }
                    } catch (MessagingException e) {
                        log.error("Sending message error: "+ e.getMessage());
                    }
                    bazaHash.get(chatID).set(2, "query");
                    bazaHash.get(chatID).set(1, "none");
                }
                else {
                    SendMessage(chatID, "Не понял",false);
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
                message.setText("Уточните");
                message.setChatId(chatID);
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row1 = new ArrayList<>();
                List<InlineKeyboardButton> row2 = new ArrayList<>();
                List<InlineKeyboardButton> row3 = new ArrayList<>();
                List<InlineKeyboardButton> row4 = new ArrayList<>();
                List<InlineKeyboardButton> row5 = new ArrayList<>();
                List<InlineKeyboardButton> row6 = new ArrayList<>();
                List<InlineKeyboardButton> row7 = new ArrayList<>();
                switch (callbackData) {

                    case "OGM":
                        var ButtonSZMKUP = new InlineKeyboardButton();
                        var ButtonSZMKNIZ = new InlineKeyboardButton();

                        ButtonSZMKUP.setText("СЗМК - Верхняя площадка");
                        ButtonSZMKUP.setCallbackData("SZMKUP");

                        ButtonSZMKNIZ.setText("СЗМК - Нижняя площадка");
                        ButtonSZMKNIZ.setCallbackData("SZMKNIZ");

                        row1.add(ButtonSZMKUP);
                        row2.add(ButtonSZMKNIZ);

                        rows.add(row1);
                        rows.add(row2);

                        markup.setKeyboard(rows);
                        message.setReplyMarkup(markup);

                        try {
                            execute(message);
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
                        break;
                    case "Project":
                        var ButtonSZMKProj = new InlineKeyboardButton();
                        var ButtonSHSProj = new InlineKeyboardButton();
                        var ButtonUHProj = new InlineKeyboardButton();

                        ButtonSZMKProj.setText("Проект СЗМК");
                        ButtonSZMKProj.setCallbackData("SZMKProj");

                        ButtonSHSProj.setText("Проект СШС");
                        ButtonSHSProj.setCallbackData("SHSProj");

                        ButtonUHProj.setText("Проект УХ");
                        ButtonUHProj.setCallbackData("UHProj");

                        row1.add(ButtonSZMKProj);
                        row2.add(ButtonSHSProj);
                        row3.add(ButtonUHProj);


                        rows.add(row1);
                        rows.add(row2);
                        rows.add(row3);


                        markup.setKeyboard(rows);
                        message.setReplyMarkup(markup);
                        try {
                            execute(message);
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
                        break;

                    case "SZMKUP":
                        var ButtonSZMKUPNEI = new InlineKeyboardButton();
                        var ButtonSZMKUPTECH = new InlineKeyboardButton();

                        ButtonSZMKUPNEI.setText("Неисправность");
                        ButtonSZMKUPNEI.setCallbackData("ZSMKUPNEIS");

                        ButtonSZMKUPTECH.setText("Техобслуживание");
                        ButtonSZMKUPTECH.setCallbackData("ZSMKUPTECH");



                        row1.add(ButtonSZMKUPNEI);
                        row2.add(ButtonSZMKUPTECH);


                        rows.add(row1);
                        rows.add(row2);


                        markup.setKeyboard(rows);
                        message.setReplyMarkup(markup);
                        try {
                            execute(message);
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
                        break;
                    case "SZMKNIZ":
                        var ButtonSZMKNIZNEI = new InlineKeyboardButton();
                        var ButtonSZMKNIZTECH = new InlineKeyboardButton();

                        ButtonSZMKNIZNEI.setText("Неисправность");
                        ButtonSZMKNIZNEI.setCallbackData("ZSMKNIZNEIS");

                        ButtonSZMKNIZTECH.setText("Техобслуживание");
                        ButtonSZMKNIZTECH.setCallbackData("ZSMKNIZTECH");



                        row1.add(ButtonSZMKNIZNEI);
                        row2.add(ButtonSZMKNIZTECH);


                        rows.add(row1);
                        rows.add(row2);


                        markup.setKeyboard(rows);
                        message.setReplyMarkup(markup);
                        try {
                            execute(message);
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
                        break;
                    case "SZMKProj":
                        var ButtonSZMK1c = new InlineKeyboardButton();
                        var ButtonSZMK1cUser = new InlineKeyboardButton();
                        var ButtonSZMKPERM1c = new InlineKeyboardButton();
                        var ButtonSZMKOTCH1c = new InlineKeyboardButton();
                        var ButtonSZMKTeazh1c = new InlineKeyboardButton();
                        var ButtonSZMKanother = new InlineKeyboardButton();



                        ButtonSZMK1c.setText("Установка 1С");
                        ButtonSZMK1c.setCallbackData("1CInstallSZMKProj");

                        ButtonSZMK1cUser.setText("Создать пользователя 1С");
                        ButtonSZMK1cUser.setCallbackData("CreateUser1CSZMKProj");

                        ButtonSZMKPERM1c.setText("Права доступа 1С");
                        ButtonSZMKPERM1c.setCallbackData("Permission1CSZMKProj");

                        ButtonSZMKOTCH1c.setText("Доп. отчет 1С");
                        ButtonSZMKOTCH1c.setCallbackData("Otchet1CSZMKProj");

                        ButtonSZMKTeazh1c.setText("Обучение 1С");
                        ButtonSZMKTeazh1c.setCallbackData("Teach1CSZMKProj");

                        ButtonSZMKanother.setText("Другое");
                        ButtonSZMKanother.setCallbackData("AnotherSZMKProj");

                        row1.add(ButtonSZMK1c);
                        row2.add(ButtonSZMK1cUser);
                        row3.add(ButtonSZMKPERM1c);
                        row4.add(ButtonSZMKOTCH1c);
                        row5.add(ButtonSZMKTeazh1c);
                        row6.add(ButtonSZMKanother);


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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
                        break;
                    case "SHSProj":
                        var ButtonSHS1c = new InlineKeyboardButton();
                        var ButtonSHS1cUser = new InlineKeyboardButton();
                        var ButtonSHSPERM1c = new InlineKeyboardButton();
                        var ButtonSHSOTCH1c = new InlineKeyboardButton();
                        var ButtonSHSTeazh1c = new InlineKeyboardButton();
                        var ButtonSHSanother = new InlineKeyboardButton();



                        ButtonSHS1c.setText("Установка 1С");
                        ButtonSHS1c.setCallbackData("1CInstallSHSProj");

                        ButtonSHS1cUser.setText("Создать пользователя 1С");
                        ButtonSHS1cUser.setCallbackData("CreateUser1CSHSProj");

                        ButtonSHSPERM1c.setText("Права доступа 1С");
                        ButtonSHSPERM1c.setCallbackData("Permission1CSHSProj");

                        ButtonSHSOTCH1c.setText("Доп. отчет 1С");
                        ButtonSHSOTCH1c.setCallbackData("Otchet1CSHSProj");

                        ButtonSHSTeazh1c.setText("Обучение 1С");
                        ButtonSHSTeazh1c.setCallbackData("Teach1CSHSProj");

                        ButtonSHSanother.setText("Другое");
                        ButtonSHSanother.setCallbackData("AnotherSHSProj");

                        row1.add(ButtonSHS1c);
                        row2.add(ButtonSHS1cUser);
                        row3.add(ButtonSHSPERM1c);
                        row4.add(ButtonSHSOTCH1c);
                        row5.add(ButtonSHSTeazh1c);
                        row6.add(ButtonSHSanother);


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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
                        break;
                    case "UHProj":
                        var ButtonUH1c = new InlineKeyboardButton();
                        var ButtonUH1cUser = new InlineKeyboardButton();
                        var ButtonUHPERM1c = new InlineKeyboardButton();
                        var ButtonUHOTCH1c = new InlineKeyboardButton();
                        var ButtonUHTeazh1c = new InlineKeyboardButton();
                        var ButtonUHanother = new InlineKeyboardButton();



                        ButtonUH1c.setText("Установка 1С");
                        ButtonUH1c.setCallbackData("1CInstallUHProj");

                        ButtonUH1cUser.setText("Создать пользователя 1С");
                        ButtonUH1cUser.setCallbackData("CreateUser1CUHProj");

                        ButtonUHPERM1c.setText("Права доступа 1С");
                        ButtonUHPERM1c.setCallbackData("Permission1CUHProj");

                        ButtonUHOTCH1c.setText("Доп. отчет 1С");
                        ButtonUHOTCH1c.setCallbackData("Otchet1CUHProj");

                        ButtonUHTeazh1c.setText("Обучение 1С");
                        ButtonUHTeazh1c.setCallbackData("Teach1CUHProj");

                        ButtonUHanother.setText("Другое");
                        ButtonUHanother.setCallbackData("AnotherUHProj");

                        row1.add(ButtonUH1c);
                        row2.add(ButtonUH1cUser);
                        row3.add(ButtonUHPERM1c);
                        row4.add(ButtonUHOTCH1c);
                        row5.add(ButtonUHTeazh1c);
                        row6.add(ButtonUHanother);


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
                            log.info("Message sent to "+chatID);
                        } catch (TelegramApiException e) {
                            log.error("Sending message error: "+ e.getMessage());
                        }
                        break;
                    case "GetSim":
                    case "ChangeSim":
                    case "SimDetail":
                    case "ChangeTarif":
                    case "BlockSim":
                    case "OtherSim":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
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
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "1CBasePerm":
                    case "1CChngPerm":
                    case "Other1C":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
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
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "DataSync":
                    case "ZUP":
                    case "PeriodClose":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "UPP":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "MailInstall":
                    case "BankInstall":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "ResDostup":
                    case "AnotherAdress":
                    case "ProgInstall":
                    case "OtherInt":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "CartChange":
                    case "IDBlock":
                    case "PCProblem":
                    case "OrgtechProb":
                    case "ProgramInstall":
                    case "GetPC":
                    case "OtherTech":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "SoglDoc":
                    case "Corresp1C":
                    case "1CDocInstall":
                    case "OtherSad":
                    case "Instruction":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "ZSMKUPNEIS":
                    case "ZSMKUPTECH":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        SendMessage(chatID, "Опишите проблему",false);
                        break;
                    case "ZSMKNIZNEIS":
                    case "ZSMKNIZTECH":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "1CInstallSZMKProj":
                    case "CreateUser1CSZMKProj":
                    case "Permission1CSZMKProj":
                    case "Otchet1CSZMKProj":
                    case "Teach1CSZMKProj":
                    case "AnotherSZMKProj":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "1CInstallSHSProj":
                    case "CreateUser1CSHSProj":
                    case "Permission1CSHSProj":
                    case "Otchet1CSHSProj":
                    case "Teach1CSHSProj":
                    case "AnotherSHSProj":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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
                    case "1CInstallUHProj":
                    case "CreateUser1CUHProj":
                    case "Permission1CUHProj":
                    case "Otchet1CUHProj":
                    case "Teach1CUHProj":
                    case "AnotherUHProj":
                        bazaHash.get(chatID).set(1,"petrgfjsv@gmail.com");
                        bazaHash.get(chatID).set(2,"Send");
                        if(bazaHash.get(chatID).get(3).equals("0")) {
                            KeyboardButton contact = new KeyboardButton("Поделиться номером телефона");
                            contact.setRequestContact(true);
                            KeyboardRow row = new KeyboardRow();
                            row.add(contact);

                            List<KeyboardRow> keyboard = new ArrayList<>();
                            keyboard.add(row);

                            ReplyKeyboardMarkup Phonemarkup = new ReplyKeyboardMarkup();
                            Phonemarkup.setKeyboard(keyboard);
                            Phonemarkup.setResizeKeyboard(true);

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatID);
                            sendMessage.setText("Для более оперативного решения вашей проблемы мы запрашиваем доступ к вашему номеру телефона. \nЕсли вы не хотите делиться своим контактом, то просто опишите свою проблему.");
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

            }else {
                StartRecieved(chatID,"name");
            }
        }
        else if(update.getMessage().hasContact()&&bazaHash.containsKey(update.getMessage().getChatId()))
        {
            long chatID = update.getMessage().getChatId();
            if(bazaHash.get(chatID).get(2).equals("Send")) {
                bazaHash.get(chatID).set(3, update.getMessage().getContact().getPhoneNumber());
                SendMessage(chatID,"Опишите проблему",false);
            }
            else {
                StartRecieved(chatID,bazaHash.get(chatID).get(0));
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
            message.setText("Привет, "+ bazaHash.get(chatID).get(0)+", выберите категорию вашего обращения:");
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
