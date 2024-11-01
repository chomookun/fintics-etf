package org.oopscraft.fintics.etf.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.etf.dao.*;
import org.oopscraft.fintics.etf.model.Asset;
import org.oopscraft.fintics.etf.model.Dividend;
import org.oopscraft.fintics.etf.model.Ohlcv;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KrMarketCollector extends AbstractMarketCollector {

    public KrMarketCollector(
            PlatformTransactionManager transactionManager,
            AssetRepository assetRepository,
            DividendRepository dividendRepository,
            OhlcvRepository ohlcvRepository
    ) {
        super(transactionManager, assetRepository, dividendRepository, ohlcvRepository);
    }

    @Scheduled(initialDelay = 10_000, fixedDelay = 60_000 * 60 * 24)
    void schedule() {
        collect();
    }

    @Override
    List<Asset> getAssets() {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06025V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "secnIssuStatPList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfSetredInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("MENU_NO","174");
            put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,detail,seach,searchIcon,comparison,link,link,wide,wide,top,");
            put("mngco", "");
            put("SETUP_DT", "");
            put("from_TOT_RECM_RATE", "");
            put("to_TOT_RECM_RATE", "");
            put("from_NETASST_TOTAMT", "");
            put("to_NETASST_TOTAMT", "");
            put("kor_SECN_NM", "");
            put("ic4_select", "2");
            put("select_sorting", "2");
            put("START_PAGE", "1");
            put("END_PAGE", "10000");
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);

        // sort
        rows.sort((o1, o2) -> {
            BigDecimal o1MarketCap = toNumber(o1.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            BigDecimal o2MarketCap = toNumber(o2.get("NETASST_TOTAMT"), BigDecimal.ZERO);
            return o2MarketCap.compareTo(o1MarketCap);
        });

        // convert assets
        return rows.stream()
                .map(row -> {
                    // market cap (etf is 1 krw unit)
                    BigDecimal marketCap = toNumber(row.get("NETASST_TOTAMT"), null);
                    if(marketCap != null) {
                        marketCap = marketCap.divide(BigDecimal.valueOf(100_000_000), MathContext.DECIMAL32)
                                .setScale(0, RoundingMode.HALF_UP);
                    }

                    // return
                    return Asset.builder()
                            .assetId(toAssetId("KR", row.get("SHOTN_ISIN")))
                            .name(row.get("KOR_SECN_NM"))
                            .market("KR")
                            .exchange("XRKX")
                            .marketCap(marketCap)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06030V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "exerInfoDtramtPayStatPlist";
        String task = "ksd.safe.bip.cnts.etf.process.EtfExerInfoPTask";
        Map<String,String> secInfo = getSecInfo(asset.getSymbol());
        String isin = secInfo.get("ISIN");
        List<Dividend> dividends = new ArrayList<>();
        int startPage = 1;
        int endPage = 30;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("MENU_NO","179");
                put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,searchIcon,searchIcon,seach,searchIcon,seach,link,link,wide,wide,top,");
                put("isin", isin);
                put("RGT_RSN_DTAIL_SORT_CD", "11");
                put("fromRGT_STD_DT", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("toRGT_STD_DT", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("START_PAGE", String.valueOf(finalStartPage));
                put("END_PAGE", String.valueOf(finalEndPage));
            }};
            String payloadXml = createSeibroPayloadXml(action, task, payloadMap);

            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);

            String responseBody = responseEntity.getBody();
            List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);

            List<Dividend> dividendPage = rows.stream()
                    .map(row -> {
                        return Dividend.builder()
                                .assetId(asset.getAssetId())
                                .date(LocalDate.parse(row.get("RGT_STD_DT"), DateTimeFormatter.BASIC_ISO_DATE))
                                .amount(new BigDecimal(row.get("ESTM_STDPRC")))
                                .build();
                    })
                    .collect(Collectors.toList());
            dividends.addAll(dividendPage);

            // check next page
            if (rows.size() < 30) {
                break;
            } else {
                startPage += 30;
                endPage += 30;
            }
        }
        // returns
        return dividends;
    }

    @Override
    List<Ohlcv> getOhlcvs(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/etf/BIP_CNTS06033V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "compstInfoStkDayprcList";
        String task = "ksd.safe.bip.cnts.etf.process.EtfCompstInfoPTask";
        Map<String,String> secInfo = getSecInfo(asset.getSymbol());
        String isin = secInfo.get("ISIN");
        List<Ohlcv> ohlcvs = new ArrayList<>();
        int startPage = 1;
        int endPage = 100;
        for (int i = 0; i < 100; i ++) {
            int finalStartPage = startPage;
            int finalEndPage = endPage;
            Map<String,String> payloadMap = new LinkedHashMap<>(){{
                put("W2XPATH", w2xPath);
                put("MENU_NO","182");
                put("CMM_BTN_ABBR_NM","allview,allview,print,hwp,word,pdf,searchIcon,seach,favorites float_left,search02,search02,link,link,wide,wide,top,");
                put("isin", isin);
                put("RGT_RSN_DTAIL_SORT_CD", "11");
                put("fromDt", dateFrom.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("toDt", dateTo.format(DateTimeFormatter.BASIC_ISO_DATE));
                put("START_PAGE", String.valueOf(finalStartPage));
                put("END_PAGE", String.valueOf(finalEndPage));
            }};
            String payloadXml = createSeibroPayloadXml(action, task, payloadMap);

            RequestEntity<String> requestEntity = RequestEntity.post(url)
                    .headers(headers)
                    .body(payloadXml);
            ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);

            String responseBody = responseEntity.getBody();
            List<Map<String, String>> rows = convertSeibroXmlToList(responseBody);

            List<Ohlcv> ohlcvPage = rows.stream()
                    .map(row -> {
                        return Ohlcv.builder()
                                .assetId(asset.getAssetId())
                                .date(LocalDate.parse(row.get("STD_DT"), DateTimeFormatter.BASIC_ISO_DATE))
                                .open(new BigDecimal(row.get("MARTP")))
                                .high(new BigDecimal(row.get("HGPRC")))
                                .low(new BigDecimal(row.get("LWPRC")))
                                .close(new BigDecimal(row.get("CPRI")))
                                .volume(new BigDecimal(row.get("TR_QTY")))
                                .build();
                    })
                    .collect(Collectors.toList());
            ohlcvs.addAll(ohlcvPage);

            // check next page
            if (rows.size() < 100) {
                break;
            } else {
                startPage += 100;
                endPage += 100;
            }
        }

        // returns
        return ohlcvs;
    }

    /**
     * creates seibro header
     * @param w2xPath w2xPath
     * @return http headers
     */
    HttpHeaders createSeibroHeaders(String w2xPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/xml");
        headers.add("Origin","https://seibro.or.kr");
        headers.add("Referer","https://seibro.or.kr/websquare/control.jsp?w2xPath=" + w2xPath);
        return headers;
    }

    /**
     * gets SEC info
     * @param symbol symbol
     * @return sec info
     */
    Map<String, String> getSecInfo(String symbol) {
        String url = "https://seibro.or.kr/websquare/engine/proworks/callServletService.jsp";
        String w2xPath = "/IPORTAL/user/stock/BIP_CNTS02006V.xml";
        HttpHeaders headers = createSeibroHeaders(w2xPath);
        headers.setContentType(MediaType.APPLICATION_XML);
        String action = "secnInfoDefault";
        String task = "ksd.safe.bip.cnts.Stock.process.SecnInfoPTask";
        Map<String,String> payloadMap = new LinkedHashMap<>(){{
            put("W2XPATH", w2xPath);
            put("SHOTN_ISIN", symbol);
        }};
        String payloadXml = createSeibroPayloadXml(action, task, payloadMap);
        RequestEntity<String> requestEntity = RequestEntity.post(url)
                .headers(headers)
                .body(payloadXml);
        ResponseEntity<String> responseEntity = getRestTemplate().exchange(requestEntity, String.class);
        String responseBody = responseEntity.getBody();
        return convertSeibroXmlToMap(responseBody);
    }

    /**
     * creates payload XML string
     * @param action seibro api action
     * @param task seibro api task
     * @param payloadMap payload map
     * @return payload XML string
     */
    String createSeibroPayloadXml(String action, String task, Map<String,String> payloadMap) {
        // Create a new Document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder ;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        }catch(Throwable e){
            throw new RuntimeException(e);
        }
        Document doc = dBuilder.newDocument();

        // Create the root element <reqParam>
        Element reqParamElement = doc.createElement("reqParam");
        doc.appendChild(reqParamElement);

        // Add attributes to <reqParam>
        Attr actionAttr = doc.createAttribute("action");
        actionAttr.setValue(action);
        reqParamElement.setAttributeNode(actionAttr);

        Attr taskAttr = doc.createAttribute("task");
        taskAttr.setValue(task);
        reqParamElement.setAttributeNode(taskAttr);

        // Add child elements to <reqParam>
        for(String key : payloadMap.keySet()) {
            String value = payloadMap.get(key);
            Element childElement = doc.createElement(key);
            Attr attr = doc.createAttribute("value");
            attr.setValue(value);
            childElement.setAttributeNode(attr);
            reqParamElement.appendChild(childElement);
        }

        // convert to string
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(reqParamElement), new StreamResult(writer));
            return writer.toString();
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * convert seibro xml response to map
     * @param responseXml response xml
     * @return map
     */
    Map<String, String> convertSeibroXmlToMap(String responseXml) {
        Map<String, String> map  = new LinkedHashMap<>();
        InputSource inputSource;
        StringReader stringReader;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            stringReader = new StringReader(responseXml);
            inputSource = new InputSource(stringReader);
            Document document = builder.parse(inputSource);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            XPathExpression expr = xPath.compile("/result/*");
            NodeList propertyNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for(int i = 0; i < propertyNodes.getLength(); i++) {
                Element propertyElement = (Element) propertyNodes.item(i);
                String propertyName = propertyElement.getTagName();
                String propertyValue = propertyElement.getAttribute("value");
                map.put(propertyName, propertyValue);
            }
        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * convert seibro response XML to list
     * @param responseXml response XML
     * @return list of seibro response map
     */
    List<Map<String, String>> convertSeibroXmlToList(String responseXml) {
        List<Map<String,String>> list = new ArrayList<>();
        InputSource inputSource;
        StringReader stringReader;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            stringReader = new StringReader(responseXml);
            inputSource = new InputSource(stringReader);
            Document document = builder.parse(inputSource);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            Double count = (Double) xPath.evaluate("count(//vector)", document, XPathConstants.NUMBER);
            if(count.intValue() == 0) {
                throw new RuntimeException("response body error - vector element count is 0.");
            }

            XPathExpression expr = xPath.compile("//vector/data/result");
            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++) {
                Map<String, String> map = new LinkedHashMap<>();
                Node result = nodeList.item(i);
                NodeList propertyNodes = result.getChildNodes();
                for(int ii = 0; ii < propertyNodes.getLength(); ii++) {
                    Element propertyElement = (Element) propertyNodes.item(ii);
                    String propertyName = propertyElement.getTagName();
                    String propertyValue = propertyElement.getAttribute("value");
                    map.put(propertyName, propertyValue);
                }
                list.add(map);
            }

        }catch(Throwable e) {
            throw new RuntimeException(e);
        }
        return list;
    }



}
