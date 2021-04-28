/*
 * Sux4J: Succinct data structures for Java
 *
 * Copyright (C) 2008-2016 Sebastiano Vigna
 *
 *  This library is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package com.github.fastfilter.bloom.count;

/**
 * A select implementation with guaranteed O(1) query time. This is a copy of
 * the (very good) implementation in Sux4J it.unimi.dsi.sux4j.bits.SimpleSelect
 * by Sebastiano Vigna (see copyright), with a custom serialization format.
 */
public class Select {

    private static final long ONES_STEP_4 = 0x1111111111111111L;
    private static final long ONES_STEP_8 = 0x0101010101010101L;
    private static final long MSBS_STEP_8 = 0x80L * ONES_STEP_8;

    private static final byte[] SELECT_IN_BYTE = {
            -1, 0, 1, 0, 2, 0, 1, 0, 3,
            0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1,
            0, 5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2,
            0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1,
            0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5,
            0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1,
            0, 3, 0, 1, 0, 2, 0, 1, 0, 7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2,
            0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1,
            0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3,
            0, 1, 0, 2, 0, 1, 0, 6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1,
            0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 5, 0, 1, 0, 2,
            0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0, 4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1,
            0, 2, 0, 1, 0, -1, -1, -1, 1, -1, 2, 2, 1, -1, 3, 3, 1, 3, 2, 2, 1,
            -1, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, -1, 5, 5, 1, 5, 2,
            2, 1, 5, 3, 3, 1, 3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1,
            3, 2, 2, 1, -1, 6, 6, 1, 6, 2, 2, 1, 6, 3, 3, 1, 3, 2, 2, 1, 6, 4,
            4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, 6, 5, 5, 1, 5, 2, 2, 1,
            5, 3, 3, 1, 3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2,
            2, 1, -1, 7, 7, 1, 7, 2, 2, 1, 7, 3, 3, 1, 3, 2, 2, 1, 7, 4, 4, 1,
            4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, 7, 5, 5, 1, 5, 2, 2, 1, 5, 3,
            3, 1, 3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1,
            7, 6, 6, 1, 6, 2, 2, 1, 6, 3, 3, 1, 3, 2, 2, 1, 6, 4, 4, 1, 4, 2,
            2, 1, 4, 3, 3, 1, 3, 2, 2, 1, 6, 5, 5, 1, 5, 2, 2, 1, 5, 3, 3, 1,
            3, 2, 2, 1, 5, 4, 4, 1, 4, 2, 2, 1, 4, 3, 3, 1, 3, 2, 2, 1, -1, -1,
            -1, -1, -1, -1, -1, 2, -1, -1, -1, 3, -1, 3, 3, 2, -1, -1, -1, 4,
            -1, 4, 4, 2, -1, 4, 4, 3, 4, 3, 3, 2, -1, -1, -1, 5, -1, 5, 5, 2,
            -1, 5, 5, 3, 5, 3, 3, 2, -1, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3,
            3, 2, -1, -1, -1, 6, -1, 6, 6, 2, -1, 6, 6, 3, 6, 3, 3, 2, -1, 6,
            6, 4, 6, 4, 4, 2, 6, 4, 4, 3, 4, 3, 3, 2, -1, 6, 6, 5, 6, 5, 5, 2,
            6, 5, 5, 3, 5, 3, 3, 2, 6, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3,
            3, 2, -1, -1, -1, 7, -1, 7, 7, 2, -1, 7, 7, 3, 7, 3, 3, 2, -1, 7,
            7, 4, 7, 4, 4, 2, 7, 4, 4, 3, 4, 3, 3, 2, -1, 7, 7, 5, 7, 5, 5, 2,
            7, 5, 5, 3, 5, 3, 3, 2, 7, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3,
            3, 2, -1, 7, 7, 6, 7, 6, 6, 2, 7, 6, 6, 3, 6, 3, 3, 2, 7, 6, 6, 4,
            6, 4, 4, 2, 6, 4, 4, 3, 4, 3, 3, 2, 7, 6, 6, 5, 6, 5, 5, 2, 6, 5,
            5, 3, 5, 3, 3, 2, 6, 5, 5, 4, 5, 4, 4, 2, 5, 4, 4, 3, 4, 3, 3, 2,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 3, -1,
            -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, 4, -1, 4, 4, 3, -1, -1, -1,
            -1, -1, -1, -1, 5, -1, -1, -1, 5, -1, 5, 5, 3, -1, -1, -1, 5, -1,
            5, 5, 4, -1, 5, 5, 4, 5, 4, 4, 3, -1, -1, -1, -1, -1, -1, -1, 6,
            -1, -1, -1, 6, -1, 6, 6, 3, -1, -1, -1, 6, -1, 6, 6, 4, -1, 6, 6,
            4, 6, 4, 4, 3, -1, -1, -1, 6, -1, 6, 6, 5, -1, 6, 6, 5, 6, 5, 5, 3,
            -1, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, -1, -1, -1, -1,
            -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 3, -1, -1, -1, 7, -1, 7, 7,
            4, -1, 7, 7, 4, 7, 4, 4, 3, -1, -1, -1, 7, -1, 7, 7, 5, -1, 7, 7,
            5, 7, 5, 5, 3, -1, 7, 7, 5, 7, 5, 5, 4, 7, 5, 5, 4, 5, 4, 4, 3, -1,
            -1, -1, 7, -1, 7, 7, 6, -1, 7, 7, 6, 7, 6, 6, 3, -1, 7, 7, 6, 7, 6,
            6, 4, 7, 6, 6, 4, 6, 4, 4, 3, -1, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5,
            6, 5, 5, 3, 7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 4, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, -1, -1, -1, -1, -1,
            -1, -1, 5, -1, -1, -1, 5, -1, 5, 5, 4, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, -1, -1, 6,
            -1, -1, -1, 6, -1, 6, 6, 4, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1,
            -1, 6, -1, 6, 6, 5, -1, -1, -1, 6, -1, 6, 6, 5, -1, 6, 6, 5, 6, 5,
            5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            7, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 4, -1,
            -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 5, -1, -1, -1,
            7, -1, 7, 7, 5, -1, 7, 7, 5, 7, 5, 5, 4, -1, -1, -1, -1, -1, -1,
            -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1, 7, -1, 7, 7, 6, -1,
            7, 7, 6, 7, 6, 6, 4, -1, -1, -1, 7, -1, 7, 7, 6, -1, 7, 7, 6, 7, 6,
            6, 5, -1, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5, 6, 5, 5, 4, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 5, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, 6, -1, -1, -1, -1, -1, -1, -1, 6, -1, -1,
            -1, 6, -1, 6, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 7, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 5,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1,
            -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1,
            -1, -1, -1, -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1, 7, -1,
            7, 7, 6, -1, 7, 7, 6, 7, 6, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 7, -1, -1, -1, -1, -1, -1,
            -1, 7, -1, -1, -1, 7, -1, 7, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, 7
        };

