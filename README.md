# lib-puller

This library help you to copy data from **application internal directory** (usually /data/data/[your package]) to **public external directory** (usually your sd card directory, /storage/emulate/0).


## Integration

```java
public class MyApplication extends Application {
	
    @Override
    public void onCreate() {
        super.onCreate();

		// Put initialization here
        Puller.getInstance().initialize(this);
    }
	
}
```


## Usage

When you start application, you'll find a notification on the top status bar. Like this:

![usage](https://raw.githubusercontent.com/walfud/lib-puller/master/doc/usage.png)

Just click it when you want to pull your file out.

A moment later(it depends on your data size), a toast message will give you the result:

![result](https://raw.githubusercontent.com/walfud/lib-puller/master/doc/result.png)

Then, you'll find the internal data has been copied to public directory:

![done](https://raw.githubusercontent.com/walfud/lib-puller/master/doc/done.png)