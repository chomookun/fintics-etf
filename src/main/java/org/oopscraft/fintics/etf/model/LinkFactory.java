package org.oopscraft.fintics.etf.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LinkFactory {

    public static List<Link> getLinks(Asset asset) {
        List<Link> links = new ArrayList<>();
        String market = Optional.ofNullable(asset.getMarket()).orElse("");
        switch (market) {
            case "US" -> links.addAll(getUsLinks(asset));
            case "KR" -> links.addAll(getKrLinks(asset));
        }
        return links;
    }

    static List<Link> getUsLinks(Asset asset) {
        List<Link> links = new ArrayList<>();
        String symbol = asset.getSymbol();
        String exchange = Optional.ofNullable(asset.getExchange()).orElse("");
        // alphasquare
        links.add(Link.of("Alphasquare", String.format("https://alphasquare.co.kr/home/market-summary?code=%s", asset.getSymbol())));
        // nasdaq
        links.add(Link.of("Nasdaq", String.format("https://www.nasdaq.com/market-activity/etf/%s", symbol)));
        // yahoo
        links.add(Link.of("Yahoo", String.format("https://finance.yahoo.com/quote/%s", symbol)));
        // finviz
        links.add(Link.of("Finviz", String.format("https://finviz.com/quote.ashx?t=%s", symbol)));
        // seekingalpha
        links.add(Link.of("Seekingalpha", String.format("https://seekingalpha.com/symbol/%s", symbol)));
        // morningstar
        switch (exchange) {
            case "XASE" -> links.add(Link.of("Morningstar", String.format("https://www.morningstar.com/etfs/arcx/%s/quote", symbol.toLowerCase())));
            default -> links.add(Link.of("Morningstar", String.format("https://www.morningstar.com/etfs/%s/%s/quote", exchange.toLowerCase(), symbol.toLowerCase())));
        }
        // etf.com
        links.add(Link.of("etf.com", String.format("https://etf.com/%s", symbol)));
        // return
        return links;
    }

    static List<Link> getKrLinks(Asset asset) {
        List<Link> links = new ArrayList<>();
        // alphasquare
        links.add(Link.of("Alphasquare", String.format("https://alphasquare.co.kr/home/market-summary?code=%s", asset.getSymbol())));
        // naver
        links.add(Link.of("Naver", String.format("https://finance.naver.com/item/main.naver?code=%s", asset.getSymbol())));
        // etfcheck
        links.add(Link.of("ETFCheck", String.format("https://www.etfcheck.co.kr/mobile/etpitem/%s", asset.getSymbol())));
        // return
        return links;
    }

}
