# Java Promises

- Javascript-like promises in java with lambdas

### Usage

> **See** `fetch` example in [`com.promise.Main`](src/com/promise/Main.java)

```java
public class Main {
    public static void main(String[] args) {
        
        Promise<String> promise = new Promise<>(resolver -> {
            for(int i = 0; ; i++) {
                if(i == 1500) {
                    resolver.resolve("The counter hit 1500!");
                    break;
                }
                
                if(i > 1500) {
                    resolver.reject(new Exception("Something Broke!"));
                    break;
                }
            }
        });
        
        promise
            .then(message -> System.out.println(message))
            .Catch(error -> System.out.println("Error: " + error));

        System.out.println("This will run before the promise finishes!");
        
    }
}
```

You can also chain Promises like in the example

```java
import com.promise.Promise;

public class Main {
    public static void main(String[] args) {

        Promise.ContinualPromise<String, Integer> promise = new Promise.ContinualPromise<>(resolver -> {
            resolver.resolve("32");
        });
        
        promise
            .then(stringifiedNumber -> Integer.valueOf(stringifiedNumber))
            .then(number -> System.out.println(number));

    }
}
```