    /**
     * Get the position of the n-th bit. Position 0 is the first bit (e.g. x = 1).
     * Undefined if n >= number of bits in x.
     *
     * @param x the value
     * @param n the bit
     * @return the position (0 for first bit, 63 for last)
     */
    public static int selectInLong(long x, int n) {
        // TODO this adds bytecode weight which influence inlining decisions
        assert n < Long.bitCount(x): n + " >= " + Long.bitCount(x);
        // Phase 1: sums by byte
        long byteSums = x - ((x & 0xa * ONES_STEP_4) >>> 1);
        byteSums = (byteSums & 3 * ONES_STEP_4) +
                ((byteSums >>> 2) & 3 * ONES_STEP_4);
        byteSums = (byteSums + (byteSums >>> 4)) & 0x0f * ONES_STEP_8;
        byteSums *= ONES_STEP_8;
        // Phase 2: compare each byte sum with rank to obtain the relevant byte
        long rankStep8 = n * ONES_STEP_8;
        int byteOffset = (int) (((((rankStep8 | MSBS_STEP_8) - byteSums) & MSBS_STEP_8) >>> 7) *
                ONES_STEP_8 >>> 53) &
                ~0x7;
        int byteRank = (int) (n - (((byteSums << 8) >>> byteOffset) & 0xFF));
        return byteOffset +
                SELECT_IN_BYTE[(int) (x >>> byteOffset & 0xFF) | byteRank << 8];
    }

}
