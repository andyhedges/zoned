package io.hedges.zoned.netty;

import io.hedges.zoned.core.dom.DnsOpCodeDom;
import io.hedges.zoned.core.dom.DnsRecordClassDom;
import io.hedges.zoned.core.dom.DnsRecordTypeDom;
import io.hedges.zoned.core.dom.DnsResponseCodeDom;

final class DnsWireMappings {

    static DnsRecordTypeDom recordTypeFromCode(int code) {
        return DnsRecordTypeDom.fromCode(code);
    }

    static int codeForRecordType(DnsRecordTypeDom type) {
        if (type == null) {
            throw new IllegalArgumentException("record type is null");
        }
        return type.code();
    }

    static DnsRecordClassDom recordClassFromCode(int code) {
        return DnsRecordClassDom.fromCode(code);
    }

    static int codeForRecordClass(DnsRecordClassDom recordClass) {
        if (recordClass == null) {
            throw new IllegalArgumentException("record class is null");
        }
        return recordClass.code();
    }

    static DnsOpCodeDom opCodeFromCode(int code) {
        return DnsOpCodeDom.fromCode(code);
    }

    static int codeForOpCode(DnsOpCodeDom opCode) {
        if (opCode == null) {
            return 0;
        }
        return opCode.code();
    }

    static DnsResponseCodeDom responseCodeFromCode(int code) {
        return DnsResponseCodeDom.fromCode(code);
    }

    static int codeForResponseCode(DnsResponseCodeDom code) {
        if (code == null) {
            return 0;
        }
        return code.code();
    }

    private DnsWireMappings() {
    }
}
