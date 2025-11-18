package com.planti.domain.plantCatalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.planti.domain.plantCatalog.dto.CodeDto;
import com.planti.domain.plantCatalog.dto.PlantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GardenService {
    private final RestTemplate restTemplate;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Value("${nongsaro.api.key}")
    private String apiKey;

    @Value("${nongsaro.api.base}")
    private String baseUrl;

    // 공통 XML 호출 -> JsonNode(트리) 반환
    private JsonNode callXmlApi(String path, Map<String, String> params) throws Exception {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(baseUrl + path)
                .queryParam("apiKey", apiKey);

        if (params != null) {
            params.forEach(b::queryParam);
        }

        URI uri = b.build().encode().toUri();
        ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("농사로 API 호출 실패: " + uri + " 상태:" + resp.getStatusCode());
        }

        // XML -> JsonNode
        return xmlMapper.readTree(resp.getBody());
    }

    // 1) 코드 목록 공통 처리: e.g. /lightList, /grwhstleList ...
    public List<CodeDto> getCodeList(String endpoint) {
        try {
            JsonNode root = callXmlApi("/" + endpoint, null);
            // 응답 트리에서 items/item 또는 body/items/item 등 경로가 달라질 수 있으니 널 체크
            JsonNode items = findItemsNode(root);
            List<CodeDto> result = new ArrayList<>();
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    String code = safeText(item, "code");
                    String codeNm = safeText(item, "codeNm");
                    result.add(new CodeDto(code, codeNm));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("코드 목록 조회 실패: " + endpoint, e);
        }
    }

    // 2) gardenList -> PlantDto 리스트
    public List<PlantDto> getGardenList(Map<String, String> queryParams) {
        try {
            JsonNode root = callXmlApi("/gardenList", queryParams);
            JsonNode items = findItemsNode(root);
            List<PlantDto> plants = new ArrayList<>();
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    // 필드 매핑: 문서 기준 필드명을 사용
                    String name = safeText(item, "cntntsSj");
                    String scientificName = safeText(item, "plntzrNm"); // 또는 plntbneNm
                    String family = safeText(item, "fmlNm");
                    // 물주기 등은 코드값 또는 코드명으로 제공되므로 우선 codeNm 필드에 매핑 시도
                    String watering = safeText(item, "watercycleSprngCodeNm");
                    if (watering == null || watering.isBlank()) {
                        watering = safeText(item, "watercycleSummerCodeNm");
                    }
                    String temperature = safeText(item, "grwhTpCodeNm"); // 생육 온도 코드명
                    String humidity = safeText(item, "hdCodeNm");
                    String pestControl = safeText(item, "dlthtsManageInfo"); // 상세 설명이 있으면
                    String functionality = safeText(item, "fncltyInfo");
                    String specialCare = safeText(item, "speclmanageInfo");
                    String toxicity = safeText(item, "toxctyInfo");

                    PlantDto p = new PlantDto(
                            name,
                            scientificName,
                            family,
                            watering,
                            temperature,
                            humidity,
                            pestControl,
                            functionality,
                            specialCare,
                            toxicity
                    );
                    plants.add(p);
                }
            }
            return plants;
        } catch (Exception e) {
            throw new RuntimeException("gardenList 호출 실패", e);
        }
    }

    // 3) gardenDtl -> 단건 상세 (cntntsNo 필수)
    public PlantDto getGardenDetail(String cntntsNo) {
        try {
            JsonNode root = callXmlApi("/gardenDtl", Map.of("cntntsNo", cntntsNo));
            // 상세는 items/item 또는 body/items/item 에 하나의 item
            JsonNode items = findItemsNode(root);
            JsonNode item = (items != null && items.isArray() && items.size() > 0) ? items.get(0) : root;
            String name = safeText(item, "cntntsSj");
            String scientificName = safeText(item, "plntbneNm");
            String family = safeText(item, "fmlNm");
            // 물주기(사계절 코드명들 합치기)
            String watering = joinIfNotBlank(
                    safeText(item, "watercycleSprngCodeNm"),
                    safeText(item, "watercycleSummerCodeNm"),
                    safeText(item, "watercycleAutumnCodeNm"),
                    safeText(item, "watercycleWinterCodeNm")
            );
            String temperature = safeText(item, "grwhTpCodeNm");
            String humidity = safeText(item, "hdCodeNm");
            String pestControl = safeText(item, "dlthtsManageInfo");
            String functionality = safeText(item, "fncltyInfo");
            String specialCare = safeText(item, "speclmanageInfo");
            String toxicity = safeText(item, "toxctyInfo");

            return new PlantDto(
                    name,
                    scientificName,
                    family,
                    watering,
                    temperature,
                    humidity,
                    pestControl,
                    functionality,
                    specialCare,
                    toxicity
            );
        } catch (Exception e) {
            throw new RuntimeException("gardenDtl 호출 실패: " + cntntsNo, e);
        }
    }

    // ----------------- 유틸 -----------------
    private JsonNode findItemsNode(JsonNode root) {
        // 다양한 응답 폴더 구조 대응: response/body/items/item 또는 response/items/item 등
        if (root == null) return null;
        // 가장 자주 쓰이는 위치 순으로 체크
        JsonNode node;
        node = root.at("/response/body/items/item");
        if (!node.isMissingNode()) return node;
        node = root.at("/response/items/item");
        if (!node.isMissingNode()) return node;
        node = root.at("/items/item");
        if (!node.isMissingNode()) return node;
        // 혹시 items 바로 아래 배열이면
        node = root.at("/response/body/items");
        if (!node.isMissingNode() && node.isArray()) return node;
        node = root.at("/response/items");
        if (!node.isMissingNode() && node.isArray()) return node;
        // fallback: search for any "item" array in tree
        Iterator<JsonNode> iter = root.findValues("item").iterator();
        while (iter.hasNext()) {
            JsonNode cand = iter.next();
            if (cand.isArray()) return cand;
        }
        return null;
    }

    private String safeText(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) return "";
        JsonNode f = node.get(field);
        if (f == null || f.isMissingNode()) return "";
        return f.asText("");
    }

    private String joinIfNotBlank(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) {
                if (sb.length() > 0) sb.append(" / ");
                sb.append(p);
            }
        }
        return sb.toString();
    }
}