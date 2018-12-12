package edu.cmu.cs.vbc.prog.triangle;

import static edu.cmu.cs.vbc.prog.triangle.SchemataLibMethods.*;
import gov.nasa.jpf.annotation.Conditional;

public class Triangle {

    public static final int INVALID = 0;

    public static final int ISOSCELES = 1;

    public static final int SCALENE = 2;

    public static final int EQUILATERAL = 3;

    @Conditional
    public static boolean _mut0 = false, _mut1 = false, _mut2 = false, _mut3 = false, _mut4 = false, _mut5 = false, _mut6 = false, _mut7 = false, _mut8 = false, _mut9 = false, _mut10 = false, _mut11 = false, _mut12 = false, _mut13 = false, _mut14 = false, _mut15 = false, _mut16 = false, _mut17 = false, _mut18 = false, _mut19 = false, _mut20 = false, _mut21 = false, _mut22 = false, _mut23 = false, _mut24 = false, _mut25 = false, _mut26 = false, _mut27 = false, _mut28 = false, _mut29 = false, _mut30 = false, _mut31 = false, _mut32 = false, _mut33 = false, _mut34 = false, _mut35 = false, _mut36 = false, _mut37 = false, _mut38 = false, _mut39 = false, _mut40 = false, _mut41 = false, _mut42 = false, _mut43 = false, _mut44 = false, _mut45 = false, _mut46 = false, _mut47 = false, _mut48 = false, _mut49 = false, _mut50 = false, _mut51 = false, _mut52 = false, _mut53 = false, _mut54 = false, _mut55 = false, _mut56 = false, _mut57 = false, _mut58 = false, _mut59 = false, _mut60 = false, _mut61 = false, _mut62 = false, _mut63 = false, _mut64 = false, _mut65 = false, _mut66 = false, _mut67 = false, _mut68 = false, _mut69 = false, _mut70 = false, _mut71 = false, _mut72 = false, _mut73 = false, _mut74 = false, _mut75 = false, _mut76 = false, _mut77 = false, _mut78 = false, _mut79 = false, _mut80 = false, _mut81 = false, _mut82 = false, _mut83 = false, _mut84 = false, _mut85 = false, _mut86 = false, _mut87 = false, _mut88 = false, _mut89 = false, _mut90 = false, _mut91 = false, _mut92 = false, _mut93 = false, _mut94 = false, _mut95 = false, _mut96 = false, _mut97 = false, _mut98 = false, _mut99 = false, _mut100 = false, _mut101 = false, _mut102 = false, _mut103 = false, _mut104 = false, _mut105 = false, _mut106 = false, _mut107 = false, _mut108 = false, _mut109 = false, _mut110 = false, _mut111 = false, _mut112 = false, _mut113 = false, _mut114 = false, _mut115 = false, _mut116 = false, _mut117 = false, _mut118 = false, _mut119 = false, _mut120 = false, _mut121 = false, _mut122 = false, _mut123 = false, _mut124 = false, _mut125 = false, _mut126 = false, _mut127 = false;

    public int classify(int a, int b, int c) {
        int trian;
        if (LCR_or(LCR_or(ROR_less_equals(a, 0, _mut0, _mut1, _mut2, _mut3, _mut4), ROR_less_equals(b, 0, _mut5, _mut6, _mut7, _mut8, _mut9), _mut10), ROR_less_equals(c, 0, _mut11, _mut12, _mut13, _mut14, _mut15), _mut16))
            return INVALID;
        trian = 0;
        if (ROR_equals(a, b, _mut17, _mut18, _mut19, _mut20, _mut21))
            trian = AOR_plus(trian, 1, _mut22, _mut23, _mut24, _mut25);
        if (ROR_equals(a, c, _mut26, _mut27, _mut28, _mut29, _mut30))
            trian = AOR_plus(trian, 2, _mut31, _mut32, _mut33, _mut34);
        if (ROR_equals(b, c, _mut35, _mut36, _mut37, _mut38, _mut39))
            trian = AOR_plus(trian, 3, _mut40, _mut41, _mut42, _mut43);
        if (ROR_equals(trian, 0, _mut44, _mut45, _mut46, _mut47, _mut48))
            if (LCR_or(LCR_or(ROR_less(AOR_plus(a, b, _mut49, _mut50, _mut51, _mut52), c, _mut53, _mut54, _mut55, _mut56, _mut57), ROR_less(AOR_plus(a, c, _mut58, _mut59, _mut60, _mut61), b, _mut62, _mut63, _mut64, _mut65, _mut66), _mut67), ROR_less(AOR_plus(b, c, _mut68, _mut69, _mut70, _mut71), a, _mut72, _mut73, _mut74, _mut75, _mut76), _mut77))
                return INVALID;
            else
                return SCALENE;
        if (ROR_greater(trian, 3, _mut78, _mut79, _mut80, _mut81, _mut82))
            return EQUILATERAL;
        if (LCR_and(ROR_equals(trian, 1, _mut83, _mut84, _mut85, _mut86, _mut87), ROR_greater(AOR_plus(a, b, _mut88, _mut89, _mut90, _mut91), c, _mut92, _mut93, _mut94, _mut95, _mut96), _mut97))
            return ISOSCELES;
        else if (LCR_and(ROR_equals(trian, 2, _mut98, _mut99, _mut100, _mut101, _mut102), ROR_greater(AOR_plus(a, c, _mut103, _mut104, _mut105, _mut106), b, _mut107, _mut108, _mut109, _mut110, _mut111), _mut112))
            return ISOSCELES;
        else if (LCR_and(ROR_equals(trian, 3, _mut113, _mut114, _mut115, _mut116, _mut117), ROR_greater(AOR_plus(b, c, _mut118, _mut119, _mut120, _mut121), a, _mut122, _mut123, _mut124, _mut125, _mut126), _mut127))
            return ISOSCELES;
        return INVALID;
    }
}
