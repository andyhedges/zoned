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

    public static RDataDom from(byte[] rdata) {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }

    @Override
    public byte[] to() {
        throw new UnsupportedOperationException("Not Implemented"); //TODO
    }
}
