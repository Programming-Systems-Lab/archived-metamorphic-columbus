package edu.columbia.cs.psl.metamorphic.compiler;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import edu.columbia.cs.psl.metamorphic.runtime.visitor.InterceptingClassVisitor;

public class MetamorphicClassFileTransformer implements ClassFileTransformer {
	private Logger logger = Logger.getLogger(MetamorphicClassFileTransformer.class);
	@Override
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		String name = className.replace("/", ".");
		if(name.startsWith("edu.columbia.cs.psl.metamorphic.example"))
		{			
			logger.info("Rewriting " + name);
			ClassReader cr = new ClassReader(classfileBuffer);
			  ClassWriter cw = new ClassWriter(cr,
		     ClassWriter.COMPUTE_MAXS |
		ClassWriter.COMPUTE_FRAMES);
			  InterceptingClassVisitor cv = new InterceptingClassVisitor(cw);
			  cv.setClassName(name);
			  cr.accept(cv, ClassReader.EXPAND_FRAMES);
			  return cw.toByteArray();
		}
		return classfileBuffer;
	}

}
