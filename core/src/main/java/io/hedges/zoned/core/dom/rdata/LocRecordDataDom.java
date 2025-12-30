// SPDX-License-Identifier: Apache-2.0
package io.hedges.zoned.core.dom.rdata;

import io.hedges.zoned.core.dom.RDataDom;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain model for DNS LOC record RDATA.
 *
 * <p>RDATA is version, size, horizontal precision, vertical precision,
 * latitude, longitude, and altitude (RFC 1876).</p>
 
 *
 * <pre>
 * +-----------+--------------+----------------------------+
 * | Field     | Size (octets)| Description                |
 * +-----------+--------------+----------------------------+
 * | Version   | 1            | LOC version.               |
 * | Size      | 1            | Diameter of sphere.        |
 * | HorizPre  | 1            | Horizontal precision.      |
 * | VertPre   | 1            | Vertical precision.        |
 * | Latitude  | 4            | Latitude value.            |
 * | Longitude | 4            | Longitude value.           |
 * | Altitude  | 4            | Altitude value.            |
 * +-----------+--------------+----------------------------+
 * </pre>

 * <p>Purpose: Publishes geographic location information such as latitude, longitude, and altitude.</p>
 * <p>RFC: <a href="https://www.rfc-editor.org/rfc/rfc1876">RFC 1876</a>.</p>*/
@Getter
@Builder
@ToString
public class LocRecordDataDom implements RDataDom {
    private int version;
    private int size;
    private int horizontalPrecision;
    private int verticalPrecision;
    private long latitude;
    private long longitude;
    private long altitude;

    public static RDataDom from(byte[] rdata) {
        if (rdata == null || rdata.length != 16) {
            throw new IllegalArgumentException("LOC RDATA must be exactly 16 bytes");
        }
        int version = RDataUtils.readU8(rdata, 0);
        if (version != 0) {
            throw new IllegalArgumentException("LOC version must be 0");
        }
        int size = RDataUtils.readU8(rdata, 1);
        int horizontalPrecision = RDataUtils.readU8(rdata, 2);
        int verticalPrecision = RDataUtils.readU8(rdata, 3);
        long latitude = RDataUtils.readU32(rdata, 4);
        long longitude = RDataUtils.readU32(rdata, 8);
        long altitude = RDataUtils.readU32(rdata, 12);
        return LocRecordDataDom.builder()
                .version(version)
                .size(size)
                .horizontalPrecision(horizontalPrecision)
                .verticalPrecision(verticalPrecision)
                .latitude(latitude)
                .longitude(longitude)
                .altitude(altitude)
                .build();
    }

    @Override
    public byte[] to() {
        if (version != 0) {
            throw new IllegalArgumentException("LOC version must be 0");
        }
        if (size < 0 || size > 0xFF) {
            throw new IllegalArgumentException("LOC size must be between 0 and 255");
        }
        if (horizontalPrecision < 0 || horizontalPrecision > 0xFF) {
            throw new IllegalArgumentException("LOC horizontal precision must be between 0 and 255");
        }
        if (verticalPrecision < 0 || verticalPrecision > 0xFF) {
            throw new IllegalArgumentException("LOC vertical precision must be between 0 and 255");
        }
        validateU32(latitude, "latitude");
        validateU32(longitude, "longitude");
        validateU32(altitude, "altitude");
        byte[] out = new byte[16];
        out[0] = (byte) (version & 0xFF);
        out[1] = (byte) (size & 0xFF);
        out[2] = (byte) (horizontalPrecision & 0xFF);
        out[3] = (byte) (verticalPrecision & 0xFF);
        RDataUtils.writeU32(out, 4, latitude);
        RDataUtils.writeU32(out, 8, longitude);
        RDataUtils.writeU32(out, 12, altitude);
        return out;
    }

    private static void validateU32(long value, String field) {
        if (value < 0 || value > 0xFFFF_FFFFL) {
            throw new IllegalArgumentException("LOC " + field + " must be between 0 and 4294967295");
        }
    }
}
