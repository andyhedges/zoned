package io.hedges.zoned.core.dom;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@ToString
public class DnsNameDom {
    private List<String> labels;

    public String toFqdn(){
        return String.join(".", labels);
    }

    public static DnsNameDom fromFqdn(String fqdn) {
        return DnsNameDom.builder().labels(Arrays.stream(fqdn.split("\\.")).toList()).build();
    }
}
