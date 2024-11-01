package org.oopscraft.fintics.etf.api.v1.dto;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class IconFactory {

    private static final Cache<String, String> iconCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    public static String getIcon(AssetResponse asset) {
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

        // add to cache
        if (icon != null) {
            iconCache.put(asset.getAssetId(), icon);
        }

        // returns
        return icon;
    }

    static String getUsIcon(AssetResponse asset) {
        String assetName = asset.getName();
        String etfBrand = assetName.split("\\s+")[0].toLowerCase();
        return switch (etfBrand) {
            case "spdr" -> "https://www.ssga.com/favicon.ico";
            case "global" -> "https://www.globalxetfs.com/favicon.ico";
            case "goldman" -> "https://cdn.gs.com/images/goldman-sachs/v1/gs-favicon.svg";
            case "j.p." -> "https://www.jpmorgan.com/etc.clientlibs/cws/clientlibs/clientlib-base/resources/jpm/images/jpm-favicon.ico";
            case "neos" -> "https://neosfunds.com/wp-content/uploads/cropped-NEOS-N-32x32.png";
            default -> String.format("https://s3-symbol-logo.tradingview.com/%s.svg", etfBrand);
        };
    }

    static String getKrIcon(AssetResponse asset) {
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
            case "RISE" -> "https://www.riseetf.co.kr/favicon.ico";
            case "PLUS" -> "https://www.plusetf.co.kr/static/ClientUI/images/common/icon-favicon.ico";
            default -> "https://ssl.pstatic.net/imgstock/fn/real/logo/stock/StockCommonETF.svg";
        };
    }

}