package edu.cmu.cs.vbc.prog.elevator;


import edu.cmu.cs.varex.annotation.VConditional;

public class FeatureSwitches {
/*
 * DO NOT EDIT! THIS FILE IS AUTOGENERATED BY fstcomp 
 */

@VConditional
public static boolean __SELECTED_FEATURE_base = true;
@VConditional
public static boolean __SELECTED_FEATURE_weight = true;
@VConditional
public static boolean __SELECTED_FEATURE_empty = true;
@VConditional
public static boolean __SELECTED_FEATURE_twothirdsfull = true;
@VConditional
public static boolean __SELECTED_FEATURE_executivefloor = true;
@VConditional
public static boolean __SELECTED_FEATURE_overloaded = false;

public static boolean valid_product() {
	if ((( !__SELECTED_FEATURE_overloaded || __SELECTED_FEATURE_weight ) && ( !__SELECTED_FEATURE_twothirdsfull || __SELECTED_FEATURE_weight )) && ( __SELECTED_FEATURE_base ))
		return true;
	else
		return false;
}

public static void init(String... args) {
	int index = 0;			
	__SELECTED_FEATURE_base = Boolean.valueOf(args[index++]);			
	__SELECTED_FEATURE_weight = Boolean.valueOf(args[index++]);
	__SELECTED_FEATURE_empty = Boolean.valueOf(args[index++]);
	__SELECTED_FEATURE_twothirdsfull =Boolean.valueOf(args[index++]);
	__SELECTED_FEATURE_executivefloor = Boolean.valueOf(args[index++]);
	__SELECTED_FEATURE_overloaded = Boolean.valueOf(args[index++]);
}

}