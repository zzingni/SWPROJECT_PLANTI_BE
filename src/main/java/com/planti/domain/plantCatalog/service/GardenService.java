package com.planti.domain.plantCatalog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.planti.domain.plantCatalog.dto.CodeDto;
import com.planti.domain.plantCatalog.dto.PlantDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GardenService {
    private static final Logger log = LoggerFactory.getLogger(GardenService.class);

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
        log.debug("Calling Nongsaro API: {}", uri);
        ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            String msg = "농사로 API 호출 실패: " + uri + " 상태:" + resp.getStatusCode();
            log.error(msg);
            throw new RuntimeException(msg);
        }

        String body = resp.getBody();
        log.debug("Nongsaro response length: {}, snippet: {}", body.length(),
                body.length() > 300 ? body.substring(0, 300) + "..." : body);

        // XML -> JsonNode
        return xmlMapper.readTree(body);
    }

    // 유틸: items 노드(단일/배열) 순회 Iterator 반환
    private Iterable<JsonNode> iterateItems(JsonNode root) {
        JsonNode items = findItemsNode(root);
        if (items == null || items.isMissingNode()) return List.of();

        if (items.isArray()) {
            List<JsonNode> list = new ArrayList<>();
            items.forEach(list::add);
            return list;
        } else if (items.isObject()) {
            return List.of(items);
        } else {
            return List.of();
        }
    }

    // 1) 코드 목록 공통 처리: e.g. /lightList, /grwhstleList ...
    public List<CodeDto> getCodeList(String endpoint) {
        try {
            JsonNode root = callXmlApi("/" + endpoint, null);
            List<CodeDto> result = new ArrayList<>();
            for (JsonNode item : iterateItems(root)) {
                // TODO: 엔드포인트별 실제 태그명을 사용해야 함
                String code = safeText(item, "code");       // 필요시 바꿔라
                String codeNm = safeText(item, "codeNm");   // 필요시 바꿔라
                result.add(new CodeDto(code, codeNm));
            }
            return result;
        } catch (Exception e) {
            log.error("코드 목록 조회 실패: {}", endpoint, e);
            throw new RuntimeException("코드 목록 조회 실패: " + endpoint, e);
        }
    }

    // 2) gardenList -> PlantDto 리스트
    public List<PlantDto> getGardenList(Map<String, String> queryParams) {
        try {
            JsonNode root = callXmlApi("/gardenList", queryParams);
            List<PlantDto> plants = new ArrayList<>();
            for (JsonNode item : iterateItems(root)) {
                String id = safeText(item, "cntntsNo");
                String name = safeText(item, "cntntsSj");
                // 목록에 과학명/가족명 없을 가능성 높음 (빈값 혹은 상세 API 필요)
                String scientificName = safeText(item, "plntbneNm"); // 목록엔 없음
                String family = safeText(item, "fmlNm");

                // 이미지/캡션 분리
                List<String> imageUrls = splitToList(safeText(item, "rtnFileUrl"));
                List<String> imageCaptions = splitToList(safeText(item, "rtnImageDc"));

                PlantDto p = new PlantDto();
                p.setId(id);
                p.setName(name);
                p.setScientificName(scientificName);
                p.setFamily(family);
                // 기타 필드는 상세 API로 채우는게 안정적
                plants.add(p);
            }
            return plants;
        } catch (Exception e) {
            log.error("gardenList 호출 실패", e);
            throw new RuntimeException("gardenList 호출 실패", e);
        }
    }

    // 3) gardenDtl -> 단건 상세 (cntntsNo 필수) — 기존과 거의 동일, 안전한 fallback 유지
    public PlantDto getGardenDetail(String cntntsNo) {
        try {
            JsonNode root = callXmlApi("/gardenDtl", Map.of("cntntsNo", cntntsNo));
            JsonNode item = findFirstItemNode(root);
            if (item == null) {
                JsonNode maybeBody = root.at("/response/body");
                if (!maybeBody.isMissingNode()) item = maybeBody;
                else item = root;
            }

            PlantDto dto = new PlantDto();
            dto.setId(cntntsNo);
            dto.setName(safeText(item, "cntntsSj"));
            dto.setScientificName(safeText(item, "plntbneNm"));
            dto.setFamily(safeText(item, "fmlNm"));
            dto.setWatering(joinIfNotBlank(
                    safeText(item, "watercycleSprngCodeNm"),
                    safeText(item, "watercycleSummerCodeNm"),
                    safeText(item, "watercycleAutumnCodeNm"),
                    safeText(item, "watercycleWinterCodeNm")
            ));
            dto.setTemperature(safeText(item, "grwhTpCodeNm"));
            dto.setHumidity(safeText(item, "hdCodeNm"));
            dto.setPestControl(safeText(item, "dlthtsManageInfo"));
            dto.setFunctionality(safeText(item, "fncltyInfo"));
            dto.setSpecialCare(safeText(item, "speclmanageInfo"));
            dto.setToxicity(safeText(item, "toxctyInfo"));

            return dto;
        } catch (Exception e) {
            log.error("gardenDtl 호출 실패: {}", cntntsNo, e);
            throw new RuntimeException("gardenDtl 호출 실패: " + cntntsNo, e);
        }
    }

    // ----------------- 유틸 -----------------
    private JsonNode findItemsNode(JsonNode root) {
        if (root == null) return null;
        JsonNode node = root.at("/response/body/items/item");
        if (!node.isMissingNode() && (node.isArray() || node.isObject())) return node;
        node = root.at("/response/items/item");
        if (!node.isMissingNode() && (node.isArray() || node.isObject())) return node;
        node = root.at("/items/item");
        if (!node.isMissingNode() && (node.isArray() || node.isObject())) return node;
        node = root.at("/response/body/items");
        if (!node.isMissingNode() && node.isArray()) return node;
        node = root.at("/response/items");
        if (!node.isMissingNode() && node.isArray()) return node;

        List<JsonNode> found = root.findValues("item");
        if (!found.isEmpty()) {
            if (found.size() == 1) return found.get(0);
            return xmlMapper.valueToTree(found);
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
        if (items.isObject()) return items;
        return null;
    }

    private List<String> splitToList(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        String[] parts = raw.split("\\|");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (!p.isBlank()) out.add(p);
        }
        return out;
    }
}