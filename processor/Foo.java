import columbus.Columbus;
import columbus.Forker;

/*
 * This is a comment
 * used by the whole thing
 */
public class Foo {

    /*@
	@meta true
    */
    public int __go(int x) {
	x = 5;
	return 4;
    }
public int go (int x)  {
 int result = __go(x);
 if (Columbus.shouldRunTest("Foo.go")) {
   int pid = Forker.fork();
   if (pid == 0) {
      Affinity.setAffinity(1);
      if (__testGo(x, result) == false) {
           System.out.println("TEST FAILED");
      }
      else {
           System.out.println("test passed");
      }
      Forker.exit();
   }
 }
return result;
}

 public synchronized boolean __testGo(int x, int result) {
    try {
        if (!(true)) return false;
        return true;
    }
    finally {
    }
 }


}
