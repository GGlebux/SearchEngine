package engine.parsing;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class PageData {
    private String path;
    private Integer code;
    private String content;
    private Set<String> links;
}
