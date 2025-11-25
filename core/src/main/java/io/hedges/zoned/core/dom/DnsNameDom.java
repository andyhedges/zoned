package io.hedges.zoned.core.dom;

import java.util.List;

public class DnsNameDom {
    private List<String> labels;

    private String toFqdn(){
        return String.join(".", labels);
    }
}
