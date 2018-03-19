package model.java.lang;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

/**
 * Trigger expanding array by modifying arraycopy signature.
 *
 * @author chupanw
 */
public class System {
    public static void arraycopy(Object[] src, int srcPos, Object[] dest, int destPos, int length) {
        java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static V<?> vArrayCopy(V<V[]> vsrc,
                                  V<java.lang.Integer> vsrcPos,
                                  V<V[]> vdest,
                                  V<java.lang.Integer> vdestPos,
                                  V<java.lang.Integer> vlength,
                                  FeatureExpr ctx) {

        vsrc.sforeach(ctx, (f1, src) -> {
            vdest.sforeach(f1, (f2, dest) -> {
                vsrcPos.sforeach(f2, (f3, srcPos) -> {
                    vdestPos.sforeach(f3, (f4, destPos) -> {
                        vlength.sforeach(f4, (f5, length) -> {
                            if (dest == src) {
                                if (srcPos > destPos) {
                                    for (int i = 0; i < length; i++) {
                                        dest[destPos + i] = V.choice(f5, src[srcPos + i], dest[destPos + i]);
                                    }
                                }
                                else {
                                    for (int i = length - 1; i >= 0; i--) {
                                        dest[destPos + i] = V.choice(f5, src[srcPos + i], dest[destPos + i]);
                                    }
                                }
                            }
                            else {
                                for (int i = 0; i < length; i++) {
                                    dest[destPos + i] = V.choice(f5, src[srcPos + i], dest[destPos + i]);
                                }
                            }
                        });
                    });
                });
            });
        });
        return null;    // dummy value
    }
}
