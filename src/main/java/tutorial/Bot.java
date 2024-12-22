package tutorial;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    public String getBotUsername() {
        return "TutorialBot";
    }

    @Override
    public String getBotToken() {
        return "7641801953:AAFNbzraA6O41AZPCdscuQQiSLdl--ZKAfs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            String answer = "Выберите криптовалюту";
            Long userId = null;


            if (update.hasMessage() && update.getMessage().hasText()) { //если пользователь отправил боту текст
                whenUserSendTextToBot(update);

            } else if (update.hasCallbackQuery()) { //проверка что именно нажатие на кнопку
                CallbackQuery callbackQuery = update.getCallbackQuery(); //информация о нажатии на кнопку
                whenUserClickOnButtonInBot(callbackQuery, userId, answer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Первый параметр - это текст, который пришлет бот пользователю
     * Второй параметр - айди пользователя
     * Третий параметр - клавиатура (или null, если клавиатура не нужна)
     */
    private void sendMessageFromBot(String answer, Long userId, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sm = new SendMessage();
        sm.setChatId(userId.toString());
        sm.setText(answer);
        //если программист передал третьим параметром не пустышку (не null, а реальную клавиатуру),
        // то бот должен присылать эту переданную клавиатуру
        if (inlineKeyboardMarkup != null) {
            // присвой (засеть) клавиатуру тому сообщению, которое отправит бот пользователю
            sm.setReplyMarkup(inlineKeyboardMarkup);
        }
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * внутри этого метода код, который срабатывает, когда пользователь боту отправляет какой-то текст
     * @param update
     */
    void whenUserSendTextToBot(Update update) {
        Message msg = update.getMessage();
        String message = msg.getText(); // сообщение пользователя
        User user = msg.getFrom(); // сам пользователь
        Long id = user.getId(); // id пользователя

        // Создание клавиатуры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        // Создание кнопок
        rowList.add(createButtonRow("BTC-USDT", "1"));
        rowList.add(createButtonRow("ETH-USDT", "2"));
        rowList.add(createButtonRow("ADA-USDT", "3"));
        rowList.add(createButtonRow("NOT-USDT", "4"));

        inlineKeyboardMarkup.setKeyboard(rowList);

        if (message.equals("/start")) {
            sendMessageFromBot("Выберите криптовалюту: ", id, inlineKeyboardMarkup);
        }
    }

    private List<InlineKeyboardButton> createButtonRow(String buttonText, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(callbackData);

        return Arrays.asList(button); // Возвращаем список с одной кнопкой
    }


    void whenUserClickOnButtonInBot(CallbackQuery callbackQuery, Long userId, String answer) {
        userId = callbackQuery.getFrom().getId();

        // Создание клавиатуры
        InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard();

        String callbackData = callbackQuery.getData(); // то что записали в setCallbackData
        System.out.println(callbackData);
        switch (callbackData) {
            case "1":
            case "2":
            case "3":
            case "4":
                sendMessageFromBot("Выберите действие: ", userId, inlineKeyboardMarkup);
                break;
            case "buy":
                String s = getValue("ETH-EUR", true);
                sendMessageFromBot(s, userId, null);
                break;
            case "sell":
                String s1 = getValue("ETH-EUR", false);
                sendMessageFromBot(s1, userId, null);
                break;
            default:
                // Обработка случая, если callbackData не соответствует ни одному из ожидаемых значений
                sendMessageFromBot("Неизвестное действие.", userId, inlineKeyboardMarkup);
                break;
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        // Создание кнопок
        InlineKeyboardButton buyButton = createButton("Купить", "buy");
        InlineKeyboardButton sellButton = createButton("Продать", "sell");

        // Список кнопок для 1-го ряда клавиатуры
        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(buyButton);
        keyboardButtonsRow.add(sellButton);

        // Список рядов
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }


    // отдельный метод
    private void sendSticker(Long chatID, String name) {
        try {
            SendSticker sticker_msg = new SendSticker();
            sticker_msg.setChatId(chatID.toString());
            sticker_msg.setSticker(new InputFile(name));
            execute(sticker_msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    String getJsonString(String urlOpenApi) {
        String resultAnswerFromUrl = "";
        try {
            URL url = new URL(urlOpenApi);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("accept", "application/json");

            BufferedReader in = new BufferedReader(new InputStreamReader((httpURLConnection.getInputStream())));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resultAnswerFromUrl = resultAnswerFromUrl + inputLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultAnswerFromUrl;
    }

    String getValue(String symbol, boolean isBuy) {
        String cryptoUrl = "https://api.kucoin.com/api/v1/market/stats?symbol=" + symbol;
        String result = getJsonString(cryptoUrl);

        JSONObject jsonResponse = new JSONObject(result);
        JSONObject data = jsonResponse.getJSONObject("data");
        String s = data.getString("buy");
        return s;
    }
}
