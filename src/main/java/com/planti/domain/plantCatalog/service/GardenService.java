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

            JsonNode item = findFirstItemNode(root);
            // fallback: 응답 구조가 item 없이 바로 필드를 내려주는 경우(rare)
            if (item == null) {
                // response/body 또는 response 아래에서 직접 필드 찾기 시도
                JsonNode maybeBody = root.at("/response/body");
                if (!maybeBody.isMissingNode()) item = maybeBody;
                else item = root; // 최후의 수단
            }

            String name = safeText(item, "cntntsSj");
            String scientificName = safeText(item, "plntbneNm");
            String family = safeText(item, "fmlNm");
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
        if (root == null) return null;

        // 흔한 위치 우선 체크
        JsonNode node = root.at("/response/body/items/item");
        if (!node.isMissingNode() && (node.isArray() || node.isObject())) {
            // object인 경우에도 배열처럼 다루기 위해 그대로 반환 (caller가 체크)
            return node;
        }

        node = root.at("/response/items/item");
        if (!node.isMissingNode() && (node.isArray() || node.isObject())) return node;

        node = root.at("/items/item");
        if (!node.isMissingNode() && (node.isArray() || node.isObject())) return node;

        // 때때로 items가 배열이 아니라 바로 item 태그들이 여러개로 들어오는 경우 처리
        node = root.at("/response/body/items");
        if (!node.isMissingNode() && node.isArray()) return node;

        node = root.at("/response/items");
        if (!node.isMissingNode() && node.isArray()) return node;

        // fallback: findAny 'item' occurrences (list of nodes)
        List<JsonNode> found = root.findValues("item");
        if (!found.isEmpty()) {
            // 만약 복수개의 item 객체가 있으면 배열로 합쳐서 반환
            // 만약 단일 item이면 그 node를 그대로 반환
            if (found.size() == 1) return found.get(0);
            ArrayList<JsonNode> arr = new ArrayList<>(found);
            // convert to ArrayNode could be done but simple approach: return first for list-processing callers
            // 여기서는 호출부에서 isArray/isObject 체크하도록 설계
            return xmlMapper.valueToTree(arr); // JsonNode 배열로 반환
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

    private JsonNode findFirstItemNode(JsonNode root) {
        JsonNode items = findItemsNode(root);
        if (items == null) return null;
        if (items.isArray() && items.size() > 0) return items.get(0);
        // items가 object(단일 item)인 경우
        if (items.isObject()) return items;
        return null;
    }
}