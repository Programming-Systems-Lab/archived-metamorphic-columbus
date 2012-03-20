package edu.columbia.cs.psl.metamorphic.example;
public class ProcessorTester_tests {
public static  java.lang.String pickFirstString_0 ( java.lang.String[] in, ProcessorTester ___object, java.lang.reflect.Method ___method) throws Exception {
return (java.lang.String) ___method.invoke(___object,new edu.columbia.cs.psl.metamorphic.inputProcessor.impl.Shuffle().apply((Object) in));

}public static  boolean pickFirstString_Check0 (java.lang.String orig, java.lang.String metamorphic, java.lang.String[] in) {
if(orig == null && metamorphic != null) return false; if(orig == null && metamorphic == null) return true;return metamorphic.equals(orig);

}public static  int findClosestValue_0 ( int[] values,  Integer target, ProcessorTester ___object, java.lang.reflect.Method ___method) throws Exception {
return (Integer) ___method.invoke(___object,new edu.columbia.cs.psl.metamorphic.inputProcessor.impl.MultiplyByNumericConstant().apply((Object) values, 10), target * 10);

}public static  boolean findClosestValue_Check0 (int orig, int metamorphic, int[] values,  Integer target) {
return metamorphic == orig * 10;

}public static  int findClosestValue_1 ( int[] values,  Integer target, ProcessorTester ___object, java.lang.reflect.Method ___method) throws Exception {
return (Integer) ___method.invoke(___object,new edu.columbia.cs.psl.metamorphic.inputProcessor.impl.AddNumericConstant().apply((Object) values, 10), target + 10);

}public static  boolean findClosestValue_Check1 (int orig, int metamorphic, int[] values,  Integer target) {
return metamorphic == orig + 10;

}
}