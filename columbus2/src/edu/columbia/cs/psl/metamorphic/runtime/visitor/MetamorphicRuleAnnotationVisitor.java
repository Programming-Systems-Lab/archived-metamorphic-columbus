package edu.columbia.cs.psl.metamorphic.runtime.visitor;

import java.util.ArrayList;

import org.objectweb.asm.AnnotationVisitor;

public class MetamorphicRuleAnnotationVisitor extends AnnotationVisitor {


	public MetamorphicRuleAnnotationVisitor(int api, AnnotationVisitor av) {
		super(api, av);
	}
	private ArrayList<String> rules = new ArrayList<String>();
	@Override
	public void visit(String name, Object value) {
		rules.add((String) value);
		super.visit(name, value);
	}
	public ArrayList<String> getRules() {
		if(child != null)
			return child.getRules();
		return rules;
	}
	private MetamorphicRuleAnnotationVisitor child;
	@Override
	public AnnotationVisitor visitArray(String name) {
		child = new MetamorphicRuleAnnotationVisitor(api, super.visitArray(name));
		return child;
	}
}
