/*
 * Copyright (c) 2018, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @modules jdk.incubator.vector
 * @run testng/othervm -ea -esa -Xbatch -XX:-TieredCompilation Long128VectorTests
 */

// -- This file was mechanically generated: Do not edit! -- //

import jdk.incubator.vector.VectorShape;
import jdk.incubator.vector.VectorSpecies;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.Vector;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.ShortVector;
import jdk.incubator.vector.LongVector;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.Integer;
import java.util.List;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class Long128VectorTests extends AbstractVectorTest {

    static final VectorSpecies<Long> SPECIES =
                LongVector.SPECIES_128;

    static final int INVOC_COUNT = Integer.getInteger("jdk.incubator.vector.test.loop-iterations", 100);


    static final int BUFFER_REPS = Integer.getInteger("jdk.incubator.vector.test.buffer-vectors", 25000 / 128);

    static final int BUFFER_SIZE = Integer.getInteger("jdk.incubator.vector.test.buffer-size", BUFFER_REPS * (128 / 8));

    interface FUnOp {
        long apply(long a);
    }

    static void assertArraysEquals(long[] r, long[] a, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i]), "at index #" + i + ", input = " + a[i]);
        }
    }

    interface FUnArrayOp {
        long[] apply(long a);
    }

    static void assertArraysEquals(long[] r, long[] a, FUnArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a[i]));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a[i]);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, boolean[] mask, FUnOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], mask[i % SPECIES.length()] ? f.apply(a[i]) : a[i], "at index #" + i + ", input = " + a[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    interface FReductionOp {
        long apply(long[] a, int idx);
    }

    interface FReductionAllOp {
        long apply(long[] a);
    }

    static void assertReductionArraysEquals(long[] r, long rc, long[] a,
                                            FReductionOp f, FReductionAllOp fa) {
        int i = 0;
        try {
            Assert.assertEquals(rc, fa.apply(a));
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(rc, fa.apply(a), "Final result is incorrect!");
            Assert.assertEquals(r[i], f.apply(a, i), "at index #" + i);
        }
    }

    interface FReductionMaskedOp {
        long apply(long[] a, int idx, boolean[] mask);
    }

    interface FReductionAllMaskedOp {
        long apply(long[] a, boolean[] mask);
    }

    static void assertReductionArraysEqualsMasked(long[] r, long rc, long[] a, boolean[] mask,
                                            FReductionMaskedOp f, FReductionAllMaskedOp fa) {
        int i = 0;
        try {
            Assert.assertEquals(rc, fa.apply(a, mask));
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(r[i], f.apply(a, i, mask));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(rc, fa.apply(a, mask), "Final result is incorrect!");
            Assert.assertEquals(r[i], f.apply(a, i, mask), "at index #" + i);
        }
    }


    interface FBoolReductionOp {
        boolean apply(boolean[] a, int idx);
    }

    static void assertReductionBoolArraysEquals(boolean[] r, boolean[] a, FBoolReductionOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a, i), "at index #" + i);
        }
    }

    static void assertInsertArraysEquals(long[] r, long[] a, long element, int index) {
        int i = 0;
        try {
            for (; i < a.length; i += 1) {
                if(i%SPECIES.length() == index) {
                    Assert.assertEquals(r[i], element);
                } else {
                    Assert.assertEquals(r[i], a[i]);
                }
            }
        } catch (AssertionError e) {
            if (i%SPECIES.length() == index) {
                Assert.assertEquals(r[i], element, "at index #" + i);
            } else {
                Assert.assertEquals(r[i], a[i], "at index #" + i);
            }
        }
    }

    static void assertRearrangeArraysEquals(long[] r, long[] a, int[] order, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    Assert.assertEquals(r[i+j], a[i+order[i+j]]);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            Assert.assertEquals(r[i+j], a[i+order[i+j]], "at index #" + idx + ", input = " + a[i+order[i+j]]);
        }
    }

    static void assertSelectFromArraysEquals(long[] r, long[] a, long[] order, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    Assert.assertEquals(r[i+j], a[i+(int)order[i+j]]);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            Assert.assertEquals(r[i+j], a[i+(int)order[i+j]], "at index #" + idx + ", input = " + a[i+(int)order[i+j]]);
        }
    }

    static void assertRearrangeArraysEquals(long[] r, long[] a, int[] order, boolean[] mask, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    if (mask[j % SPECIES.length()])
                         Assert.assertEquals(r[i+j], a[i+order[i+j]]);
                    else
                         Assert.assertEquals(r[i+j], (long)0);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            if (mask[j % SPECIES.length()])
                Assert.assertEquals(r[i+j], a[i+order[i+j]], "at index #" + idx + ", input = " + a[i+order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
            else
                Assert.assertEquals(r[i+j], (long)0, "at index #" + idx + ", input = " + a[i+order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
        }
    }

    static void assertSelectFromArraysEquals(long[] r, long[] a, long[] order, boolean[] mask, int vector_len) {
        int i = 0, j = 0;
        try {
            for (; i < a.length; i += vector_len) {
                for (j = 0; j < vector_len; j++) {
                    if (mask[j % SPECIES.length()])
                         Assert.assertEquals(r[i+j], a[i+(int)order[i+j]]);
                    else
                         Assert.assertEquals(r[i+j], (long)0);
                }
            }
        } catch (AssertionError e) {
            int idx = i + j;
            if (mask[j % SPECIES.length()])
                Assert.assertEquals(r[i+j], a[i+(int)order[i+j]], "at index #" + idx + ", input = " + a[i+(int)order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
            else
                Assert.assertEquals(r[i+j], (long)0, "at index #" + idx + ", input = " + a[i+(int)order[i+j]] + ", mask = " + mask[j % SPECIES.length()]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a) {
        int i = 0;
        for (; i < a.length; i += SPECIES.length()) {
            int idx = i;
            for (int j = idx; j < (idx + SPECIES.length()); j++)
                a[j]=a[idx];
        }

        try {
            for (i = 0; i < a.length; i++) {
                Assert.assertEquals(r[i], a[i]);
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], a[i], "at index #" + i + ", input = " + a[i]);
        }
    }

    interface FBinOp {
        long apply(long a, long b);
    }

    interface FBinMaskOp {
        long apply(long a, long b, boolean m);

        static FBinMaskOp lift(FBinOp f) {
            return (a, b, m) -> m ? f.apply(a, b) : a;
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i]), "(" + a[i] + ", " + b[i] + ") at index #" + i);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()]),
                                "(" + a[i] + ", " + b[(i / SPECIES.length()) * SPECIES.length()] + ") at index #" + i);
        }
    }

    static void assertBroadcastLongArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()])));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()])),
                                "(" + a[i] + ", " + b[(i / SPECIES.length()) * SPECIES.length()] + ") at index #" + i);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertBroadcastArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] +
                                ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }

    static void assertBroadcastLongArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertBroadcastLongArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertBroadcastLongArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()]), mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], (long)((long)b[(i / SPECIES.length()) * SPECIES.length()]),
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] +
                                ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }

    static void assertShiftArraysEquals(long[] r, long[] a, long[] b, FBinOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]));
                }
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j]), "at index #" + i + ", " + j);
        }
    }

    static void assertShiftArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinOp f) {
        assertShiftArraysEquals(r, a, b, mask, FBinMaskOp.lift(f));
    }

    static void assertShiftArraysEquals(long[] r, long[] a, long[] b, boolean[] mask, FBinMaskOp f) {
        int i = 0;
        int j = 0;
        try {
            for (; j < a.length; j += SPECIES.length()) {
                for (i = 0; i < SPECIES.length(); i++) {
                    Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j], mask[i]));
                }
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i+j], f.apply(a[i+j], b[j], mask[i]), "at index #" + i + ", input1 = " + a[i+j] + ", input2 = " + b[j] + ", mask = " + mask[i]);
        }
    }

    interface FTernOp {
        long apply(long a, long b, long c);
    }

    interface FTernMaskOp {
        long apply(long a, long b, long c, boolean m);

        static FTernMaskOp lift(FTernOp f) {
            return (a, b, c, m) -> m ? f.apply(a, b, c) : a;
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " + c[i]);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask, FTernOp f) {
        assertArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask, FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[i], mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = "
              + b[i] + ", input3 = " + c[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()]), "at index #" +
                                i + ", input1 = " + a[i] + ", input2 = " + b[i] + ", input3 = " +
                                c[(i / SPECIES.length()) * SPECIES.length()]);
        }
    }

    static void assertAltBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i]), "at index #" +
                                i + ", input1 = " + a[i] + ", input2 = " +
                                b[(i / SPECIES.length()) * SPECIES.length()] + ",  input3 = " + c[i]);
        }
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernOp f) {
        assertBroadcastArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()],
                                    mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[i], c[(i / SPECIES.length()) * SPECIES.length()],
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] + ", input2 = " +
                                b[i] + ", input3 = " + c[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }

    static void assertAltBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernOp f) {
        assertAltBroadcastArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertAltBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                            FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i],
                                    mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()], c[i],
                                mask[i % SPECIES.length()]), "at index #" + i + ", input1 = " + a[i] +
                                ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] +
                                ", input3 = " + c[i] + ", mask = " + mask[i % SPECIES.length()]);
        }
    }

    static void assertDoubleBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, FTernOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                    c[(i / SPECIES.length()) * SPECIES.length()]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                c[(i / SPECIES.length()) * SPECIES.length()]), "at index #" + i + ", input1 = " + a[i]
                                + ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] + ", input3 = " +
                                c[(i / SPECIES.length()) * SPECIES.length()]);
        }
    }

    static void assertDoubleBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                                  FTernOp f) {
        assertDoubleBroadcastArraysEquals(r, a, b, c, mask, FTernMaskOp.lift(f));
    }

    static void assertDoubleBroadcastArraysEquals(long[] r, long[] a, long[] b, long[] c, boolean[] mask,
                                                  FTernMaskOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                    c[(i / SPECIES.length()) * SPECIES.length()], mask[i % SPECIES.length()]));
            }
        } catch (AssertionError err) {
            Assert.assertEquals(r[i], f.apply(a[i], b[(i / SPECIES.length()) * SPECIES.length()],
                                c[(i / SPECIES.length()) * SPECIES.length()], mask[i % SPECIES.length()]), "at index #"
                                + i + ", input1 = " + a[i] + ", input2 = " + b[(i / SPECIES.length()) * SPECIES.length()] +
                                ", input3 = " + c[(i / SPECIES.length()) * SPECIES.length()] + ", mask = " +
                                mask[i % SPECIES.length()]);
        }
    }



    interface FBinArrayOp {
        long apply(long[] a, int b);
    }

    static void assertArraysEquals(long[] r, long[] a, FBinArrayOp f) {
        int i = 0;
        try {
            for (; i < a.length; i++) {
                Assert.assertEquals(r[i], f.apply(a, i));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], f.apply(a,i), "at index #" + i);
        }
    }

    interface FGatherScatterOp {
        long[] apply(long[] a, int ix, int[] b, int iy);
    }

    static void assertArraysEquals(long[] r, long[] a, int[] b, FGatherScatterOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, b, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, i, b, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + " at index #" + i);
        }
    }

    interface FGatherMaskedOp {
        long[] apply(long[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    interface FScatterMaskedOp {
        long[] apply(long[] r, long[] a, int ix, boolean[] mask, int[] b, int iy);
    }

    static void assertArraysEquals(long[] r, long[] a, int[] b, boolean[] mask, FGatherMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, i, mask, b, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + ", mask: "
              + Arrays.toString(mask)
              + " at index #" + i);
        }
    }

    static void assertArraysEquals(long[] r, long[] a, int[] b, boolean[] mask, FScatterMaskedOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(r, a, i, mask, b, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(r, a, i, mask, b, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref,
              "(ref: " + Arrays.toString(ref) + ", res: " + Arrays.toString(res) + ", a: "
              + Arrays.toString(Arrays.copyOfRange(a, i, i+SPECIES.length()))
              + ", b: "
              + Arrays.toString(Arrays.copyOfRange(b, i, i+SPECIES.length()))
              + ", r: "
              + Arrays.toString(Arrays.copyOfRange(r, i, i+SPECIES.length()))
              + ", mask: "
              + Arrays.toString(mask)
              + " at index #" + i);
        }
    }

    interface FLaneOp {
        long[] apply(long[] a, int origin, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, int origin, FLaneOp f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, origin, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, origin, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i);
        }
    }

    interface FLaneBop {
        long[] apply(long[] a, long[] b, int origin, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, FLaneBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin);
        }
    }

    interface FLaneMaskedBop {
        long[] apply(long[] a, long[] b, int origin, boolean[] mask, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, boolean[] mask, FLaneMaskedBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, mask, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, mask, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin);
        }
    }

    interface FLanePartBop {
        long[] apply(long[] a, long[] b, int origin, int part, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, int part, FLanePartBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, part, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, part, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin
              + ", with part #" + part);
        }
    }

    interface FLanePartMaskedBop {
        long[] apply(long[] a, long[] b, int origin, int part, boolean[] mask, int idx);
    }

    static void assertArraysEquals(long[] r, long[] a, long[] b, int origin, int part, boolean[] mask, FLanePartMaskedBop f) {
        int i = 0;
        try {
            for (; i < a.length; i += SPECIES.length()) {
                Assert.assertEquals(Arrays.copyOfRange(r, i, i+SPECIES.length()),
                  f.apply(a, b, origin, part, mask, i));
            }
        } catch (AssertionError e) {
            long[] ref = f.apply(a, b, origin, part, mask, i);
            long[] res = Arrays.copyOfRange(r, i, i+SPECIES.length());
            Assert.assertEquals(res, ref, "(ref: " + Arrays.toString(ref)
              + ", res: " + Arrays.toString(res)
              + "), at index #" + i
              + ", at origin #" + origin
              + ", with part #" + part);
        }
    }

    static int intCornerCaseValue(int i) {
        switch(i % 5) {
            case 0:
                return Integer.MAX_VALUE;
            case 1:
                return Integer.MIN_VALUE;
            case 2:
                return Integer.MIN_VALUE;
            case 3:
                return Integer.MAX_VALUE;
            default:
                return (int)0;
        }
    }

    static final List<IntFunction<long[]>> INT_LONG_GENERATORS = List.of(
            withToString("long[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(-i * 5));
            }),
            withToString("long[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i * 5));
            }),
            withToString("long[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((long)(i + 1) == 0) ? 1 : (long)(i + 1)));
            }),
            withToString("long[intCornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)intCornerCaseValue(i));
            })
    );

    static void assertArraysEquals(int[] r, long[] a, int offs) {
        int i = 0;
        try {
            for (; i < r.length; i++) {
                Assert.assertEquals(r[i], (int)(a[i+offs]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], (int)(a[i+offs]), "at index #" + i + ", input = " + a[i+offs]);
        }
    }



    static void assertArraysEquals(long[] r, long[] a, int offs) {
        int i = 0;
        try {
            for (; i < r.length; i++) {
                Assert.assertEquals(r[i], (long)(a[i+offs]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], (long)(a[i+offs]), "at index #" + i + ", input = " + a[i+offs]);
        }
    }

    static void assertArraysEquals(double[] r, long[] a, int offs) {
        int i = 0;
        try {
            for (; i < r.length; i++) {
                Assert.assertEquals(r[i], (double)(a[i+offs]));
            }
        } catch (AssertionError e) {
            Assert.assertEquals(r[i], (double)(a[i+offs]), "at index #" + i + ", input = " + a[i+offs]);
        }
    }


    static long bits(long e) {
        return  e;
    }

    static final List<IntFunction<long[]>> LONG_GENERATORS = List.of(
            withToString("long[-i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(-i * 5));
            }),
            withToString("long[i * 5]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i * 5));
            }),
            withToString("long[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (((long)(i + 1) == 0) ? 1 : (long)(i + 1)));
            }),
            withToString("long[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> cornerCaseValue(i));
            })
    );

    // Create combinations of pairs
    // @@@ Might be sensitive to order e.g. div by 0
    static final List<List<IntFunction<long[]>>> LONG_GENERATOR_PAIRS =
        Stream.of(LONG_GENERATORS.get(0)).
                flatMap(fa -> LONG_GENERATORS.stream().skip(1).map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] boolUnaryOpProvider() {
        return BOOL_ARRAY_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    static final List<List<IntFunction<long[]>>> LONG_GENERATOR_TRIPLES =
        LONG_GENERATOR_PAIRS.stream().
                flatMap(pair -> LONG_GENERATORS.stream().map(f -> List.of(pair.get(0), pair.get(1), f))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] longBinaryOpProvider() {
        return LONG_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longIndexedOpProvider() {
        return LONG_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longBinaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longTernaryOpProvider() {
        return LONG_GENERATOR_TRIPLES.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longTernaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_GENERATOR_TRIPLES.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpProvider() {
        return LONG_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longtoIntUnaryOpProvider() {
        return INT_LONG_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }


    @DataProvider
    public Object[][] maskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] maskCompareOpProvider() {
        return BOOLEAN_MASK_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shuffleProvider() {
        return INT_SHUFFLE_GENERATORS.stream().
                map(f -> new Object[]{f}).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] shuffleCompareOpProvider() {
        return INT_SHUFFLE_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpShuffleProvider() {
        return INT_SHUFFLE_GENERATORS.stream().
                flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpShuffleMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> INT_SHUFFLE_GENERATORS.stream().
                    flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                        return new Object[] {fa, fs, fm};
                }))).
                toArray(Object[][]::new);
    }

    static final List<BiFunction<Integer,Integer,long[]>> LONG_SHUFFLE_GENERATORS = List.of(
            withToStringBi("shuffle[random]", (Integer l, Integer m) -> {
                long[] a = new long[l];
                int upper = m;
                for (int i = 0; i < 1; i++) {
                    a[i] = (long)RAND.nextInt(upper);
                }
                return a;
            })
    );

    @DataProvider
    public Object[][] longUnaryOpSelectFromProvider() {
        return LONG_SHUFFLE_GENERATORS.stream().
                flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryOpSelectFromMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_SHUFFLE_GENERATORS.stream().
                    flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                        return new Object[] {fa, fs, fm};
                }))).
                toArray(Object[][]::new);
    }


    @DataProvider
    public Object[][] longUnaryOpIndexProvider() {
        return INT_INDEX_GENERATORS.stream().
                flatMap(fs -> LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fs};
                })).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longUnaryMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fm, fs};
            }))).
            toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] scatterMaskedOpIndexProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
          flatMap(fs -> INT_INDEX_GENERATORS.stream().flatMap(fm ->
            LONG_GENERATORS.stream().flatMap(fn ->
              LONG_GENERATORS.stream().map(fa -> {
                    return new Object[] {fa, fn, fm, fs};
            })))).
            toArray(Object[][]::new);
    }

    static final List<IntFunction<long[]>> LONG_COMPARE_GENERATORS = List.of(
            withToString("long[i]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)i);
            }),
            withToString("long[i - length / 2]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i - (s * BUFFER_REPS / 2)));
            }),
            withToString("long[i + 1]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i + 1));
            }),
            withToString("long[i - 2]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> (long)(i - 2));
            }),
            withToString("long[zigZag(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> i%3 == 0 ? (long)i : (i%3 == 1 ? (long)(i + 1) : (long)(i - 2)));
            }),
            withToString("long[cornerCaseValue(i)]", (int s) -> {
                return fill(s * BUFFER_REPS,
                            i -> cornerCaseValue(i));
            })
    );

    static final List<List<IntFunction<long[]>>> LONG_TEST_GENERATOR_ARGS =
        LONG_COMPARE_GENERATORS.stream().
                map(fa -> List.of(fa)).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] longTestOpProvider() {
        return LONG_TEST_GENERATOR_ARGS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longTestOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_TEST_GENERATOR_ARGS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    static final List<List<IntFunction<long[]>>> LONG_COMPARE_GENERATOR_PAIRS =
        LONG_COMPARE_GENERATORS.stream().
                flatMap(fa -> LONG_COMPARE_GENERATORS.stream().map(fb -> List.of(fa, fb))).
                collect(Collectors.toList());

    @DataProvider
    public Object[][] longCompareOpProvider() {
        return LONG_COMPARE_GENERATOR_PAIRS.stream().map(List::toArray).
                toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] longCompareOpMaskProvider() {
        return BOOLEAN_MASK_GENERATORS.stream().
                flatMap(fm -> LONG_COMPARE_GENERATOR_PAIRS.stream().map(lfa -> {
                    return Stream.concat(lfa.stream(), Stream.of(fm)).toArray();
                })).
                toArray(Object[][]::new);
    }

    interface ToLongF {
        long apply(int i);
    }

    static long[] fill(int s , ToLongF f) {
        return fill(new long[s], f);
    }

    static long[] fill(long[] a, ToLongF f) {
        for (int i = 0; i < a.length; i++) {
            a[i] = f.apply(i);
        }
        return a;
    }

    static long cornerCaseValue(int i) {
        switch(i % 5) {
            case 0:
                return Long.MAX_VALUE;
            case 1:
                return Long.MIN_VALUE;
            case 2:
                return Long.MIN_VALUE;
            case 3:
                return Long.MAX_VALUE;
            default:
                return (long)0;
        }
    }

    static long get(long[] a, int i) {
        return (long) a[i];
    }

    static final IntFunction<long[]> fr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new long[length];
    };

    static final IntFunction<boolean[]> fmr = (vl) -> {
        int length = BUFFER_REPS * vl;
        return new boolean[length];
    };


    static void replaceZero(long[] a, long v) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == 0) {
                a[i] = v;
            }
        }
    }

    static void replaceZero(long[] a, boolean[] mask, long v) {
        for (int i = 0; i < a.length; i++) {
            if (mask[i % mask.length] && a[i] == 0) {
                a[i] = v;
            }
        }
    }

    static boolean eq(long a, long b) {
        return a == b;
    }

    static boolean neq(long a, long b) {
        return a != b;
    }

    static boolean lt(long a, long b) {
        return a < b;
    }

    static boolean le(long a, long b) {
        return a <= b;
    }

    static boolean gt(long a, long b) {
        return a > b;
    }

    static boolean ge(long a, long b) {
        return a >= b;
    }

    static boolean ult(long a, long b) {
        return Long.compareUnsigned(a, b) < 0;
    }

    static boolean ule(long a, long b) {
        return Long.compareUnsigned(a, b) <= 0;
    }

    static boolean ugt(long a, long b) {
        return Long.compareUnsigned(a, b) > 0;
    }

    static boolean uge(long a, long b) {
        return Long.compareUnsigned(a, b) >= 0;
    }

    @Test
    static void smokeTest1() {
        LongVector three = LongVector.broadcast(SPECIES, (byte)-3);
        LongVector three2 = (LongVector) SPECIES.broadcast(-3);
        assert(three.eq(three2).allTrue());
        LongVector three3 = three2.broadcast(1).broadcast(-3);
        assert(three.eq(three3).allTrue());
        int scale = 2;
        Class<?> ETYPE = long.class;
        if (ETYPE == double.class || ETYPE == long.class)
            scale = 1000000;
        else if (ETYPE == byte.class && SPECIES.length() >= 64)
            scale = 1;
        LongVector higher = three.addIndex(scale);
        VectorMask<Long> m = three.compare(VectorOperators.LE, higher);
        assert(m.allTrue());
        m = higher.min((long)-1).test(VectorOperators.IS_NEGATIVE);
        assert(m.allTrue());
        long max = higher.reduceLanes(VectorOperators.MAX);
        assert(max == -3 + scale * (SPECIES.length()-1));
    }

    private static long[]
    bothToArray(LongVector a, LongVector b) {
        long[] r = new long[a.length() + b.length()];
        a.intoArray(r, 0);
        b.intoArray(r, a.length());
        return r;
    }

    @Test
    static void smokeTest2() {
        // Do some zipping and shuffling.
        LongVector io = (LongVector) SPECIES.broadcast(0).addIndex(1);
        LongVector io2 = (LongVector) VectorShuffle.iota(SPECIES,0,1,false).toVector();
        Assert.assertEquals(io, io2);
        LongVector a = io.add((long)1); //[1,2]
        LongVector b = a.neg();  //[-1,-2]
        long[] abValues = bothToArray(a,b); //[1,2,-1,-2]
        VectorShuffle<Long> zip0 = VectorShuffle.makeZip(SPECIES, 0);
        VectorShuffle<Long> zip1 = VectorShuffle.makeZip(SPECIES, 1);
        LongVector zab0 = a.rearrange(zip0,b); //[1,-1]
        LongVector zab1 = a.rearrange(zip1,b); //[2,-2]
        long[] zabValues = bothToArray(zab0, zab1); //[1,-1,2,-2]
        // manually zip
        long[] manual = new long[zabValues.length];
        for (int i = 0; i < manual.length; i += 2) {
            manual[i+0] = abValues[i/2];
            manual[i+1] = abValues[a.length() + i/2];
        }
        Assert.assertEquals(Arrays.toString(zabValues), Arrays.toString(manual));
        VectorShuffle<Long> unz0 = VectorShuffle.makeUnzip(SPECIES, 0);
        VectorShuffle<Long> unz1 = VectorShuffle.makeUnzip(SPECIES, 1);
        LongVector uab0 = zab0.rearrange(unz0,zab1);
        LongVector uab1 = zab0.rearrange(unz1,zab1);
        long[] abValues1 = bothToArray(uab0, uab1);
        Assert.assertEquals(Arrays.toString(abValues), Arrays.toString(abValues1));
    }

    static void iotaShuffle() {
        LongVector io = (LongVector) SPECIES.broadcast(0).addIndex(1);
        LongVector io2 = (LongVector) VectorShuffle.iota(SPECIES, 0 , 1, false).toVector();
        Assert.assertEquals(io, io2);
    }

    @Test
    // Test all shuffle related operations.
    static void shuffleTest() {
        // To test backend instructions, make sure that C2 is used.
        for (int loop = 0; loop < INVOC_COUNT * INVOC_COUNT; loop++) {
            iotaShuffle();
        }
    }

    @Test
    void viewAsIntegeralLanesTest() {
        Vector<?> asIntegral = SPECIES.zero().viewAsIntegralLanes();
        Assert.assertEquals(asIntegral.species(), SPECIES);
    }

    @Test
    void viewAsFloatingLanesTest() {
        Vector<?> asFloating = SPECIES.zero().viewAsFloatingLanes();
        VectorSpecies<?> asFloatingSpecies = asFloating.species();
        Assert.assertNotEquals(asFloatingSpecies.elementType(), SPECIES.elementType());
        Assert.assertEquals(asFloatingSpecies.vectorShape(), SPECIES.vectorShape());
        Assert.assertEquals(asFloatingSpecies.length(), SPECIES.length());
        Assert.assertEquals(asFloating.viewAsIntegralLanes().species(), SPECIES);
    }

    @Test
    // Test div by 0.
    static void bitwiseDivByZeroSmokeTest() {
        try {
            LongVector a = (LongVector) SPECIES.broadcast(0).addIndex(1);
            LongVector b = (LongVector) SPECIES.broadcast(0);
            a.div(b);
            Assert.fail();
        } catch (ArithmeticException e) {
        }

        try {
            LongVector a = (LongVector) SPECIES.broadcast(0).addIndex(1);
            LongVector b = (LongVector) SPECIES.broadcast(0);
            VectorMask<Long> m = a.lt((long) 1);
            a.div(b, m);
            Assert.fail();
        } catch (ArithmeticException e) {
        }
    }
    static long ADD(long a, long b) {
        return (long)(a + b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ADDLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::ADD);
    }
    static long add(long a, long b) {
        return (long)(a + b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void addLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.add(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, Long128VectorTests::add);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ADDLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ADD, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::ADD);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void addLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.add(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::add);
    }
    static long SUB(long a, long b) {
        return (long)(a - b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void SUBLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::SUB);
    }
    static long sub(long a, long b) {
        return (long)(a - b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void subLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.sub(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, Long128VectorTests::sub);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void SUBLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.SUB, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::SUB);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void subLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.sub(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::sub);
    }
    static long MUL(long a, long b) {
        return (long)(a * b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MULLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::MUL);
    }
    static long mul(long a, long b) {
        return (long)(a * b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void mulLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.mul(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, Long128VectorTests::mul);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void MULLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MUL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::MUL);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void mulLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.mul(bv, vmask).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::mul);
    }



    static long DIV(long a, long b) {
        return (long)(a / b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void DIVLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        replaceZero(b, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.DIV, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::DIV);
    }
    static long div(long a, long b) {
        return (long)(a / b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void divLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        replaceZero(b, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.div(bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::div);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void DIVLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        replaceZero(b, mask, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.DIV, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::DIV);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void divLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        replaceZero(b, mask, (long) 1);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.div(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::div);
    }

    static long FIRST_NONZERO(long a, long b) {
        return (long)((a)!=0?a:b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void FIRST_NONZEROLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::FIRST_NONZERO);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void FIRST_NONZEROLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.FIRST_NONZERO, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::FIRST_NONZERO);
    }

    static long AND(long a, long b) {
        return (long)(a & b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ANDLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::AND);
    }
    static long and(long a, long b) {
        return (long)(a & b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void andLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.and(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, Long128VectorTests::and);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ANDLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::AND);
    }


    static long AND_NOT(long a, long b) {
        return (long)(a & ~b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void AND_NOTLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::AND_NOT);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void AND_NOTLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.AND_NOT, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::AND_NOT);
    }


    static long OR(long a, long b) {
        return (long)(a | b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ORLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::OR);
    }
    static long or(long a, long b) {
        return (long)(a | b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void orLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.or(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, Long128VectorTests::or);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ORLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.OR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::OR);
    }


    static long XOR(long a, long b) {
        return (long)(a ^ b);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void XORLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::XOR);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void XORLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.XOR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::XOR);
    }


    @Test(dataProvider = "longBinaryOpProvider")
    static void addLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.add(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::add);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void addLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.add(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, Long128VectorTests::add);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void subLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.sub(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::sub);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void subLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.sub(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, Long128VectorTests::sub);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void mulLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.mul(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::mul);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void mulLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.mul(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, Long128VectorTests::mul);
    }




    @Test(dataProvider = "longBinaryOpProvider")
    static void divLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        replaceZero(b, (long) 1);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.div(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::div);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void divLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        replaceZero(b, (long) 1);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.div(b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, Long128VectorTests::div);
    }



    @Test(dataProvider = "longBinaryOpProvider")
    static void ORLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::OR);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void orLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.or(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::or);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ORLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, Long128VectorTests::OR);
    }



    @Test(dataProvider = "longBinaryOpProvider")
    static void ANDLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.AND, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::AND);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void andLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.and(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::and);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ANDLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.AND, b[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, mask, Long128VectorTests::AND);
    }



    @Test(dataProvider = "longBinaryOpProvider")
    static void ORLong128VectorTestsBroadcastLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, (long)b[i]).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, Long128VectorTests::OR);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ORLong128VectorTestsBroadcastMaskedLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.OR, (long)b[i], vmask).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, mask, Long128VectorTests::OR);
    }


    @Test(dataProvider = "longBinaryOpProvider")
    static void ADDLong128VectorTestsBroadcastLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.ADD, (long)b[i]).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, Long128VectorTests::ADD);
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ADDLong128VectorTestsBroadcastMaskedLongSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.ADD, (long)b[i], vmask).intoArray(r, i);
        }

        assertBroadcastLongArraysEquals(r, a, b, mask, Long128VectorTests::ADD);
    }

    static long LSHL(long a, long b) {
        return (long)((a << b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHLLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::LSHL);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHLLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHL, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::LSHL);
    }






    static long ASHR(long a, long b) {
        return (long)((a >> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ASHRLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::ASHR);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ASHRLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.ASHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::ASHR);
    }






    static long LSHR(long a, long b) {
        return (long)((a >>> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHRLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::LSHR);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHRLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.LSHR, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::LSHR);
    }






    static long LSHL_unary(long a, long b) {
        return (long)((a << b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHLLong128VectorTestsShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, Long128VectorTests::LSHL_unary);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHLLong128VectorTestsShift(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHL, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, Long128VectorTests::LSHL_unary);
    }






    static long LSHR_unary(long a, long b) {
        return (long)((a >>> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void LSHRLong128VectorTestsShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, Long128VectorTests::LSHR_unary);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void LSHRLong128VectorTestsShift(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.LSHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, Long128VectorTests::LSHR_unary);
    }






    static long ASHR_unary(long a, long b) {
        return (long)((a >> b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void ASHRLong128VectorTestsShift(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i]).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, Long128VectorTests::ASHR_unary);
    }



    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void ASHRLong128VectorTestsShift(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ASHR, (int)b[i], vmask).intoArray(r, i);
            }
        }

        assertShiftArraysEquals(r, a, b, mask, Long128VectorTests::ASHR_unary);
    }





    static long MIN(long a, long b) {
        return (long)(Math.min(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MINLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MIN, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::MIN);
    }
    static long min(long a, long b) {
        return (long)(Math.min(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void minLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.min(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, Long128VectorTests::min);
    }
    static long MAX(long a, long b) {
        return (long)(Math.max(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MAXLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.lanewise(VectorOperators.MAX, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::MAX);
    }
    static long max(long a, long b) {
        return (long)(Math.max(a, b));
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void maxLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.max(bv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, Long128VectorTests::max);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MINLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.MIN, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::MIN);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void minLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.min(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::min);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void MAXLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.MAX, b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::MAX);
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void maxLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.max(b[i]).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, Long128VectorTests::max);
    }

    static long ANDReduce(long[] a, int idx) {
        long res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }

    static long ANDReduceAll(long[] a) {
        long res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res &= ANDReduce(a, i);
        }

        return res;
    }


    @Test(dataProvider = "longUnaryOpProvider")
    static void ANDReduceLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::ANDReduce, Long128VectorTests::ANDReduceAll);
    }


    static long ANDReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = -1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res &= a[i];
        }

        return res;
    }

    static long ANDReduceAllMasked(long[] a, boolean[] mask) {
        long res = -1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res &= ANDReduceMasked(a, i, mask);
        }

        return res;
    }


    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ANDReduceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = -1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = -1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra &= av.reduceLanes(VectorOperators.AND, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::ANDReduceMasked, Long128VectorTests::ANDReduceAllMasked);
    }


    static long ORReduce(long[] a, int idx) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }

    static long ORReduceAll(long[] a) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res |= ORReduce(a, i);
        }

        return res;
    }


    @Test(dataProvider = "longUnaryOpProvider")
    static void ORReduceLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::ORReduce, Long128VectorTests::ORReduceAll);
    }


    static long ORReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res |= a[i];
        }

        return res;
    }

    static long ORReduceAllMasked(long[] a, boolean[] mask) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res |= ORReduceMasked(a, i, mask);
        }

        return res;
    }


    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ORReduceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra |= av.reduceLanes(VectorOperators.OR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::ORReduceMasked, Long128VectorTests::ORReduceAllMasked);
    }


    static long XORReduce(long[] a, int idx) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res ^= a[i];
        }

        return res;
    }

    static long XORReduceAll(long[] a) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res ^= XORReduce(a, i);
        }

        return res;
    }


    @Test(dataProvider = "longUnaryOpProvider")
    static void XORReduceLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::XORReduce, Long128VectorTests::XORReduceAll);
    }


    static long XORReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res ^= a[i];
        }

        return res;
    }

    static long XORReduceAllMasked(long[] a, boolean[] mask) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res ^= XORReduceMasked(a, i, mask);
        }

        return res;
    }


    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void XORReduceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra ^= av.reduceLanes(VectorOperators.XOR, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::XORReduceMasked, Long128VectorTests::XORReduceAllMasked);
    }

    static long ADDReduce(long[] a, int idx) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res += a[i];
        }

        return res;
    }

    static long ADDReduceAll(long[] a) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res += ADDReduce(a, i);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpProvider")
    static void ADDReduceLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::ADDReduce, Long128VectorTests::ADDReduceAll);
    }
    static long ADDReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 0;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res += a[i];
        }

        return res;
    }

    static long ADDReduceAllMasked(long[] a, boolean[] mask) {
        long res = 0;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res += ADDReduceMasked(a, i, mask);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ADDReduceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 0;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra += av.reduceLanes(VectorOperators.ADD, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::ADDReduceMasked, Long128VectorTests::ADDReduceAllMasked);
    }
    static long MULReduce(long[] a, int idx) {
        long res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res *= a[i];
        }

        return res;
    }

    static long MULReduceAll(long[] a) {
        long res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res *= MULReduce(a, i);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpProvider")
    static void MULReduceLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL);
            }
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::MULReduce, Long128VectorTests::MULReduceAll);
    }
    static long MULReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = 1;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if (mask[i % SPECIES.length()])
                res *= a[i];
        }

        return res;
    }

    static long MULReduceAllMasked(long[] a, boolean[] mask) {
        long res = 1;
        for (int i = 0; i < a.length; i += SPECIES.length()) {
            res *= MULReduceMasked(a, i, mask);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MULReduceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 1;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = 1;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra *= av.reduceLanes(VectorOperators.MUL, vmask);
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::MULReduceMasked, Long128VectorTests::MULReduceAllMasked);
    }
    static long MINReduce(long[] a, int idx) {
        long res = Long.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (long)Math.min(res, a[i]);
        }

        return res;
    }

    static long MINReduceAll(long[] a) {
        long res = Long.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (long)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpProvider")
    static void MINReduceLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = Long.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long)Math.min(ra, av.reduceLanes(VectorOperators.MIN));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::MINReduce, Long128VectorTests::MINReduceAll);
    }
    static long MINReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = Long.MAX_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (long)Math.min(res, a[i]);
        }

        return res;
    }

    static long MINReduceAllMasked(long[] a, boolean[] mask) {
        long res = Long.MAX_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (long)Math.min(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MINReduceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = Long.MAX_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MIN, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MAX_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long)Math.min(ra, av.reduceLanes(VectorOperators.MIN, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::MINReduceMasked, Long128VectorTests::MINReduceAllMasked);
    }
    static long MAXReduce(long[] a, int idx) {
        long res = Long.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res = (long)Math.max(res, a[i]);
        }

        return res;
    }

    static long MAXReduceAll(long[] a) {
        long res = Long.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            res = (long)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpProvider")
    static void MAXReduceLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = Long.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long)Math.max(ra, av.reduceLanes(VectorOperators.MAX));
            }
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::MAXReduce, Long128VectorTests::MAXReduceAll);
    }
    static long MAXReduceMasked(long[] a, int idx, boolean[] mask) {
        long res = Long.MIN_VALUE;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            if(mask[i % SPECIES.length()])
                res = (long)Math.max(res, a[i]);
        }

        return res;
    }

    static long MAXReduceAllMasked(long[] a, boolean[] mask) {
        long res = Long.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if(mask[i % SPECIES.length()])
                res = (long)Math.max(res, a[i]);
        }

        return res;
    }
    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void MAXReduceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = Long.MIN_VALUE;

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                r[i] = av.reduceLanes(VectorOperators.MAX, vmask);
            }
        }

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            ra = Long.MIN_VALUE;
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                ra = (long)Math.max(ra, av.reduceLanes(VectorOperators.MAX, vmask));
            }
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::MAXReduceMasked, Long128VectorTests::MAXReduceAllMasked);
    }

    static boolean anyTrue(boolean[] a, int idx) {
        boolean res = false;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res |= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void anyTrueLong128VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.anyTrue();
            }
        }

        assertReductionBoolArraysEquals(r, mask, Long128VectorTests::anyTrue);
    }


    static boolean allTrue(boolean[] a, int idx) {
        boolean res = true;
        for (int i = idx; i < (idx + SPECIES.length()); i++) {
            res &= a[i];
        }

        return res;
    }


    @Test(dataProvider = "boolUnaryOpProvider")
    static void allTrueLong128VectorTests(IntFunction<boolean[]> fm) {
        boolean[] mask = fm.apply(SPECIES.length());
        boolean[] r = fmr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < mask.length; i += SPECIES.length()) {
                VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, i);
                r[i] = vmask.allTrue();
            }
        }

        assertReductionBoolArraysEquals(r, mask, Long128VectorTests::allTrue);
    }


    @Test(dataProvider = "longUnaryOpProvider")
    static void withLong128VectorTests(IntFunction<long []> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.withLane(0, (long)4).intoArray(r, i);
            }
        }

        assertInsertArraysEquals(r, a, (long)4, 0);
    }
    static boolean testIS_DEFAULT(long a) {
        return bits(a)==0;
    }

    @Test(dataProvider = "longTestOpProvider")
    static void IS_DEFAULTLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                VectorMask<Long> mv = av.test(VectorOperators.IS_DEFAULT);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), testIS_DEFAULT(a[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longTestOpMaskProvider")
    static void IS_DEFAULTMaskedLong128VectorTestsSmokeTest(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.test(VectorOperators.IS_DEFAULT, vmask);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j),  vmask.laneIsSet(j) && testIS_DEFAULT(a[i + j]));
            }
        }
    }
    static boolean testIS_NEGATIVE(long a) {
        return bits(a)<0;
    }

    @Test(dataProvider = "longTestOpProvider")
    static void IS_NEGATIVELong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                VectorMask<Long> mv = av.test(VectorOperators.IS_NEGATIVE);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), testIS_NEGATIVE(a[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longTestOpMaskProvider")
    static void IS_NEGATIVEMaskedLong128VectorTestsSmokeTest(IntFunction<long[]> fa,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.test(VectorOperators.IS_NEGATIVE, vmask);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j),  vmask.laneIsSet(j) && testIS_NEGATIVE(a[i + j]));
            }
        }
    }




    @Test(dataProvider = "longCompareOpProvider")
    static void LTLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), lt(a[i + j], b[i + j]));
                }
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void ltLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.lt(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), lt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void LTLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && lt(a[i + j], b[i + j]));
                }
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void GTLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), gt(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void GTLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && gt(a[i + j], b[i + j]));
                }
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void EQLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.EQ, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), eq(a[i + j], b[i + j]));
                }
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void eqLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.eq(bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), eq(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void EQLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.EQ, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && eq(a[i + j], b[i + j]));
                }
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void NELong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.NE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), neq(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void NELong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.NE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && neq(a[i + j], b[i + j]));
                }
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void LELong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), le(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void LELong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.LE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && le(a[i + j], b[i + j]));
                }
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void GELong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ge(a[i + j], b[i + j]));
                }
            }
        }
    }

    @Test(dataProvider = "longCompareOpMaskProvider")
    static void GELong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.GE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ge(a[i + j], b[i + j]));
                }
            }
        }
    }



    @Test(dataProvider = "longCompareOpProvider")
    static void UNSIGNED_LTLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_LT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ult(a[i + j], b[i + j]));
                }
            }
        }
    }



    @Test(dataProvider = "longCompareOpMaskProvider")
    static void UNSIGNED_LTLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_LT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ult(a[i + j], b[i + j]));
                }
            }
        }
    }




    @Test(dataProvider = "longCompareOpProvider")
    static void UNSIGNED_GTLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_GT, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ugt(a[i + j], b[i + j]));
                }
            }
        }
    }



    @Test(dataProvider = "longCompareOpMaskProvider")
    static void UNSIGNED_GTLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_GT, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ugt(a[i + j], b[i + j]));
                }
            }
        }
    }




    @Test(dataProvider = "longCompareOpProvider")
    static void UNSIGNED_LELong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_LE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), ule(a[i + j], b[i + j]));
                }
            }
        }
    }



    @Test(dataProvider = "longCompareOpMaskProvider")
    static void UNSIGNED_LELong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_LE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && ule(a[i + j], b[i + j]));
                }
            }
        }
    }




    @Test(dataProvider = "longCompareOpProvider")
    static void UNSIGNED_GELong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_GE, bv);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), uge(a[i + j], b[i + j]));
                }
            }
        }
    }



    @Test(dataProvider = "longCompareOpMaskProvider")
    static void UNSIGNED_GELong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                VectorMask<Long> mv = av.compare(VectorOperators.UNSIGNED_GE, bv, vmask);

                // Check results as part of computation.
                for (int j = 0; j < SPECIES.length(); j++) {
                    Assert.assertEquals(mv.laneIsSet(j), mask[j] && uge(a[i + j], b[i + j]));
                }
            }
        }
    }



    @Test(dataProvider = "longCompareOpProvider")
    static void LTLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.LT, b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i]);
            }
        }
    }


    @Test(dataProvider = "longCompareOpMaskProvider")
    static void LTLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa,
                                IntFunction<long[]> fb, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.LT, b[i], vmask);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), mask[j] && (a[i + j] < b[i]));
            }
        }
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void EQLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.EQ, b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i]);
            }
        }
    }


    @Test(dataProvider = "longCompareOpMaskProvider")
    static void EQLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa,
                                IntFunction<long[]> fb, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());

        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.compare(VectorOperators.EQ, b[i], vmask);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), mask[j] && (a[i + j] == b[i]));
            }
        }
    }


    static long blend(long a, long b, boolean mask) {
        return mask ? b : a;
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void blendLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.blend(bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::blend);
    }

    @Test(dataProvider = "longUnaryOpShuffleProvider")
    static void RearrangeLong128VectorTests(IntFunction<long[]> fa,
                                           BiFunction<Integer,Integer,int[]> fs) {
        long[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.rearrange(VectorShuffle.fromArray(SPECIES, order, i)).intoArray(r, i);
            }
        }

        assertRearrangeArraysEquals(r, a, order, SPECIES.length());
    }

    @Test(dataProvider = "longUnaryOpShuffleMaskProvider")
    static void RearrangeLong128VectorTestsMaskedSmokeTest(IntFunction<long[]> fa,
                                                          BiFunction<Integer,Integer,int[]> fs,
                                                          IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        int[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.rearrange(VectorShuffle.fromArray(SPECIES, order, i), vmask).intoArray(r, i);
        }

        assertRearrangeArraysEquals(r, a, order, mask, SPECIES.length());
    }
    @Test(dataProvider = "longUnaryOpProvider")
    static void getLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                int num_lanes = SPECIES.length();
                // Manually unroll because full unroll happens after intrinsification.
                // Unroll is needed because get intrinsic requires for index to be a known constant.
                if (num_lanes == 1) {
                    r[i]=av.lane(0);
                } else if (num_lanes == 2) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                } else if (num_lanes == 4) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                } else if (num_lanes == 8) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                } else if (num_lanes == 16) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                } else if (num_lanes == 32) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                } else if (num_lanes == 64) {
                    r[i]=av.lane(0);
                    r[i+1]=av.lane(1);
                    r[i+2]=av.lane(2);
                    r[i+3]=av.lane(3);
                    r[i+4]=av.lane(4);
                    r[i+5]=av.lane(5);
                    r[i+6]=av.lane(6);
                    r[i+7]=av.lane(7);
                    r[i+8]=av.lane(8);
                    r[i+9]=av.lane(9);
                    r[i+10]=av.lane(10);
                    r[i+11]=av.lane(11);
                    r[i+12]=av.lane(12);
                    r[i+13]=av.lane(13);
                    r[i+14]=av.lane(14);
                    r[i+15]=av.lane(15);
                    r[i+16]=av.lane(16);
                    r[i+17]=av.lane(17);
                    r[i+18]=av.lane(18);
                    r[i+19]=av.lane(19);
                    r[i+20]=av.lane(20);
                    r[i+21]=av.lane(21);
                    r[i+22]=av.lane(22);
                    r[i+23]=av.lane(23);
                    r[i+24]=av.lane(24);
                    r[i+25]=av.lane(25);
                    r[i+26]=av.lane(26);
                    r[i+27]=av.lane(27);
                    r[i+28]=av.lane(28);
                    r[i+29]=av.lane(29);
                    r[i+30]=av.lane(30);
                    r[i+31]=av.lane(31);
                    r[i+32]=av.lane(32);
                    r[i+33]=av.lane(33);
                    r[i+34]=av.lane(34);
                    r[i+35]=av.lane(35);
                    r[i+36]=av.lane(36);
                    r[i+37]=av.lane(37);
                    r[i+38]=av.lane(38);
                    r[i+39]=av.lane(39);
                    r[i+40]=av.lane(40);
                    r[i+41]=av.lane(41);
                    r[i+42]=av.lane(42);
                    r[i+43]=av.lane(43);
                    r[i+44]=av.lane(44);
                    r[i+45]=av.lane(45);
                    r[i+46]=av.lane(46);
                    r[i+47]=av.lane(47);
                    r[i+48]=av.lane(48);
                    r[i+49]=av.lane(49);
                    r[i+50]=av.lane(50);
                    r[i+51]=av.lane(51);
                    r[i+52]=av.lane(52);
                    r[i+53]=av.lane(53);
                    r[i+54]=av.lane(54);
                    r[i+55]=av.lane(55);
                    r[i+56]=av.lane(56);
                    r[i+57]=av.lane(57);
                    r[i+58]=av.lane(58);
                    r[i+59]=av.lane(59);
                    r[i+60]=av.lane(60);
                    r[i+61]=av.lane(61);
                    r[i+62]=av.lane(62);
                    r[i+63]=av.lane(63);
                } else {
                    for (int j = 0; j < SPECIES.length(); j++) {
                        r[i+j]=av.lane(j);
                    }
                }
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::get);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void BroadcastLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector.broadcast(SPECIES, a[i]).intoArray(r, i);
            }
        }

        assertBroadcastArraysEquals(r, a);
    }





    @Test(dataProvider = "longUnaryOpProvider")
    static void ZeroLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector.zero(SPECIES).intoArray(a, i);
            }
        }

        Assert.assertEquals(a, r);
    }




    static long[] sliceUnary(long[] a, int origin, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = a[idx+i+origin];
            else
                res[i] = (long)0;
        }
        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void sliceUnaryLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.slice(origin).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, origin, Long128VectorTests::sliceUnary);
    }
    static long[] sliceBinary(long[] a, long[] b, int origin, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = a[idx+i+origin];
            else {
                res[i] = b[idx+j];
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void sliceBinaryLong128VectorTestsBinary(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.slice(origin, bv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, Long128VectorTests::sliceBinary);
    }
    static long[] slice(long[] a, long[] b, int origin, boolean[] mask, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = mask[i] ? a[idx+i+origin] : (long)0;
            else {
                res[i] = mask[i] ? b[idx+j] : (long)0;
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void sliceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
    IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        long[] r = new long[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.slice(origin, bv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, mask, Long128VectorTests::slice);
    }
    static long[] unsliceUnary(long[] a, int origin, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i < origin)
                res[i] = (long)0;
            else {
                res[i] = a[idx+j];
                j++;
            }
        }
        return res;
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void unsliceUnaryLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.unslice(origin).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, origin, Long128VectorTests::unsliceUnary);
    }
    static long[] unsliceBinary(long[] a, long[] b, int origin, int part, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if (part == 0) {
                if (i < origin)
                    res[i] = b[idx+i];
                else {
                    res[i] = a[idx+j];
                    j++;
                }
            } else if (part == 1) {
                if (i < origin)
                    res[i] = a[idx+SPECIES.length()-origin+i];
                else {
                    res[i] = b[idx+origin+j];
                    j++;
                }
            }
        }
        return res;
    }

    @Test(dataProvider = "longBinaryOpProvider")
    static void unsliceBinaryLong128VectorTestsBinary(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] r = new long[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        int part = (new java.util.Random()).nextInt(2);
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.unslice(origin, bv, part).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, part, Long128VectorTests::unsliceBinary);
    }
    static long[] unslice(long[] a, long[] b, int origin, int part, boolean[] mask, int idx) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0, j = 0; i < SPECIES.length(); i++){
            if(i+origin < SPECIES.length())
                res[i] = b[idx+i+origin];
            else {
                res[i] = b[idx+j];
                j++;
            }
        }
        for (int i = 0; i < SPECIES.length(); i++){
            res[i] = mask[i] ? a[idx+i] : res[i];
        }
        long[] res1 = new long[SPECIES.length()];
        if (part == 0) {
            for (int i = 0, j = 0; i < SPECIES.length(); i++){
                if (i < origin)
                    res1[i] = b[idx+i];
                else {
                   res1[i] = res[j];
                   j++;
                }
            }
        } else if (part == 1) {
            for (int i = 0, j = 0; i < SPECIES.length(); i++){
                if (i < origin)
                    res1[i] = res[SPECIES.length()-origin+i];
                else {
                    res1[i] = b[idx+origin+j];
                    j++;
                }
            }
        }
        return res1;
    }

    @Test(dataProvider = "longBinaryOpMaskProvider")
    static void unsliceLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
    IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long[] r = new long[a.length];
        int origin = (new java.util.Random()).nextInt(SPECIES.length());
        int part = (new java.util.Random()).nextInt(2);
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                av.unslice(origin, bv, part, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, origin, part, mask, Long128VectorTests::unslice);
    }























    static long BITWISE_BLEND(long a, long b, long c) {
        return (long)((a&~(c))|(b&c));
    }
    static long bitwiseBlend(long a, long b, long c) {
        return (long)((a&~(c))|(b&c));
    }


    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                LongVector cv = LongVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, c, Long128VectorTests::BITWISE_BLEND);
    }
    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.bitwiseBlend(bv, cv).intoArray(r, i);
        }

        assertArraysEquals(r, a, b, c, Long128VectorTests::bitwiseBlend);
    }


    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                LongVector bv = LongVector.fromArray(SPECIES, b, i);
                LongVector cv = LongVector.fromArray(SPECIES, c, i);
                av.lanewise(VectorOperators.BITWISE_BLEND, bv, cv, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, c, mask, Long128VectorTests::BITWISE_BLEND);
    }




    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, bv, c[i]).intoArray(r, i);
        }
        assertBroadcastArraysEquals(r, a, b, c, Long128VectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLong128VectorTestsAltBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], cv).intoArray(r, i);
        }
        assertAltBroadcastArraysEquals(r, a, b, c, Long128VectorTests::BITWISE_BLEND);
    }
    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.bitwiseBlend(bv, c[i]).intoArray(r, i);
        }
        assertBroadcastArraysEquals(r, a, b, c, Long128VectorTests::bitwiseBlend);
    }

    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLong128VectorTestsAltBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.bitwiseBlend(b[i], cv).intoArray(r, i);
        }
        assertAltBroadcastArraysEquals(r, a, b, c, Long128VectorTests::bitwiseBlend);
    }


    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, b, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, bv, c[i], vmask).intoArray(r, i);
        }

        assertBroadcastArraysEquals(r, a, b, c, mask, Long128VectorTests::BITWISE_BLEND);
    }

    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLong128VectorTestsAltBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector cv = LongVector.fromArray(SPECIES, c, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], cv, vmask).intoArray(r, i);
        }

        assertAltBroadcastArraysEquals(r, a, b, c, mask, Long128VectorTests::BITWISE_BLEND);
    }




    @Test(dataProvider = "longTernaryOpProvider")
    static void BITWISE_BLENDLong128VectorTestsDoubleBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], c[i]).intoArray(r, i);
        }

        assertDoubleBroadcastArraysEquals(r, a, b, c, Long128VectorTests::BITWISE_BLEND);
    }
    @Test(dataProvider = "longTernaryOpProvider")
    static void bitwiseBlendLong128VectorTestsDoubleBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb, IntFunction<long[]> fc) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.bitwiseBlend(b[i], c[i]).intoArray(r, i);
        }

        assertDoubleBroadcastArraysEquals(r, a, b, c, Long128VectorTests::bitwiseBlend);
    }


    @Test(dataProvider = "longTernaryOpMaskProvider")
    static void BITWISE_BLENDLong128VectorTestsDoubleBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb,
                                          IntFunction<long[]> fc, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());
        long[] c = fc.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            av.lanewise(VectorOperators.BITWISE_BLEND, b[i], c[i], vmask).intoArray(r, i);
        }

        assertDoubleBroadcastArraysEquals(r, a, b, c, mask, Long128VectorTests::BITWISE_BLEND);
    }


    static long NEG(long a) {
        return (long)(-((long)a));
    }

    static long neg(long a) {
        return (long)(-((long)a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void NEGLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::NEG);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void negLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.neg().intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::neg);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void NEGMaskedLong128VectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NEG, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, Long128VectorTests::NEG);
    }

    static long ABS(long a) {
        return (long)(Math.abs((long)a));
    }

    static long abs(long a) {
        return (long)(Math.abs((long)a));
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void ABSLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::ABS);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void absLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.abs().intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::abs);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ABSMaskedLong128VectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ABS, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, Long128VectorTests::ABS);
    }


    static long NOT(long a) {
        return (long)(~((long)a));
    }

    static long not(long a) {
        return (long)(~((long)a));
    }



    @Test(dataProvider = "longUnaryOpProvider")
    static void NOTLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::NOT);
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void notLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.not().intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::not);
    }



    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void NOTMaskedLong128VectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.NOT, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, Long128VectorTests::NOT);
    }



    static long ZOMO(long a) {
        return (long)((a==0?0:-1));
    }



    @Test(dataProvider = "longUnaryOpProvider")
    static void ZOMOLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, Long128VectorTests::ZOMO);
    }



    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ZOMOMaskedLong128VectorTests(IntFunction<long[]> fa,
                                                IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.lanewise(VectorOperators.ZOMO, vmask).intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, mask, Long128VectorTests::ZOMO);
    }




    static long[] gather(long a[], int ix, int[] b, int iy) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++) {
            int bi = iy + i;
            res[i] = a[b[bi] + ix];
        }
        return res;
    }

    @Test(dataProvider = "longUnaryOpIndexProvider")
    static void gatherLong128VectorTests(IntFunction<long[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        long[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        long[] r = new long[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i, b, i);
                av.intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::gather);
    }
    static long[] gatherMasked(long a[], int ix, boolean[] mask, int[] b, int iy) {
        long[] res = new long[SPECIES.length()];
        for (int i = 0; i < SPECIES.length(); i++) {
            int bi = iy + i;
            if (mask[i]) {
              res[i] = a[b[bi] + ix];
            }
        }
        return res;
    }

    @Test(dataProvider = "longUnaryMaskedOpIndexProvider")
    static void gatherMaskedLong128VectorTests(IntFunction<long[]> fa, BiFunction<Integer,Integer,int[]> fs, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        int[] b    = fs.apply(a.length, SPECIES.length());
        long[] r = new long[a.length];
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i, b, i, vmask);
                av.intoArray(r, i);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::gatherMasked);
    }

    static long[] scatter(long a[], int ix, int[] b, int iy) {
      long[] res = new long[SPECIES.length()];
      for (int i = 0; i < SPECIES.length(); i++) {
        int bi = iy + i;
        res[b[bi]] = a[i + ix];
      }
      return res;
    }

    @Test(dataProvider = "longUnaryOpIndexProvider")
    static void scatterLong128VectorTests(IntFunction<long[]> fa, BiFunction<Integer,Integer,int[]> fs) {
        long[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        long[] r = new long[a.length];

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, b, i);
            }
        }

        assertArraysEquals(r, a, b, Long128VectorTests::scatter);
    }

    static long[] scatterMasked(long r[], long a[], int ix, boolean[] mask, int[] b, int iy) {
      // First, gather r.
      long[] oldVal = gather(r, ix, b, iy);
      long[] newVal = new long[SPECIES.length()];

      // Second, blending it with a.
      for (int i = 0; i < SPECIES.length(); i++) {
        newVal[i] = blend(oldVal[i], a[i+ix], mask[i]);
      }

      // Third, scatter: copy old value of r, and scatter it manually.
      long[] res = Arrays.copyOfRange(r, ix, ix+SPECIES.length());
      for (int i = 0; i < SPECIES.length(); i++) {
        int bi = iy + i;
        res[b[bi]] = newVal[i];
      }

      return res;
    }

    @Test(dataProvider = "scatterMaskedOpIndexProvider")
    static void scatterMaskedLong128VectorTests(IntFunction<long[]> fa, IntFunction<long[]> fb, BiFunction<Integer,Integer,int[]> fs, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        int[] b = fs.apply(a.length, SPECIES.length());
        long[] r = fb.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                LongVector av = LongVector.fromArray(SPECIES, a, i);
                av.intoArray(r, i, b, i, vmask);
            }
        }

        assertArraysEquals(r, a, b, mask, Long128VectorTests::scatterMasked);
    }


    @Test(dataProvider = "longCompareOpProvider")
    static void ltLong128VectorTestsBroadcastSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.lt(b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] < b[i]);
            }
        }
    }

    @Test(dataProvider = "longCompareOpProvider")
    static void eqLong128VectorTestsBroadcastMaskedSmokeTest(IntFunction<long[]> fa, IntFunction<long[]> fb) {
        long[] a = fa.apply(SPECIES.length());
        long[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            VectorMask<Long> mv = av.eq(b[i]);

            // Check results as part of computation.
            for (int j = 0; j < SPECIES.length(); j++) {
                Assert.assertEquals(mv.laneIsSet(j), a[i + j] == b[i]);
            }
        }
    }

    @Test(dataProvider = "longtoIntUnaryOpProvider")
    static void toIntArrayLong128VectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            int[] r = av.toIntArray();
            assertArraysEquals(r, a, i);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void toLongArrayLong128VectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            long[] r = av.toLongArray();
            assertArraysEquals(r, a, i);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void toDoubleArrayLong128VectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            double[] r = av.toDoubleArray();
            assertArraysEquals(r, a, i);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void toStringLong128VectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            String str = av.toString();

            long subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            Assert.assertTrue(str.equals(Arrays.toString(subarr)), "at index " + i + ", string should be = " + Arrays.toString(subarr) + ", but is = " + str);
        }
    }

    @Test(dataProvider = "longUnaryOpProvider")
    static void hashCodeLong128VectorTestsSmokeTest(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            int hash = av.hashCode();

            long subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            int expectedHash = Objects.hash(SPECIES, Arrays.hashCode(subarr));
            Assert.assertTrue(hash == expectedHash, "at index " + i + ", hash should be = " + expectedHash + ", but is = " + hash);
        }
    }



    @Test(dataProvider = "longUnaryOpProvider")
    static void ADDReduceLongLong128VectorTests(IntFunction<long[]> fa) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        long ra = 0;

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            r[i] = av.reduceLanesToLong(VectorOperators.ADD);
        }

        ra = 0;
        for (int i = 0; i < a.length; i ++) {
            ra += r[i];
        }

        assertReductionArraysEquals(r, ra, a,
                Long128VectorTests::ADDReduce, Long128VectorTests::ADDReduceAll);
    }

    @Test(dataProvider = "longUnaryOpMaskProvider")
    static void ADDReduceLongLong128VectorTestsMasked(IntFunction<long[]> fa, IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);
        long ra = 0;

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            r[i] = av.reduceLanesToLong(VectorOperators.ADD, vmask);
        }

        ra = 0;
        for (int i = 0; i < a.length; i ++) {
            ra += r[i];
        }

        assertReductionArraysEqualsMasked(r, ra, a, mask,
                Long128VectorTests::ADDReduceMasked, Long128VectorTests::ADDReduceAllMasked);
    }

    @Test(dataProvider = "longUnaryOpSelectFromProvider")
    static void SelectFromLong128VectorTests(IntFunction<long[]> fa,
                                           BiFunction<Integer,Integer,long[]> fs) {
        long[] a = fa.apply(SPECIES.length());
        long[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, order, i);
            bv.selectFrom(av).intoArray(r, i);
        }

        assertSelectFromArraysEquals(r, a, order, SPECIES.length());
    }

    @Test(dataProvider = "longUnaryOpSelectFromMaskProvider")
    static void SelectFromLong128VectorTestsMaskedSmokeTest(IntFunction<long[]> fa,
                                                           BiFunction<Integer,Integer,long[]> fs,
                                                           IntFunction<boolean[]> fm) {
        long[] a = fa.apply(SPECIES.length());
        long[] order = fs.apply(a.length, SPECIES.length());
        long[] r = fr.apply(SPECIES.length());
        boolean[] mask = fm.apply(SPECIES.length());
        VectorMask<Long> vmask = VectorMask.fromArray(SPECIES, mask, 0);

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            LongVector av = LongVector.fromArray(SPECIES, a, i);
            LongVector bv = LongVector.fromArray(SPECIES, order, i);
            bv.selectFrom(av, vmask).intoArray(r, i);
        }

        assertSelectFromArraysEquals(r, a, order, mask, SPECIES.length());
    }

    @Test(dataProvider = "shuffleProvider")
    static void shuffleMiscellaneousLong128VectorTestsSmokeTest(BiFunction<Integer,Integer,int[]> fs) {
        int[] a = fs.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var shuffle = VectorShuffle.fromArray(SPECIES, a, i);
            int hash = shuffle.hashCode();
            int length = shuffle.length();

            int subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            int expectedHash = Objects.hash(SPECIES, Arrays.hashCode(subarr));
            Assert.assertTrue(hash == expectedHash, "at index " + i + ", hash should be = " + expectedHash + ", but is = " + hash);
            Assert.assertEquals(length, SPECIES.length());
        }
    }

    @Test(dataProvider = "shuffleProvider")
    static void shuffleToStringLong128VectorTestsSmokeTest(BiFunction<Integer,Integer,int[]> fs) {
        int[] a = fs.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var shuffle = VectorShuffle.fromArray(SPECIES, a, i);
            String str = shuffle.toString();

            int subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            Assert.assertTrue(str.equals("Shuffle" + Arrays.toString(subarr)), "at index " +
                i + ", string should be = " + Arrays.toString(subarr) + ", but is = " + str);
        }
    }

    @Test(dataProvider = "shuffleCompareOpProvider")
    static void shuffleEqualsLong128VectorTestsSmokeTest(BiFunction<Integer,Integer,int[]> fa, BiFunction<Integer,Integer,int[]> fb) {
        int[] a = fa.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());
        int[] b = fb.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = VectorShuffle.fromArray(SPECIES, a, i);
            var bv = VectorShuffle.fromArray(SPECIES, b, i);
            boolean eq = av.equals(bv);
            int to = i + SPECIES.length();
            Assert.assertEquals(eq, Arrays.equals(a, i, to, b, i, to));
        }
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskEqualsLong128VectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            boolean equals = av.equals(bv);
            int to = i + SPECIES.length();
            Assert.assertEquals(equals, Arrays.equals(a, i, to, b, i, to));
        }
    }

    static boolean beq(boolean a, boolean b) {
        return (a == b);
    }

    @Test(dataProvider = "maskCompareOpProvider")
    static void maskEqLong128VectorTestsSmokeTest(IntFunction<boolean[]> fa, IntFunction<boolean[]> fb) {
        boolean[] a = fa.apply(SPECIES.length());
        boolean[] b = fb.apply(SPECIES.length());
        boolean[] r = new boolean[a.length];

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var av = SPECIES.loadMask(a, i);
            var bv = SPECIES.loadMask(b, i);
            var cv = av.eq(bv);
            cv.intoArray(r, i);
        }
        assertArraysEquals(r, a, b, Long128VectorTests::beq);
    }

    @Test(dataProvider = "maskProvider")
    static void maskHashCodeLong128VectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var vmask = SPECIES.loadMask(a, i);
            int hash = vmask.hashCode();

            boolean subarr[] = Arrays.copyOfRange(a, i, i + SPECIES.length());
            int expectedHash = Objects.hash(SPECIES, Arrays.hashCode(subarr));
            Assert.assertTrue(hash == expectedHash, "at index " + i + ", hash should be = " + expectedHash + ", but is = " + hash);
        }
    }

    @Test(dataProvider = "maskProvider")
    static void maskTrueCountLong128VectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var vmask = SPECIES.loadMask(a, i);
            int tcount = vmask.trueCount();
            int expectedTcount = 0;
            for (int j = i; j < i + SPECIES.length(); j++) {
                expectedTcount += a[j] ? 1 : 0;
            }
            Assert.assertTrue(tcount == expectedTcount, "at index " + i + ", trueCount should be = " + expectedTcount + ", but is = " + tcount);
        }
    }

    @Test(dataProvider = "maskProvider")
    static void maskLastTrueLong128VectorTestsSmokeTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());

        for (int i = 0; i < a.length; i += SPECIES.length()) {
            var vmask = SPECIES.loadMask(a, i);
            int ltrue = vmask.lastTrue();
            int j = i + SPECIES.length() - 1;
            for (; j >= i; j--) {
                if (a[j]) break;
            }
            int expectedLtrue = j - i;

            Assert.assertTrue(ltrue == expectedLtrue, "at index " + i +
                ", lastTrue should be = " + expectedLtrue + ", but is = " + ltrue);
        }
    }

    @Test(dataProvider = "maskProvider")
    static void maskCastLong128VectorTestsIntTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                var res = vmask.cast(IntVector.SPECIES_64);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }
    @Test(dataProvider = "maskProvider")
    static void maskCastLong128VectorTestsLongTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                var res = vmask.cast(LongVector.SPECIES_128);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }
    @Test(dataProvider = "maskProvider")
    static void maskCastLong128VectorTestsFloatTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                var res = vmask.cast(FloatVector.SPECIES_64);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }
    @Test(dataProvider = "maskProvider")
    static void maskCastLong128VectorTestsDoubleTest(IntFunction<boolean[]> fa) {
        boolean[] a = fa.apply(SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vmask = SPECIES.loadMask(a, i);
                var res = vmask.cast(DoubleVector.SPECIES_128);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }

    @Test(dataProvider = "shuffleProvider")
    static void shuffleCastLong128VectorTestsIntTest(BiFunction<Integer,Integer,int[]> fa) {
        int[] a = fa.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vshuffle = VectorShuffle.fromArray(SPECIES, a, i);
                var res = vshuffle.cast(IntVector.SPECIES_64);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }
    @Test(dataProvider = "shuffleProvider")
    static void shuffleCastLong128VectorTestsLongTest(BiFunction<Integer,Integer,int[]> fa) {
        int[] a = fa.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vshuffle = VectorShuffle.fromArray(SPECIES, a, i);
                var res = vshuffle.cast(LongVector.SPECIES_128);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }
    @Test(dataProvider = "shuffleProvider")
    static void shuffleCastLong128VectorTestsFloatTest(BiFunction<Integer,Integer,int[]> fa) {
        int[] a = fa.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vshuffle = VectorShuffle.fromArray(SPECIES, a, i);
                var res = vshuffle.cast(FloatVector.SPECIES_64);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }
    @Test(dataProvider = "shuffleProvider")
    static void shuffleCastLong128VectorTestsDoubleTest(BiFunction<Integer,Integer,int[]> fa) {
        int[] a = fa.apply(SPECIES.length() * BUFFER_REPS, SPECIES.length());
        for (int ic = 0; ic < INVOC_COUNT; ic++) {
            for (int i = 0; i < a.length; i += SPECIES.length()) {
                var vshuffle = VectorShuffle.fromArray(SPECIES, a, i);
                var res = vshuffle.cast(DoubleVector.SPECIES_128);
                assertArraysEquals(res.toArray(), a, i);
            }
        }
    }


    @DataProvider
    public static Object[][] longMaskProvider() {
        return new Object[][]{
                {0xFFFFFFFFFFFFFFFFL},
                {0x0000000000000000L},
                {0x5555555555555555L},
                {0x0123456789abcdefL},
        };
    }

    @Test(dataProvider = "longMaskProvider")
    static void maskFromToLongLong128VectorTestsSmokeTest(long inputLong) {
        var vmask = VectorMask.fromLong(SPECIES, inputLong);
        long outputLong = vmask.toLong();
        Assert.assertEquals(outputLong, inputLong & (((1L << (SPECIES.length() - 1)) << 1) - 1));
    }

    @DataProvider
    public static Object[][] offsetProvider() {
        return new Object[][]{
                {0},
                {-1},
                {+1},
                {+2},
                {-2},
        };
    }

    @Test(dataProvider = "offsetProvider")
    static void indexInRangeLong128VectorTestsSmokeTest(int offset) {
        int limit = SPECIES.length() * BUFFER_REPS;
        for (int i = 0; i < limit; i += SPECIES.length()) {
            var actualMask = SPECIES.indexInRange(i + offset, limit);
            var expectedMask = SPECIES.maskAll(true).indexInRange(i + offset, limit);
            assert(actualMask.equals(expectedMask));
            for (int j = 0; j < SPECIES.length(); j++)  {
                int index = i + j + offset;
                Assert.assertEquals(actualMask.laneIsSet(j), index >= 0 && index < limit);
            }
        }
    }

    @DataProvider
    public static Object[][] lengthProvider() {
        return new Object[][]{
                {0},
                {1},
                {32},
                {37},
                {1024},
                {1024+1},
                {1024+5},
        };
    }

    @Test(dataProvider = "lengthProvider")
    static void loopBoundLong128VectorTestsSmokeTest(int length) {
        int actualLoopBound = SPECIES.loopBound(length);
        int expectedLoopBound = length - Math.floorMod(length, SPECIES.length());
        Assert.assertEquals(actualLoopBound, expectedLoopBound);
    }

    @Test
    static void ElementSizeLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        int elsize = av.elementSize();
        Assert.assertEquals(elsize, Long.SIZE);
    }

    @Test
    static void VectorShapeLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorShape vsh = av.shape();
        assert(vsh.equals(VectorShape.S_128_BIT));
    }

    @Test
    static void ShapeWithLanesLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorShape vsh = av.shape();
        VectorSpecies species = vsh.withLanes(long.class);
        assert(species.equals(SPECIES));
    }

    @Test
    static void ElementTypeLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        assert(av.species().elementType() == long.class);
    }

    @Test
    static void SpeciesElementSizeLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        assert(av.species().elementSize() == Long.SIZE);
    }

    @Test
    static void VectorTypeLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        assert(av.species().vectorType() == av.getClass());
    }

    @Test
    static void WithLanesLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorSpecies species = av.species().withLanes(long.class);
        assert(species.equals(SPECIES));
    }

    @Test
    static void WithShapeLong128VectorTestsSmokeTest() {
        LongVector av = LongVector.zero(SPECIES);
        VectorShape vsh = av.shape();
        VectorSpecies species = av.species().withShape(vsh);
        assert(species.equals(SPECIES));
    }
}

