package io.hedges.zoned.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DnsExecutionContext {
    private final DnsRequestContextDom request;
    private DnsMessageDom response;
    private final Map<String, Object> attributes = new HashMap<>();

    public DnsExecutionContext(DnsRequestContextDom request) {
        this.request = request;
    }

    public DnsRequestContextDom getRequest() {
        return request;
    }

    public Optional<DnsMessageDom> getResponse() {
        return Optional.ofNullable(response);
    }

    public void setResponse(DnsMessageDom response) {
        this.response = response;
    }

    public void putAttr(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getAttr(String key, Class<T> type) {
        Object v = attributes.get(key);
        if (type.isInstance(v)) {
            return Optional.of(type.cast(v));
        }
        return Optional.empty();
    }
}
