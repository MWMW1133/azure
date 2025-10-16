package com.azure.service.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatNlpServiceHttp implements ChatNlpService {

    private static final Logger log = LoggerFactory.getLogger(ChatNlpServiceHttp.class);

    // 복수형 우선, 비어있으면 단수형, 그것도 없으면 디폴트
    @Value("${mt.libre.urls:}")
    private String urlsCsv;

    @Value("${mt.libre.url:}")
    private String singleUrl;

    // 존재하면 바디에 api_key로 같이 보냄 (LibreTranslate 규격)
    @Value("${mt.libre.apiKey:}")
    private String apiKey;

    private final RestTemplate rest = makeRest();

    private RestTemplate makeRest() {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        f.setReadTimeout((int) Duration.ofSeconds(8).toMillis());
        return new RestTemplate(f);
    }

    private List<String> resolveEndpoints() {
        List<String> list = new ArrayList<>();
        if (StringUtils.hasText(urlsCsv)) {
            list.addAll(Arrays.stream(urlsCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }
        if (list.isEmpty() && StringUtils.hasText(singleUrl)) {
            list.add(singleUrl.trim());
        }
        if (list.isEmpty()) {
            list.add("https://libretranslate.com");
        }
        return list;
    }

    @Override
    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank()) return "";

        String target = normalize(targetLang);
        List<String> endpoints = resolveEndpoints(); // properties에서 읽어온 리스트

        for (String base : endpoints) {
            String url = base.endsWith("/") ? base + "translate" : base + "/translate";
            try {
                Map<String, Object> body = new HashMap<>();
                body.put("q", text);
                body.put("source", "auto");
                body.put("target", target);
                body.put("format", "text");
                if (StringUtils.hasText(apiKey)) body.put("api_key", apiKey);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

                ResponseEntity<Map> resp = rest.postForEntity(url, req, Map.class);

                // 3xx → Location으로 '한 번만' 재-POST
                if (resp.getStatusCode().is3xxRedirection() && resp.getHeaders().getLocation() != null) {
                    String loc = resp.getHeaders().getLocation().toString();
                    String redir = loc.contains("/translate")
                            ? loc
                            : (loc.endsWith("/") ? loc + "translate" : loc + "/translate");
                    log.debug("[mt] retry POST -> {}", redir);
                    resp = rest.postForEntity(redir, req, Map.class);
                }

                if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                    Object t = resp.getBody().get("translatedText");
                    if (t instanceof String s && !s.isBlank()) return s;
                }

                log.warn("[mt] unexpected resp via {}: status={}, body={}",
                        base, resp.getStatusCode(), resp.getBody());
            } catch (Exception e) {
                log.warn("[mt] fail via {} ({}), trying next…", base, e.toString());
            }
        }
        return text; // 전부 실패하면 원문 유지
    }

    @Override
    public String summarize(String text) {
        if (text == null || text.isBlank()) return "";
        return text.length() > 40 ? text.substring(0, 40) + "..." : text;
    }

    private String normalize(String t) {
        if (t == null) return "en";
        String s = t.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "en", "english", "영어" -> "en";
            case "ko", "korean", "한국어", "한글" -> "ko";
            case "ja", "japanese", "일본어" -> "ja";
            case "zh", "chinese", "중국어", "zh-cn", "cn" -> "zh";
            default -> "en";
        };
    }
}
