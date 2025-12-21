package io.hedges.zoned.core.dom.rdata;

final class TestBytes {

    private TestBytes() {
    }

    static byte[] concat(byte[]... chunks) {
        int total = 0;
        for (byte[] chunk : chunks) {
            total += chunk.length;
        }
        byte[] out = new byte[total];
        int idx = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, out, idx, chunk.length);
            idx += chunk.length;
        }
        return out;
    }
}
