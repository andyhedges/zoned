// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata.edns;

public class UnknownEdnsOptionDom implements EdnsOptionDom {

    private final int code;
    private final byte[] data;

    public UnknownEdnsOptionDom(int code, byte[] data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public byte[] getData() {
        return data;
    }
}
