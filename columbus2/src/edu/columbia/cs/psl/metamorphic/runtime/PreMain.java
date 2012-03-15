package edu.columbia.cs.psl.metamorphic.runtime;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.columbia.cs.psl.metamorphic.compiler.MetamorphicClassFileTransformer;

public class PreMain {
	public static void premain(String args, Instrumentation inst) {
		
		ClassFileTransformer transformer = 
								new MetamorphicClassFileTransformer();
		inst.addTransformer(transformer);
	}
}