package org.oopscraft.fintics.etf.model;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IconFactory {

    private static final Cache<String, String> iconCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public static String getIcon(Asset asset) {
        // checks cache
        String icon = iconCache.getIfPresent(asset.getAssetId());
        if (icon != null) {
            return icon;
        }
        // get icon
        icon = switch (Optional.ofNullable(asset.getMarket()).orElse("")) {
            case "US" -> getUsIcon(asset);
            case "KR" -> getKrIcon(asset);
            default -> null;
        };
        if (!isIconAvailable(icon)) {
            icon = "/static/image/icon-asset.svg";
        }
        iconCache.put(asset.getAssetId(), icon);
        return icon;
    }

    static String getUsIcon(Asset asset) {
        String assetName = asset.getName();
        String etfBrand = assetName.split("\\s+")[0].toLowerCase();
        return switch (etfBrand) {
            case "spdr" -> "https://www.ssga.com/favicon.ico";
            case "global" -> "https://www.globalxetfs.com/favicon.ico";
            case "goldman" -> "https://cdn.gs.com/images/goldman-sachs/v1/gs-favicon.svg";
            default -> String.format("https://s3-symbol-logo.tradingview.com/%s.svg", etfBrand);
        };
    }

    static String getKrIcon(Asset asset) {
        String assetName = asset.getName();
        String etfBrand = assetName.split("\\s+")[0];
        return switch (etfBrand) {
            case "KODEX" -> "https://www.samsungfund.com/assets/icons/favicon.png";
            case "TIGER" -> "https://www.tigeretf.com/common/images/favicon.ico";
            case "KBSTAR" ->"https://www.kbstaretf.com/favicon.ico";
            case "KOSEF" -> "https://www.kosef.co.kr/favicon.ico";
            case "ACE" -> "https://www.aceetf.co.kr/favicon.ico";
            case "ARIRANG" -> "http://arirangetf.com/image/common/favicon.ico";
            case "SOL" -> "https://www.soletf.com/static/pc/img/common/favicon.ico";
            case "TIMEFOLIO" -> "https://timefolio.co.kr/images/common/favicon.ico";
            default -> "https://ssl.pstatic.net/imgstock/fn/real/logo/stock/StockCommonETF.svg";
        };
    }

    static boolean isIconAvailable(String icon) {
        try {
            URL url = new URL(icon);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.setInstanceFollowRedirects(true);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return true;
            }
        } catch (IOException ignore) {}
        return false;
    }


}
