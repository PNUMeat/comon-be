package site.codemonster.comon.domain.problem.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolvedAcAPIResponse {
    private Integer problemId;
    private String titleKo;
    private Integer level;

    private List<Tag> tags;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tag {
        private String key;
        private List<DisplayName> displayNames;

        public String getKoreanName() {
            if (displayNames != null) {
                return displayNames.stream()
                        .filter(dn -> "ko".equals(dn.getLanguage()))
                        .map(DisplayName::getName)
                        .findFirst()
                        .orElse(key);
            }
            return key;
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DisplayName {
        private String language;
        private String name;
    }

    // 태그들을 쉼표로 구분된 문자열로 반환
    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return "";
        }

        return tags.stream()
                .map(Tag::getKoreanName)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
