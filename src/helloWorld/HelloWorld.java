package helloWorld;

public class HelloWorld {

	public native void sayHello();

	static {
		System.loadLibrary("mtdev4j");
	}

	public static void main(String[] args) {
		HelloWorld h = new HelloWorld();
		h.sayHello();
	}
}
