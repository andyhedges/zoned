package io.hedges.zoned.app;

import io.hedges.zoned.core.DnsClient;
import io.hedges.zoned.core.DnsRequestContext;
import io.hedges.zoned.core.DnsRequestRouter;
import io.hedges.zoned.core.dom.DnsHeaderDom;
import io.hedges.zoned.core.dom.DnsMessageDom;
import io.hedges.zoned.core.dom.DnsNameDom;
import io.hedges.zoned.core.dom.DnsResourceRecordDom;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class DefaultDnsRequestRouter implements DnsRequestRouter {

    private DnsClient client;

    @Override
    public void handle(DnsRequestContext ctx) {
        String query = ctx.query().questions().getFirst().name().toFqdn();

        List<DnsResourceRecordDom> answers = new ArrayList<>();
        client.lookup(query).whenComplete((addresses, t) -> {

            addresses.forEach(a -> {
                DnsResourceRecordDom dom = DnsResourceRecordDom.builder().name(DnsNameDom.builder().labels(Arrays.stream(a.getHostName().split("\\.")).toList()).build()).build();
                answers.add(dom);
            });

            ctx.response(
                    DnsMessageDom
                            .builder()
                            .header(
                                    DnsHeaderDom
                                            .builder()
                                            .id(ctx.query().header().id())
                                            .build()
                            )
                            .answers(answers)
                            .build());
        });

    }
}
