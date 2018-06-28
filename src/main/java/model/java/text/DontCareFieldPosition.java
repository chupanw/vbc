package model.java.text;

import de.fosd.typechef.featureexpr.FeatureExpr;
import edu.cmu.cs.varex.V;

public class DontCareFieldPosition extends FieldPosition {

    public DontCareFieldPosition(FeatureExpr ctx) {
        super(V.one(ctx, 0), ctx, 0);
    }
}
