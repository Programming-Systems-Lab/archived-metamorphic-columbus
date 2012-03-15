package edu.columbia.cs.psl.metamorphic.example;
public class ProcessorTester_tests {
public int findClosestValue_0 ( int[] values,  int target, ProcessorTester ___object, java.lang.reflect.Method ___method) throws Exception {
return (Integer) ___method.invoke(___object,new edu.columbia.cs.psl.metamorphic.inputProcessor.impl.MultiplyByNumericConstant().apply(values, 10), target * 10) ;

}public boolean findClosestValue_Check0 (int orig, int metamorphic) {
return metamorphic ==  orig * 10;

}public int findClosestValue_1 ( int[] values,  int target, ProcessorTester ___object, java.lang.reflect.Method ___method) throws Exception {
return (Integer) ___method.invoke(___object,new edu.columbia.cs.psl.metamorphic.inputProcessor.impl.AddNumericConstant().apply(values, 10), target + 10) ;

}public boolean findClosestValue_Check1 (int orig, int metamorphic) {
return metamorphic ==  orig + 10;

}
}