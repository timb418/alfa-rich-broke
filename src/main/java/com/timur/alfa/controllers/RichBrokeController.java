package com.timur.alfa.controllers;

import com.timur.alfa.giphy.GiphyFeign;
import com.timur.alfa.giphy.GiphyHandler;
import com.timur.alfa.openexchange.OpenExchangeFeign;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Random;

@RestController
public class RichBrokeController {

    @Value("${currencyToCompare}")
    String currency;
    @Value("${currencyApiKey}")
    String currencyApiKey;
    @Value("${giphyApiKey}")
    String giphyApiKey;

    final GiphyHandler giphyHandler;
    private final OpenExchangeFeign openExchangeFeign;
    private final GiphyFeign giphyFeign;
    String datePathYesterday;


    public RichBrokeController(GiphyHandler giphyHandler, OpenExchangeFeign openExchangeFeign, GiphyFeign giphyFeign) {
        this.giphyHandler = giphyHandler;
        this.openExchangeFeign = openExchangeFeign;
        this.giphyFeign = giphyFeign;

        LocalDate current_date = LocalDate.now().minusDays(1);
        this.datePathYesterday = current_date.getYear() + "-" +
                Utils.numberToXXFormatter(current_date.getMonthValue()) + "-" +
                Utils.numberToXXFormatter(current_date.getDayOfMonth());
    }

    @GetMapping("/richorbroke")
    public String getCurrencyInfoForYesterday(/*@RequestParam(name = "currency") String currency*/) {
        ResponseEntity<String> responseEntityYesterday = openExchangeFeign.getCurrencyInfoForYesterday(datePathYesterday,
                currencyApiKey);
        ResponseEntity<String> responseEntityToday = openExchangeFeign.getCurrencyInfoForToday(currencyApiKey);

        JSONObject jsonObject = new JSONObject(responseEntityYesterday.getBody());
        Double yesterdayValue = jsonObject.getJSONObject("rates").getDouble(currency);

        jsonObject = new JSONObject(responseEntityToday.getBody());
        Double todayValue = jsonObject.getJSONObject("rates").getDouble(currency);

        // logic to return a giphy link
        String giphySearchQuery = todayValue >= yesterdayValue ? "Rich" : "Poor";
        ResponseEntity<String> responseEntityGiphy =
                giphyFeign.getGifLinkFromSearch(giphyApiKey, giphySearchQuery, 1,
                        new Random().nextInt(2000));
        jsonObject = new JSONObject(responseEntityGiphy.getBody());
        String gifLink = jsonObject.getJSONArray("data")
                .getJSONObject(0)
                .getJSONObject("images")
                .getJSONObject("original")
                .getString("url");

        return gifLink;
    }
}
